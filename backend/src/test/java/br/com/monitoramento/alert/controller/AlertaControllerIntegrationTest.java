package br.com.monitoramento.alert.controller;

import br.com.monitoramento.alert.entity.Alerta;
import br.com.monitoramento.alert.entity.NivelAlerta;
import br.com.monitoramento.alert.entity.TipoAlerta;
import br.com.monitoramento.alert.repository.AlertaRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa a listagem e resolução de alertas via HTTP contra um banco H2 real.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlertaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private AlertaRepository alertaRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void configurar() throws Exception {
        alertaRepository.deleteAll();
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
    void listarAbertos_deveRetornarApenasNaoResolvidos() throws Exception {
        Alerta aberto = alertaRepository.save(
                new Alerta(TipoAlerta.DISCO, "GLOBAL", NivelAlerta.WARNING, "Disco alto"));
        Alerta resolvido = new Alerta(TipoAlerta.INTERNET, "GLOBAL", NivelAlerta.CRITICAL, "Internet fora");
        resolvido.resolver();
        alertaRepository.save(resolvido);

        mockMvc.perform(get("/api/alertas/abertos").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(aberto.getId()));
    }

    @Test
    void resolver_alertaAberto_deveMarcarComoResolvido() throws Exception {
        Alerta aberto = alertaRepository.save(
                new Alerta(TipoAlerta.CAMERA, "3", NivelAlerta.CRITICAL, "Câmera offline"));

        mockMvc.perform(put("/api/alertas/" + aberto.getId() + "/resolver")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolvido").value(true));
    }

    @Test
    void resolver_alertaJaResolvido_deveRetornar409() throws Exception {
        Alerta jaResolvido = new Alerta(TipoAlerta.CAMERA, "3", NivelAlerta.CRITICAL, "Câmera offline");
        jaResolvido.resolver();
        alertaRepository.save(jaResolvido);

        mockMvc.perform(put("/api/alertas/" + jaResolvido.getId() + "/resolver")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void resolver_alertaInexistente_deveRetornar404() throws Exception {
        mockMvc.perform(put("/api/alertas/9999/resolver").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
