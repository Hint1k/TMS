package com.demo.tms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.info.Info;

/**
 * {@code SwaggerConfig} configures the Swagger OpenAPI documentation for the application.
 * It sets up the basic API metadata and security configuration for JWT Bearer authentication.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Configures the OpenAPI documentation for the application.
     * <p>
     * This method creates an {@link OpenAPI} instance that includes the API title, version,
     * and security configurations, specifically for Bearer authentication using JWT.
     * </p>
     *
     * @return An {@link OpenAPI} instance with the custom configuration for API documentation.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("API Documentation").version("1.0"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}