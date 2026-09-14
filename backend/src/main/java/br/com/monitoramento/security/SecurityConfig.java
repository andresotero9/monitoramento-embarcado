package br.com.monitoramento.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuração de segurança da aplicação: autenticação stateless via JWT,
 * autorização por endpoint e CORS restrito às origens configuradas.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final List<String> corsAllowedOrigins;

    /**
     * {@code app.cors.allowed-origins} é uma string única, podendo conter múltiplas
     * origens separadas por vírgula (ex.: "http://localhost:5173,https://app.exemplo.com").
     * O split manual evita depender de conversão automática de List via SpEL.
     */
    public SecurityConfig(@Value("${app.cors.allowed-origins}") String corsAllowedOriginsRaw) {
        this.corsAllowedOrigins = Arrays.stream(corsAllowedOriginsRaw.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
    }

    /**
     * Endpoints acessíveis sem autenticação. Usado tanto pelo Spring Security
     * quanto pelo {@link JwtAuthenticationFilter} (via {@code shouldNotFilter}).
     */
    @Bean
    public RequestMatcher endpointsPublicos() {
        return new OrRequestMatcher(
                new AntPathRequestMatcher("/api/auth/login", "POST"),
                new AntPathRequestMatcher("/api/auth/register", "POST"),
                new AntPathRequestMatcher("/actuator/health"),
                new AntPathRequestMatcher("/actuator/health/**"),
                new AntPathRequestMatcher("/swagger-ui.html"),
                new AntPathRequestMatcher("/swagger-ui/**"),
                new AntPathRequestMatcher("/v3/api-docs/**"),
                // Servem arquivos para a tag <video>, que não envia header Authorization;
                // autenticados por token de sessão via query string (ver StreamController).
                new AntPathRequestMatcher("/api/stream/*/playlist.m3u8"),
                new AntPathRequestMatcher("/api/stream/*/*.ts")
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, RequestMatcher endpointsPublicos,
                                                     JwtAuthenticationFilter jwtAuthenticationFilter,
                                                     RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                                                     RestAccessDeniedHandler restAccessDeniedHandler) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // API stateless com JWT não é vulnerável a CSRF baseado em cookies
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(endpointsPublicos).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(corsAllowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService userDetailsService,
                                                              PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}
