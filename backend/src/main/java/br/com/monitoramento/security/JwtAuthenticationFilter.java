package br.com.monitoramento.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Intercepta requisições HTTP, extrai o JWT do header {@code Authorization},
 * valida e, se válido, popula o {@link SecurityContextHolder} para que os
 * endpoints protegidos reconheçam o usuário autenticado.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String PREFIXO_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final RequestMatcher endpointsPublicos;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService,
                                    RequestMatcher endpointsPublicos) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.endpointsPublicos = endpointsPublicos;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return endpointsPublicos.matches(request);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HEADER_AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(PREFIXO_BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(PREFIXO_BEARER.length());

        // Tenta extrair o username do token e autenticar o usuário. Se o token for inválido, expirado ou malformado, apenas segue sem autenticar.
        try {
            String username = jwtService.extrairUsername(token);

            // Se o username for válido e não houver autenticação já presente no contexto, carrega os detalhes do usuário e autentica.
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                //
                if (jwtService.isTokenValido(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token inválido/expirado/malformado: apenas segue sem autenticar.
            // O acesso a endpoints protegidos resultará em 401 pelo Spring Security.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
