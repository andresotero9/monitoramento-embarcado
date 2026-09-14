package br.com.monitoramento.monitoring.camera;

/**
 * Resultado de uma tentativa de ping.
 *
 * @param sucesso    {@code true} quando o host respondeu dentro do timeout
 * @param tempoPingMs tempo de resposta em milissegundos; {@code null} quando falhou
 */
public record PingResult(boolean sucesso, Long tempoPingMs) {

    public static PingResult sucesso(long tempoPingMs) {
        return new PingResult(true, tempoPingMs);
    }

    public static PingResult falha() {
        return new PingResult(false, null);
    }
}
