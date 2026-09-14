package br.com.monitoramento.streaming;

/**
 * Status de uma sessão de streaming HLS sob demanda.
 */
public enum StreamStatus {
    /** Nenhuma sessão ativa para a câmera. */
    NAO_INICIADO,
    /** Processo FFmpeg em execução, mas ainda sem segmentos suficientes para reprodução. */
    INICIANDO,
    /** Playlist HLS pronta para reprodução. */
    PRONTO
}
