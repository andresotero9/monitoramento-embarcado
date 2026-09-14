package br.com.monitoramento.camera.service;

import br.com.monitoramento.camera.dto.CameraRequestDTO;
import br.com.monitoramento.camera.dto.CameraResponseDTO;
import br.com.monitoramento.camera.entity.Camera;
import br.com.monitoramento.camera.mapper.CameraMapper;
import br.com.monitoramento.camera.repository.CameraRepository;
import br.com.monitoramento.camera.security.CameraCredentialCipher;
import br.com.monitoramento.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Regras de negócio para cadastro e consulta de câmeras.
 *
 * <p>A senha da câmera é sempre cifrada antes de persistir e nunca retorna
 * em texto puro nas respostas da API (ver {@link CameraResponseDTO}).</p>
 */
@Service
public class CameraService {

    private final CameraRepository cameraRepository;
    private final CameraMapper cameraMapper;
    private final CameraCredentialCipher credentialCipher;

    public CameraService(CameraRepository cameraRepository, CameraMapper cameraMapper,
                          CameraCredentialCipher credentialCipher) {
        this.cameraRepository = cameraRepository;
        this.cameraMapper = cameraMapper;
        this.credentialCipher = credentialCipher;
    }

    @Transactional(readOnly = true)
    public List<CameraResponseDTO> listarTodas() {
        return cameraRepository.findAll().stream()
                .map(cameraMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CameraResponseDTO buscarPorId(Long id) {
        return cameraMapper.toResponseDTO(buscarEntidadeOuFalhar(id));
    }

    @Transactional
    public CameraResponseDTO criar(CameraRequestDTO dto) {
        String senhaCifrada = credentialCipher.cifrar(dto.getSenha());
        Camera camera = new Camera(
                dto.getNome(), dto.getDescricao(), dto.getEnderecoIp(), dto.getPortaHttp(),
                dto.getPortaRtsp(), dto.getUsuario(), senhaCifrada, Boolean.TRUE.equals(dto.getAtiva()));
        return cameraMapper.toResponseDTO(cameraRepository.save(camera));
    }

    @Transactional
    public CameraResponseDTO atualizar(Long id, CameraRequestDTO dto) {
        Camera camera = buscarEntidadeOuFalhar(id);
        String senhaCifrada = (dto.getSenha() != null && !dto.getSenha().isBlank())
                ? credentialCipher.cifrar(dto.getSenha())
                : null;
        camera.atualizarDados(
                dto.getNome(), dto.getDescricao(), dto.getEnderecoIp(), dto.getPortaHttp(),
                dto.getPortaRtsp(), dto.getUsuario(), senhaCifrada, Boolean.TRUE.equals(dto.getAtiva()));
        return cameraMapper.toResponseDTO(camera);
    }

    @Transactional
    public void excluir(Long id) {
        Camera camera = buscarEntidadeOuFalhar(id);
        cameraRepository.delete(camera);
    }

    /**
     * Recupera a senha em texto puro para uso interno pelo monitoramento
     * (ping/RTSP). Nunca deve ser exposta em uma resposta HTTP.
     */
    @Transactional(readOnly = true)
    public String recuperarSenhaTextoPuro(Camera camera) {
        return credentialCipher.decifrar(camera.getSenhaCriptografada());
    }

    @Transactional(readOnly = true)
    public Camera buscarEntidadeOuFalhar(Long id) {
        return cameraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Câmera não encontrada: id=" + id));
    }
}
