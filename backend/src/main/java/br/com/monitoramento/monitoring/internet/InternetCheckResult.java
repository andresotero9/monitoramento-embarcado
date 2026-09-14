package br.com.monitoramento.monitoring.internet;

/**
 * Resultado de uma tentativa de verificação de conectividade.
 *
 * @param sucesso        {@code true} quando a conexão foi estabelecida com sucesso
 * @param tempoRespostaMs tempo de resposta em milissegundos; {@code null} quando falhou
 * @param mensagemErro   descrição do erro, quando {@code sucesso} é {@code false}
 */
public record InternetCheckResult(boolean sucesso, Long tempoRespostaMs, String mensagemErro) {

    public static InternetCheckResult sucesso(long tempoRespostaMs) {
        return new InternetCheckResult(true, tempoRespostaMs, null);
    }

    public static InternetCheckResult falha(String mensagemErro) {
        return new InternetCheckResult(false, null, mensagemErro);
    }
}
