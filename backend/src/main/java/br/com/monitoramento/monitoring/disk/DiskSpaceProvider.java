package br.com.monitoramento.monitoring.disk;

/**
 * Abstrai a leitura de uso de disco do filesystem, permitindo que
 * {@code DiskMonitoringService} seja testado com um mock, sem depender do
 * filesystem real (ver Seção 30 dos requisitos).
 */
public interface DiskSpaceProvider {

    /**
     * Lê o uso de disco do caminho informado.
     *
     * @param path caminho do filesystem a ser inspecionado (ex.: "/")
     */
    DiskSpaceInfo lerUsoDisco(String path);
}
