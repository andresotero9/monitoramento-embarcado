package br.com.monitoramento.streaming;

import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Representa uma sessão de streaming HLS ativa para uma câmera.
 *
 * <p>Não é persistida no banco — vive apenas em memória enquanto o processo
 * FFmpeg está ativo, gerenciada por {@code StreamSessionManager}.</p>
 */
class StreamSession {

    private final Long cameraId;
    private final Process processo;
    private final Path diretorioSaida;
    private final String token;
    private final AtomicReference<Instant> ultimoAcesso;

    StreamSession(Long cameraId, Process processo, Path diretorioSaida, String token) {
        this.cameraId = cameraId;
        this.processo = processo;
        this.diretorioSaida = diretorioSaida;
        this.token = token;
        this.ultimoAcesso = new AtomicReference<>(Instant.now());
    }

    Long getCameraId() {
        return cameraId;
    }

    Process getProcesso() {
        return processo;
    }

    Path getDiretorioSaida() {
        return diretorioSaida;
    }

    String getToken() {
        return token;
    }

    Instant getUltimoAcesso() {
        return ultimoAcesso.get();
    }

    void marcarAcesso() {
        this.ultimoAcesso.set(Instant.now());
    }

    boolean isProcessoVivo() {
        return processo.isAlive();
    }
}
