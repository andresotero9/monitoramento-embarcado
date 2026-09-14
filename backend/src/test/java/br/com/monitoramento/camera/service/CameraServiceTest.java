package br.com.monitoramento.camera.service;

import br.com.monitoramento.camera.dto.CameraRequestDTO;
import br.com.monitoramento.camera.dto.CameraResponseDTO;
import br.com.monitoramento.camera.entity.Camera;
import br.com.monitoramento.camera.mapper.CameraMapper;
import br.com.monitoramento.camera.repository.CameraRepository;
import br.com.monitoramento.camera.security.CameraCredentialCipher;
import br.com.monitoramento.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CameraServiceTest {

    @Mock
    private CameraRepository cameraRepository;
    @Mock
    private CameraMapper cameraMapper;
    @Mock
    private CameraCredentialCipher credentialCipher;

    private CameraService cameraService;

    @BeforeEach
    void configurar() {
        cameraService = new CameraService(cameraRepository, cameraMapper, credentialCipher);
    }

    private CameraRequestDTO criarDto(String senha) {
        CameraRequestDTO dto = new CameraRequestDTO();
        dto.setNome("Entrada");
        dto.setEnderecoIp("192.168.0.10");
        dto.setPortaRtsp(554);
        dto.setUsuario("admin");
        dto.setSenha(senha);
        dto.setAtiva(true);
        return dto;
    }

    @Test
    void criar_deveCifrarSenhaAntesDePersistir() {
        when(credentialCipher.cifrar("segredo123")).thenReturn("SENHA_CIFRADA");
        when(cameraRepository.save(any(Camera.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cameraMapper.toResponseDTO(any(Camera.class))).thenReturn(mock(CameraResponseDTO.class));

        cameraService.criar(criarDto("segredo123"));

        ArgumentCaptor<Camera> captor = ArgumentCaptor.forClass(Camera.class);
        verify(cameraRepository).save(captor.capture());
        assertThat(captor.getValue().getSenhaCriptografada()).isEqualTo("SENHA_CIFRADA");
    }

    @Test
    void atualizar_semSenhaInformada_devePreservarSenhaAtual() {
        Camera existente = new Camera("Entrada", null, "192.168.0.10", null, 554,
                "admin", "SENHA_ANTIGA_CIFRADA", true);
        when(cameraRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(cameraMapper.toResponseDTO(existente)).thenReturn(mock(CameraResponseDTO.class));

        cameraService.atualizar(1L, criarDto(null)); // senha não informada

        assertThat(existente.getSenhaCriptografada()).isEqualTo("SENHA_ANTIGA_CIFRADA");
        verify(credentialCipher, never()).cifrar(any());
    }

    @Test
    void atualizar_comNovaSenha_deveCifrarESubstituir() {
        Camera existente = new Camera("Entrada", null, "192.168.0.10", null, 554,
                "admin", "SENHA_ANTIGA_CIFRADA", true);
        when(cameraRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(credentialCipher.cifrar("novaSenha")).thenReturn("SENHA_NOVA_CIFRADA");
        when(cameraMapper.toResponseDTO(existente)).thenReturn(mock(CameraResponseDTO.class));

        cameraService.atualizar(1L, criarDto("novaSenha"));

        assertThat(existente.getSenhaCriptografada()).isEqualTo("SENHA_NOVA_CIFRADA");
    }

    @Test
    void buscarPorId_inexistente_deveLancarResourceNotFoundException() {
        when(cameraRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cameraService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void excluir_deveDelegarParaRepository() {
        Camera existente = new Camera("Entrada", null, "192.168.0.10", null, 554,
                "admin", "SENHA_CIFRADA", true);
        when(cameraRepository.findById(1L)).thenReturn(Optional.of(existente));

        cameraService.excluir(1L);

        verify(cameraRepository).delete(existente);
    }
}
