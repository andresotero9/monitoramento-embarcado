package br.com.monitoramento.auth.controller;

import br.com.monitoramento.auth.entity.Role;
import br.com.monitoramento.auth.entity.Usuario;
import br.com.monitoramento.auth.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa o fluxo real de login (Controller → Security → Service → Repository → H2).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void criarUsuarioDeTeste() {
        usuarioRepository.deleteAll();
        usuarioRepository.save(new Usuario("Admin Teste", "admin.teste", passwordEncoder.encode("senha123"), Role.ADMIN));
    }

    @Test
    void login_comCredenciaisValidas_deveRetornarToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "admin.teste", "password", "senha123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("admin.teste"));
    }

    @Test
    void login_comSenhaInvalida_deveRetornar401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "admin.teste", "password", "senhaErrada"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_semUsername_deveRetornar400ComErroDeValidacao() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("password", "senha123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void register_comDadosValidos_deveCriarUsuarioComRoleOperadorERetornar201() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nome", "João da Silva",
                                "username", "joao",
                                "password", "SenhaSegura123",
                                "confirmPassword", "SenhaSegura123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.nome").value("João da Silva"))
                .andExpect(jsonPath("$.username").value("joao"))
                .andExpect(jsonPath("$.role").value("OPERADOR"))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        Usuario salvo = usuarioRepository.findByUsername("joao").orElseThrow();
        assertThat(salvo.getPasswordHash()).isNotEqualTo("SenhaSegura123");
        assertThat(passwordEncoder.matches("SenhaSegura123", salvo.getPasswordHash())).isTrue();
        assertThat(salvo.getRole()).isEqualTo(Role.OPERADOR);
        assertThat(salvo.isAtivo()).isTrue();
    }

    @Test
    void register_comUsernameJaCadastrado_deveRetornar409() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nome", "Outro Admin",
                                "username", "admin.teste",
                                "password", "SenhaSegura123",
                                "confirmPassword", "SenhaSegura123"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("USERNAME_ALREADY_EXISTS"));
    }

    @Test
    void register_comSenhasDiferentes_deveRetornar400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nome", "João da Silva",
                                "username", "joao2",
                                "password", "SenhaSegura123",
                                "confirmPassword", "SenhaDiferente123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void register_comSenhaFraca_deveRetornar400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nome", "João da Silva",
                                "username", "joao3",
                                "password", "1234567",
                                "confirmPassword", "1234567"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void register_comCamposObrigatoriosAusentes_deveRetornar400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("username", "joao4"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void register_comUsernameContendoEspacos_deveRetornar400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nome", "João da Silva",
                                "username", "joao com espaco",
                                "password", "SenhaSegura123",
                                "confirmPassword", "SenhaSegura123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
