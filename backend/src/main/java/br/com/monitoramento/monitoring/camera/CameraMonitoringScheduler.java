package br.com.monitoramento.monitoring.camera;

import br.com.monitoramento.monitoring.camera.service.CameraMonitoringService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Aciona a verificação de todas as câmeras cadastradas a cada 15 segundos (tick curto).
 *
 * <p>Assim como nos demais monitoramentos, {@link CameraMonitoringService} decide,
 * a cada tick, se uma verificação real deve ocorrer com base na periodicidade
 * configurada ({@code camera.periodicidade.segundos}).</p>
 */
@Component
public class CameraMonitoringScheduler {

    private final CameraMonitoringService cameraMonitoringService;

    public CameraMonitoringScheduler(CameraMonitoringService cameraMonitoringService) {
        this.cameraMonitoringService = cameraMonitoringService;
    }

    @Scheduled(fixedDelay = 15 * 1000)
    public void tick() {
        cameraMonitoringService.executarRotina();
    }
}
