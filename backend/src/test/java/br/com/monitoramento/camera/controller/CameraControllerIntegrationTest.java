package br.com.monitoramento.camera.controller;

import br.com.monitoramento.auth.entity.Role;
import br.com.monitoramento.auth.entity.Usuario;
import br.com.monitoramento.auth.repository.UsuarioRepository;
import br.com.monitoramento.camera.repository.CameraRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testa o CRUD completo de câmeras via HTTP, autenticado com um JWT real emitido
 * pelo próprio fluxo de login — cobre Controller, Security, Service e Repository
 * juntos, contra um banco H2 real (perfil "test").
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CameraControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CameraRepository cameraRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void configurar() throws Exception {
        cameraRepository.deleteAll();
        usuarioRepository.deleteAll();
        usuarioRepository.save(new Usuario("Operador Teste", "operador.teste", passwordEncoder.encode("senha123"), Role.ADMIN));

        MvcResult resultadoLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", "operador.teste", "password", "senha123"))))
                .andExpect(status().isOk())
                .andReturn();

        Map<?, ?> corpo = objectMapper.readValue(resultadoLogin.getResponse().getContentAsString(), Map.class);
        token = (String) corpo.get("token");
    }

    @Test
    void semToken_deveRetornar401() throws Exception {
        mockMvc.perform(get("/api/cameras"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crudCompleto_deveFuncionarDeAPontaAPonta() throws Exception {
        Map<String, Object> novaCamera = Map.of(
                "nome", "Entrada",
                "descricao", "Câmera da entrada principal",
                "enderecoIp", "192.168.0.50",
                "portaRtsp", 554,
                "usuario", "admin",
                "senha", "segredo123",
                "ativa", true
        );

        // Criar
        MvcResult resultadoCriacao = mockMvc.perform(post("/api/cameras")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novaCamera)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Entrada"))
                .andExpect(jsonPath("$.senha").doesNotExist()) // senha nunca é retornada
                .andReturn();

        Map<?, ?> cameraCriada = objectMapper.readValue(
                resultadoCriacao.getResponse().getContentAsString(), Map.class);
        Number id = (Number) cameraCriada.get("id");

        // Buscar por id
        mockMvc.perform(get("/api/cameras/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enderecoIp").value("192.168.0.50"));

        // Listar
        mockMvc.perform(get("/api/cameras").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // Atualizar
        Map<String, Object> atualizacao = Map.of(
                "nome", "Entrada Principal",
                "enderecoIp", "192.168.0.50",
                "portaRtsp", 554,
                "ativa", false
        );
        mockMvc.perform(put("/api/cameras/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizacao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Entrada Principal"))
                .andExpect(jsonPath("$.ativa").value(false));

        // A senha original deve ter sido preservada (cifrada) no banco, mesmo sem reenviá-la.
        assertThat(cameraRepository.findById(id.longValue()).orElseThrow().getSenhaCriptografada())
                .isNotBlank();

        // Excluir
        mockMvc.perform(delete("/api/cameras/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cameras/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void criar_semNomeObrigatorio_deveRetornar400() throws Exception {
        Map<String, Object> cameraInvalida = Map.of("enderecoIp", "192.168.0.50", "portaRtsp", 554, "ativa", true);

        mockMvc.perform(post("/api/cameras")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cameraInvalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
