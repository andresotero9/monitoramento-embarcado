package br.com.monitoramento.monitoring.disk;

import br.com.monitoramento.monitoring.disk.service.DiskMonitoringService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Aciona a verificação de uso de disco a cada 30 segundos (tick curto).
 *
 * <p>Assim como no monitoramento de Internet, o tick é mais frequente que a
 * periodicidade configurada (padrão 300s); {@link DiskMonitoringService} decide
 * se uma verificação real deve ocorrer.</p>
 */
@Component
public class DiskMonitoringScheduler {

    private final DiskMonitoringService diskMonitoringService;

    public DiskMonitoringScheduler(DiskMonitoringService diskMonitoringService) {
        this.diskMonitoringService = diskMonitoringService;
    }

    @Scheduled(fixedDelay = 30 * 1000)
    public void tick() {
        diskMonitoringService.executarRotina();
    }
}
