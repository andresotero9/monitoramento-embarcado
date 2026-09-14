package br.com.monitoramento.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura a documentação OpenAPI/Swagger, incluindo o esquema de autenticação
 * Bearer JWT usado por todos os endpoints protegidos.
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_BEARER = "bearer-jwt";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Monitoramento de Equipamento Embarcado")
                        .description("Monitoramento de Internet, Disco e Câmeras IP de um equipamento embarcado Linux")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_BEARER))
                .components(new Components()
                        .addSecuritySchemes(ESQUEMA_BEARER, new SecurityScheme()
                                .name(ESQUEMA_BEARER)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
