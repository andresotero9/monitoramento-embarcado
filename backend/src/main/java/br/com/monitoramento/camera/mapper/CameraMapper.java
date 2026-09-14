package br.com.monitoramento.camera.mapper;

import br.com.monitoramento.camera.dto.CameraResponseDTO;
import br.com.monitoramento.camera.entity.Camera;
import org.springframework.stereotype.Component;

/**
 * Converte entre a entidade {@link Camera} e seus DTOs de API.
 *
 * <p>A conversão de {@code CameraRequestDTO} para {@link Camera} não está aqui
 * pois exige cifrar a senha ({@link br.com.monitoramento.camera.security.CameraCredentialCipher}),
 * responsabilidade mantida no {@code CameraService} para não vazar a dependência de
 * criptografia para a camada de mapeamento.</p>
 */
@Component
public class CameraMapper {

    public CameraResponseDTO toResponseDTO(Camera camera) {
        return new CameraResponseDTO(
                camera.getId(),
                camera.getNome(),
                camera.getDescricao(),
                camera.getEnderecoIp(),
                camera.getPortaHttp(),
                camera.getPortaRtsp(),
                camera.getUsuario(),
                camera.isAtiva(),
                camera.getDataCriacao(),
                camera.getDataAtualizacao()
        );
    }
}
