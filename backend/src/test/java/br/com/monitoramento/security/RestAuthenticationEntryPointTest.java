package br.com.monitoramento.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa isoladamente que requisições sem autenticação válida recebem 401
 * (não o 403 padrão do Spring Security), no formato de erro padrão da API.
 */
class RestAuthenticationEntryPointTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint(objectMapper);

    @Test
    void commence_deveResponderComStatus401EFormatoPadraoDeErro() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/cameras");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("sem token"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/json");

        JsonNode corpo = objectMapper.readTree(response.getContentAsString());
        assertThat(corpo.get("status").asInt()).isEqualTo(401);
        assertThat(corpo.get("error").asText()).isEqualTo("UNAUTHORIZED");
        assertThat(corpo.get("path").asText()).isEqualTo("/api/cameras");
    }
}
