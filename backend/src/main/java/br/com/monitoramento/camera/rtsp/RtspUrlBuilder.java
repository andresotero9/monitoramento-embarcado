package br.com.monitoramento.camera.rtsp;

import br.com.monitoramento.camera.entity.Camera;
import br.com.monitoramento.camera.security.CameraCredentialCipher;
import br.com.monitoramento.config_sistema.service.ConfiguracaoService;
import org.springframework.stereotype.Component;

/**
 * Monta a URL RTSP de conexão com uma câmera, decifrando a senha e aplicando
 * o caminho RTSP configurado globalmente ({@code camera.rtsp.caminho}).
 *
 * <p>Compartilhado entre {@code CameraStreamCheckerImpl} (monitoramento periódico)
 * e {@code HlsStreamService} (streaming sob demanda) para evitar duplicação de
 * lógica entre os dois usos de RTSP.</p>
 */
@Component
public class RtspUrlBuilder {

    private static final String CHAVE_CAMINHO_RTSP = "camera.rtsp.caminho";

    private final CameraCredentialCipher credentialCipher;
    private final ConfiguracaoService configuracaoService;

    public RtspUrlBuilder(CameraCredentialCipher credentialCipher, ConfiguracaoService configuracaoService) {
        this.credentialCipher = credentialCipher;
        this.configuracaoService = configuracaoService;
    }

    /**
     * Constrói a URL RTSP completa para a câmera, incluindo usuário, senha decifrada,
     * endereço IP, porta e caminho RTSP configurado.
     * @param camera
     * @return URL RTSP completa
     */
    public String construir(Camera camera) {
        String senha = credentialCipher.decifrar(camera.getSenhaCriptografada());
        String caminho = configuracaoService.getString(CHAVE_CAMINHO_RTSP, "/");

        StringBuilder url = new StringBuilder("rtsp://");
        if (camera.getUsuario() != null && !camera.getUsuario().isBlank()) {
            url.append(camera.getUsuario());
            if (senha != null && !senha.isBlank()) {
                url.append(':').append(senha);
            }
            url.append('@');
        }
        url.append(camera.getEnderecoIp()).append(':').append(camera.getPortaRtsp());
        if (!caminho.startsWith("/")) {
            url.append('/');
        }
        url.append(caminho);
        return url.toString();
    }
}
