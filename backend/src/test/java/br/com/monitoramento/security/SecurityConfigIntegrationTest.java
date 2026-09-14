package br.com.monitoramento.security;

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
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confirma a matriz de acesso público/protegido definida em {@link SecurityConfig}:
 * login e registro são públicos, todo o restante exige um JWT válido.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void configurar() {
        usuarioRepository.deleteAll();
        usuarioRepository.save(new Usuario("Admin Teste", "admin.seguranca", passwordEncoder.encode("senha123"), Role.ADMIN));
    }

    @Test
    void register_semJwt_deveSerAcessivelPublicamente() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "nome", "Novo Usuario",
                                "username", "novo." + UUID.randomUUID(),
                                "password", "SenhaSegura123",
                                "confirmPassword", "SenhaSegura123"))))
                .andExpect(status().isCreated());
    }

    @Test
    void login_semJwt_deveSerAcessivelPublicamente() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "admin.seguranca", "password", "senha123"))))
                .andExpect(status().isOk());
    }

    @Test
    void endpointProtegido_semJwt_deveRetornar401() throws Exception {
        mockMvc.perform(get("/api/cameras"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void endpointProtegido_comJwtValido_deveSerAcessivel() throws Exception {
        MvcResult resultadoLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "admin.seguranca", "password", "senha123"))))
                .andExpect(status().isOk())
                .andReturn();

        Map<?, ?> corpo = objectMapper.readValue(resultadoLogin.getResponse().getContentAsString(), Map.class);
        String token = (String) corpo.get("token");

        mockMvc.perform(get("/api/cameras").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
