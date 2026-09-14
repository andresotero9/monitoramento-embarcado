package br.com.monitoramento.security;

import br.com.monitoramento.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Substitui o {@code Http403ForbiddenEntryPoint} padrão do Spring Security (que
 * responderia 403 mesmo para requisições sem autenticação nenhuma), respondendo
 * 401 no mesmo formato de erro usado pelo restante da API (ver
 * {@code GlobalExceptionHandler}) — 401 indica ausência/invalidez de credenciais,
 * enquanto 403 ({@link RestAccessDeniedHandler}) fica reservado para usuários
 * autenticados sem permissão suficiente.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        ErrorResponse body = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "UNAUTHORIZED",
                "Autenticação necessária para acessar este recurso", request.getRequestURI(), null);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
