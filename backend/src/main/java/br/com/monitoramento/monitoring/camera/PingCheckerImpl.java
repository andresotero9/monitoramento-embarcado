package br.com.monitoramento.monitoring.camera;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Executa o comando {@code ping} do Linux via {@link ProcessBuilder}.
 *
 * <p>Optou-se por invocar o binário do sistema em vez de {@code InetAddress.isReachable}
 * porque este último costuma exigir privilégios de root para enviar pacotes ICMP em Linux,
 * o que nem sempre está disponível dentro de um container.</p>
 */
@Component
public class PingCheckerImpl implements PingChecker {

    private static final Logger log = LoggerFactory.getLogger(PingCheckerImpl.class);

    @Override
    public PingResult ping(String enderecoIp, int timeoutMs) {
        int timeoutSegundos = Math.max(1, timeoutMs / 1000);
        long inicio = System.currentTimeMillis();

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ping", "-c", "1", "-W", String.valueOf(timeoutSegundos), enderecoIp);
            pb.redirectErrorStream(true);
            Process processo = pb.start();

            boolean concluiu = processo.waitFor(timeoutMs + 1000L, TimeUnit.MILLISECONDS);
            if (!concluiu) {
                processo.destroyForcibly();
                return PingResult.falha();
            }

            long tempoDecorrido = System.currentTimeMillis() - inicio;
            return processo.exitValue() == 0 ? PingResult.sucesso(tempoDecorrido) : PingResult.falha();
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("Falha ao executar ping para {}: {}", enderecoIp, e.getMessage());
            return PingResult.falha();
        }
    }
}
