package br.com.monitoramento.monitoring.internet;

import br.com.monitoramento.monitoring.internet.service.InternetMonitoringService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Aciona a verificação de conectividade a cada 10 segundos (tick curto).
 *
 * <p>O tick é propositalmente mais frequente que a periodicidade configurada
 * (padrão 60s): quem decide se uma verificação real deve ocorrer é
 * {@link InternetMonitoringService#executarRotina()}, que compara
 * o tempo decorrido com o valor de {@code internet.periodicidade.segundos} lido do
 * banco a cada tick. Isso permite alterar a periodicidade em runtime sem reiniciar
 * a aplicação.</p>
 */
@Component
public class InternetMonitoringScheduler {

    private final InternetMonitoringService internetMonitoringService;

    public InternetMonitoringScheduler(InternetMonitoringService internetMonitoringService) {
        this.internetMonitoringService = internetMonitoringService;
    }

    @Scheduled(fixedDelay = 10_000)
    public void tick() {
        internetMonitoringService.executarRotina();
    }
}
