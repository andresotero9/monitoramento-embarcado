package br.com.monitoramento.monitoring.camera;

/**
 * Resultado de uma tentativa de captura de um frame via RTSP.
 *
 * @param frameCapturado {@code true} quando ao menos um frame foi obtido com sucesso
 * @param mensagemErro   descrição do erro, quando {@code frameCapturado} é {@code false}
 */
public record StreamCheckResult(boolean frameCapturado, String mensagemErro) {

    public static StreamCheckResult sucesso() {
        return new StreamCheckResult(true, null);
    }

    public static StreamCheckResult falha(String mensagemErro) {
        return new StreamCheckResult(false, mensagemErro);
    }
}
