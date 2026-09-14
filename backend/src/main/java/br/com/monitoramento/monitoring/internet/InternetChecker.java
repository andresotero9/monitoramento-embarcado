package br.com.monitoramento.monitoring.internet;

/**
 * Abstrai o teste de conectividade com a Internet, permitindo que
 * {@code InternetMonitoringService} seja testado com um mock, sem depender
 * de rede real (ver Seção 30 dos requisitos).
 */
public interface InternetChecker {

    /**
     * Testa a conectividade com o endereço informado.
     *
     * @param enderecoIp endereço IP ou host a ser testado
     * @param timeoutMs  tempo máximo de espera em milissegundos
     */
    InternetCheckResult check(String enderecoIp, int timeoutMs);
}
