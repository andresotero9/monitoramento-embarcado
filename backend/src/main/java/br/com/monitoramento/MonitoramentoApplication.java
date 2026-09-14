package br.com.monitoramento;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Ponto de entrada da aplicação de monitoramento de equipamento embarcado.
 *
 * <p>O agendamento de tarefas ({@code @EnableScheduling}) é habilitado aqui pois
 * os schedulers de Internet, Disco e Câmeras (implementados na Etapa 6) dependem
 * dele para executar periodicamente.</p>
 */
@SpringBootApplication
@EnableScheduling
public class MonitoramentoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitoramentoApplication.class, args);
    }
}
