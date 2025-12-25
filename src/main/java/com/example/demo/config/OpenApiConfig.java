package com.example.demo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI customOpenAPI() {

        return new OpenAPI()
                // ✅ Server URL
                .servers(List.of(
                        new Server().url("https://9107.32procr.amypo.ai/")
                ))

                // ✅ API Info
                .info(new Info()
                        .title("Leave Overlap Team Capacity Analyzer API")
                        .version("1.0.0")
                        .description("API for managing employee leaves and team capacity analysis")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@example.com"))
                )

                // ✅ Enable authentication globally (Authorize button)
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME))

                // ✅ JWT Bearer configuration
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token")
                        )
                );
    }
}
