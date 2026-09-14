package br.com.monitoramento.monitoring.camera;

import br.com.monitoramento.camera.entity.Camera;
import br.com.monitoramento.camera.rtsp.RtspUrlBuilder;
import br.com.monitoramento.config_sistema.service.ConfiguracaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Tenta capturar um único frame do stream RTSP da câmera utilizando FFmpeg.
 *
 * <p>Diferente do {@code HlsStreamService} (Etapa 7), que mantém um processo
 * FFmpeg contínuo para servir vídeo ao vivo ao usuário, esta implementação dispara
 * um processo curto com {@code -frames:v 1}, suficiente para validar que o canal
 * RTSP está respondendo — sem manter overhead de streaming contínuo apenas para
 * fins de monitoramento.</p>
 */
@Component
public class CameraStreamCheckerImpl implements CameraStreamChecker {

    private static final Logger log = LoggerFactory.getLogger(CameraStreamCheckerImpl.class);
    private static final String CHAVE_TIMEOUT_MS = "camera.rtsp.timeout.ms";

    private final RtspUrlBuilder rtspUrlBuilder;
    private final ConfiguracaoService configuracaoService;
    private final String ffmpegPath;

    public CameraStreamCheckerImpl(RtspUrlBuilder rtspUrlBuilder, ConfiguracaoService configuracaoService, @Value("${app.streaming.ffmpeg-path}") String ffmpegPath) {
        this.rtspUrlBuilder = rtspUrlBuilder;
        this.configuracaoService = configuracaoService;
        this.ffmpegPath = ffmpegPath;
    }

    /**
     * Tenta capturar um único frame do stream RTSP da câmera.
     * Se o processo FFmpeg não concluir dentro do timeout configurado,
     * ou se nenhum frame for capturado, retorna {@code StreamCheckResult.falha()} com a mensagem apropriada
     *
     * @param camera
     * @return StreamCheckResult indicando sucesso ou falha na captura do frame
     */
    @Override
    public StreamCheckResult check(Camera camera) {
        int timeoutMs = configuracaoService.getInt(CHAVE_TIMEOUT_MS, 5000);
        String rtspUrl = rtspUrlBuilder.construir(camera);
        Path arquivoTemporario = criarArquivoTemporario(camera.getId());

        try {
            List<String> comando = montarComandoFfmpeg(rtspUrl, timeoutMs, arquivoTemporario);
            ProcessBuilder pb = new ProcessBuilder(comando);
            pb.redirectErrorStream(true);
            Process processo = pb.start();

            boolean concluiu = processo.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            if (!concluiu) {
                processo.destroyForcibly();
                return StreamCheckResult.falha("Timeout ao tentar capturar frame RTSP");
            }

            boolean frameValido = Files.exists(arquivoTemporario) && Files.size(arquivoTemporario) > 0;
            if (!frameValido) {
                return StreamCheckResult.falha("Nenhum frame RTSP capturado (câmera pode estar offline)");
            }

            return StreamCheckResult.sucesso();
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("Falha ao verificar stream RTSP da câmera {}: {}", camera.getId(), e.getMessage());
            return StreamCheckResult.falha("Erro ao executar FFmpeg: " + e.getMessage());
        } finally {
            limparArquivoTemporario(arquivoTemporario);
        }
    }

    /**
     * Monta o comando FFmpeg para capturar um único frame do stream RTSP.
     *
     * @param rtspUrl
     * @param timeoutMs
     * @param arquivoSaida
     * @return Lista de strings representando o comando FFmpeg
     */
    private List<String> montarComandoFfmpeg(String rtspUrl, int timeoutMs, Path arquivoSaida) {
        List<String> comando = new ArrayList<>();
        comando.add(ffmpegPath);
        comando.add("-y");
        comando.add("-rtsp_transport");
        comando.add("tcp");
        comando.add("-timeout");
        comando.add(String.valueOf(timeoutMs * 1000L)); // FFmpeg espera microssegundos
        comando.add("-i");
        comando.add(rtspUrl);
        comando.add("-frames:v");
        comando.add("1");
        comando.add("-f");
        comando.add("image2");
        comando.add(arquivoSaida.toString());
        return comando;
    }

    /**
     * Cria um arquivo temporário para armazenar o frame capturado do stream RTSP.
     * O arquivo será excluído após a verificação.
     *
     * @param cameraId ID da câmera, usado para nomear o arquivo temporário
     * @return Path do arquivo temporário criado
     */
    private Path criarArquivoTemporario(Long cameraId) {
        try {
            return Files.createTempFile("camera-check-" + cameraId + "-", ".jpg");
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao criar arquivo temporário para verificação RTSP", e);
        }
    }

    /**
     * Remove o arquivo temporário criado para armazenar o frame capturado.
     * Se a exclusão falhar, apenas registra um aviso no log.
     *
     * @param arquivo Path do arquivo temporário a ser removido
     */
    private void limparArquivoTemporario(Path arquivo) {
        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            log.warn("Falha ao remover arquivo temporário {}: {}", arquivo, e.getMessage());
        }
    }
}

