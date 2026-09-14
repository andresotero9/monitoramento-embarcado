package br.com.monitoramento.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa isoladamente que um usuário autenticado sem permissão suficiente
 * recebe 403, no formato de erro padrão da API.
 */
class RestAccessDeniedHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private final RestAccessDeniedHandler accessDeniedHandler = new RestAccessDeniedHandler(objectMapper);

    @Test
    void handle_deveResponderComStatus403EFormatoPadraoDeErro() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/cameras/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        accessDeniedHandler.handle(request, response, new AccessDeniedException("sem permissão"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith("application/json");

        JsonNode corpo = objectMapper.readTree(response.getContentAsString());
        assertThat(corpo.get("status").asInt()).isEqualTo(403);
        assertThat(corpo.get("error").asText()).isEqualTo("ACCESS_DENIED");
        assertThat(corpo.get("path").asText()).isEqualTo("/api/cameras/1");
    }
}
