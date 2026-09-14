package br.com.monitoramento.monitoring.camera;

/**
 * Abstrai o teste de ping a um host, permitindo que {@code CameraMonitoringService}
 * seja testado com um mock, sem depender de rede real (ver Seção 30 dos requisitos).
 */
public interface PingChecker {

    PingResult ping(String enderecoIp, int timeoutMs);
}
