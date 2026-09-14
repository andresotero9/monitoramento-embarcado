package br.com.monitoramento.security;

import br.com.monitoramento.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Responde 403 no mesmo formato de erro usado pelo restante da API quando um
 * usuário autenticado tenta acessar um recurso sem permissão suficiente (ver
 * {@link RestAuthenticationEntryPoint} para o caso de ausência de autenticação).
 */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {
        ErrorResponse body = new ErrorResponse(HttpStatus.FORBIDDEN.value(), "ACCESS_DENIED",
                "Acesso negado ao recurso solicitado", request.getRequestURI(), null);

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
