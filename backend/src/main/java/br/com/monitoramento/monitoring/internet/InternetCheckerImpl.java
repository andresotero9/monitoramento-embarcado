package br.com.monitoramento.monitoring.internet;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Testa conectividade abrindo uma conexão TCP na porta 53 (DNS) do IP configurado.
 *
 * <p>Optou-se por uma conexão TCP em vez de ICMP ({@code InetAddress.isReachable})
 * porque ICMP costuma exigir privilégios de root em Linux e é frequentemente
 * bloqueado por firewalls, tornando falsos negativos comuns. A porta 53 (DNS) é
 * escolhida por estar quase sempre acessível em servidores públicos como 8.8.8.8 (Google Public DNS).</p>
 */
@Component
public class InternetCheckerImpl implements InternetChecker {

    /**
     * Porta 53 (DNS) em vez de ICMP (ping) — o comentário no código explica essa escolha: ICMP normalmente exige
     * privilégios de root/admin no Linux e é frequentemente bloqueado por firewalls corporativos, causando falsos
     * negativos (a internet estar OK mas o ping falhar mesmo assim). A porta 53 quase sempre está acessível em
     * servidores DNS públicos como esse, tornando o teste mais confiável.
     */
    private static final int PORTA_TESTE = 53;

    @Override
    public InternetCheckResult check(String enderecoIp, int timeoutMs) {
        long inicio = System.currentTimeMillis();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(enderecoIp, PORTA_TESTE), timeoutMs);
            long tempoResposta = System.currentTimeMillis() - inicio;
            return InternetCheckResult.sucesso(tempoResposta);
        } catch (IOException e) {
            return InternetCheckResult.falha("Falha ao conectar em " + enderecoIp + ": " + e.getMessage());
        }
    }
}
