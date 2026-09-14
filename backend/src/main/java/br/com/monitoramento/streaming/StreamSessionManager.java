package br.com.monitoramento.streaming;

import br.com.monitoramento.camera.entity.Camera;
import br.com.monitoramento.camera.service.CameraService;
import br.com.monitoramento.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Gerencia o ciclo de vida das sessões de streaming HLS ativas em memória.
 *
 * <p>Garante no máximo um processo FFmpeg ativo por câmera, emite um token opaco
 * de acesso por sessão (usado nas URLs de playlist/segmentos, já que a tag
 * {@code <video>} não envia headers customizados) e encerra automaticamente
 * sessões inativas por mais de {@code app.streaming.inatividade-timeout-segundos},
 * evitando processos FFmpeg e arquivos temporários órfãos.</p>
 */
@Component
public class StreamSessionManager {

    private static final Logger log = LoggerFactory.getLogger(StreamSessionManager.class);

    private final Map<Long, StreamSession> sessoesAtivas = new ConcurrentHashMap<>();

    private final HlsStreamService hlsStreamService;
    private final CameraService cameraService;
    private final long inatividadeTimeoutSegundos;

    public StreamSessionManager(HlsStreamService hlsStreamService, CameraService cameraService,
                                 @Value("${app.streaming.inatividade-timeout-segundos}") long inatividadeTimeoutSegundos) {
        this.hlsStreamService = hlsStreamService;
        this.cameraService = cameraService;
        this.inatividadeTimeoutSegundos = inatividadeTimeoutSegundos;
    }

    /**
     * Inicia (ou reaproveita, se já ativa) a sessão de streaming da câmera.
     *
     * @return o token de acesso a ser usado nas URLs de playlist/segmentos
     */
    public synchronized String iniciar(Long cameraId) {
        StreamSession existente = sessoesAtivas.get(cameraId);
        if (existente != null && existente.isProcessoVivo()) {
            existente.marcarAcesso();
            return existente.getToken();
        }

        Camera camera = cameraService.buscarEntidadeOuFalhar(cameraId);
        if (!camera.isAtiva()) {
            throw new BusinessException("Não é possível iniciar stream de uma câmera inativa: id=" + cameraId);
        }

        Path diretorioSaida = hlsStreamService.resolverDiretorioSaida(cameraId);
        String token = UUID.randomUUID().toString();

        try {
            Process processo = hlsStreamService.iniciarProcesso(camera, diretorioSaida);
            sessoesAtivas.put(cameraId, new StreamSession(cameraId, processo, diretorioSaida, token));
            log.info("Sessão de streaming iniciada para câmera id={}", cameraId);
            return token;
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao iniciar streaming para câmera id=" + cameraId, e);
        }
    }

    public synchronized void parar(Long cameraId) {
        StreamSession sessao = sessoesAtivas.remove(cameraId);
        if (sessao == null) {
            return; // Operação idempotente: já não há sessão ativa.
        }
        encerrarSessao(sessao);
    }

    public StreamStatus status(Long cameraId) {
        StreamSession sessao = sessoesAtivas.get(cameraId);
        if (sessao == null || !sessao.isProcessoVivo()) {
            return StreamStatus.NAO_INICIADO;
        }
        Path playlist = hlsStreamService.resolverArquivoPlaylist(sessao.getDiretorioSaida());
        return Files.exists(playlist) ? StreamStatus.PRONTO : StreamStatus.INICIANDO;
    }

    /**
     * Valida o token de acesso e, se válido, renova o timestamp de última
     * atividade da sessão (evitando que ela seja encerrada por inatividade
     * enquanto o player continua consumindo os arquivos).
     */
    public boolean validarTokenERenovarAcesso(Long cameraId, String token) {
        StreamSession sessao = sessoesAtivas.get(cameraId);
        if (sessao == null || !sessao.getToken().equals(token)) {
            return false;
        }
        sessao.marcarAcesso();
        return true;
    }

    public Path resolverArquivo(Long cameraId, String nomeArquivo) {
        StreamSession sessao = sessoesAtivas.get(cameraId);
        if (sessao == null) {
            return null;
        }
        return sessao.getDiretorioSaida().resolve(nomeArquivo);
    }

    /**
     * Encerra periodicamente sessões sem atividade recente, evitando processos
     * FFmpeg e diretórios temporários órfãos quando o usuário fecha a aba sem
     * chamar {@code /stop} explicitamente.
     */
    @Scheduled(fixedDelay = 30_000)
    void limparSessoesInativas() {
        Instant limite = Instant.now().minusSeconds(inatividadeTimeoutSegundos);

        sessoesAtivas.entrySet().removeIf(entry -> {
            StreamSession sessao = entry.getValue();
            boolean inativa = sessao.getUltimoAcesso().isBefore(limite);
            boolean processoMorto = !sessao.isProcessoVivo();

            if (inativa || processoMorto) {
                log.info("Encerrando sessão de streaming por inatividade: cameraId={}", entry.getKey());
                encerrarSessao(sessao);
                return true;
            }
            return false;
        });
    }

    private void encerrarSessao(StreamSession sessao) {
        if (sessao.isProcessoVivo()) {
            sessao.getProcesso().destroy();
            try {
                if (!sessao.getProcesso().waitFor(5, TimeUnit.SECONDS)) {
                    sessao.getProcesso().destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                sessao.getProcesso().destroyForcibly();
            }
        }
        limparDiretorio(sessao.getDiretorioSaida());
    }

    private void limparDiretorio(Path diretorio) {
        if (!Files.exists(diretorio)) {
            return;
        }
        try (var arquivos = Files.walk(diretorio)) {
            arquivos.sorted(Comparator.reverseOrder()).forEach(caminho -> {
                try {
                    Files.deleteIfExists(caminho);
                } catch (IOException e) {
                    log.warn("Falha ao remover arquivo temporário de streaming {}: {}", caminho, e.getMessage());
                }
            });
        } catch (IOException e) {
            log.warn("Falha ao limpar diretório de streaming {}: {}", diretorio, e.getMessage());
        }
    }
}
