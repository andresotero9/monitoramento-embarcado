package br.com.monitoramento.auth.service;

import br.com.monitoramento.auth.dto.UsuarioCreateRequest;
import br.com.monitoramento.auth.dto.UsuarioResponse;
import br.com.monitoramento.auth.entity.Role;
import br.com.monitoramento.auth.entity.Usuario;
import br.com.monitoramento.auth.repository.UsuarioRepository;
import br.com.monitoramento.exception.UsernameJaCadastradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testa as regras de negócio do cadastro de usuários isoladamente (repositório e
 * encoder mockados) — ver {@link br.com.monitoramento.auth.controller.AuthControllerIntegrationTest}
 * para o fluxo completo via HTTP.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioService usuarioService;

    @BeforeEach
    void configurar() {
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    private UsuarioCreateRequest criarRequestValido() {
        UsuarioCreateRequest request = new UsuarioCreateRequest();
        request.setNome("João da Silva");
        request.setUsername("joao");
        request.setPassword("SenhaSegura123");
        request.setConfirmPassword("SenhaSegura123");
        return request;
    }

    @Test
    void cadastrar_comDadosValidos_devePersistirComSenhaCriptografadaERoleOperador() {
        UsuarioCreateRequest request = criarRequestValido();
        when(usuarioRepository.existsByUsername("joao")).thenReturn(false);
        when(passwordEncoder.encode("SenhaSegura123")).thenReturn("hash-bcrypt-simulado");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        UsuarioResponse response = usuarioService.cadastrar(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario persistido = captor.getValue();

        assertThat(persistido.getPasswordHash()).isEqualTo("hash-bcrypt-simulado");
        assertThat(persistido.getPasswordHash()).isNotEqualTo("SenhaSegura123");
        assertThat(persistido.getRole()).isEqualTo(Role.OPERADOR);
        assertThat(persistido.isAtivo()).isTrue();

        assertThat(response.getNome()).isEqualTo("João da Silva");
        assertThat(response.getUsername()).isEqualTo("joao");
        assertThat(response.getRole()).isEqualTo("OPERADOR");
        assertThat(response.isAtivo()).isTrue();
    }

    @Test
    void cadastrar_comUsernameJaExistente_deveLancarExcecaoSemPersistir() {
        UsuarioCreateRequest request = criarRequestValido();
        when(usuarioRepository.existsByUsername("joao")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastrar(request))
                .isInstanceOf(UsernameJaCadastradoException.class);

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cadastrar_comCorridaDeConcorrencia_deveTraduzirViolacaoDeUnicidadeParaUsernameJaCadastrado() {
        UsuarioCreateRequest request = criarRequestValido();
        when(usuarioRepository.existsByUsername("joao")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenThrow(new DataIntegrityViolationException("uk_usuario_username"));

        assertThatThrownBy(() -> usuarioService.cadastrar(request))
                .isInstanceOf(UsernameJaCadastradoException.class);
    }
}
