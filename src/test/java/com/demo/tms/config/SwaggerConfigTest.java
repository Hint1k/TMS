package com.demo.tms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import static org.junit.jupiter.api.Assertions.*;

class SwaggerConfigTest {

    @Test
    void testCustomOpenAPI() {
        // Load the context with the SwaggerConfig
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SwaggerConfig.class);

        // Get the OpenAPI bean from the context
        OpenAPI openAPI = context.getBean(OpenAPI.class);

        // Verify the OpenAPI bean is not null
        assertNotNull(openAPI);

        // Verify the title and version
        assertEquals("API Documentation", openAPI.getInfo().getTitle());
        assertEquals("1.0", openAPI.getInfo().getVersion());

        // Verify the security scheme
        assertNotNull(openAPI.getComponents().getSecuritySchemes());
        SecurityScheme securityScheme = openAPI.getComponents().getSecuritySchemes().get("Bearer Authentication");
        assertNotNull(securityScheme);
        assertEquals(SecurityScheme.Type.HTTP, securityScheme.getType());
        assertEquals("bearer", securityScheme.getScheme());
        assertEquals("JWT", securityScheme.getBearerFormat());

        // Verify the security requirement
        assertNotNull(openAPI.getSecurity());
        SecurityRequirement securityRequirement = openAPI.getSecurity().getFirst();
        assertNotNull(securityRequirement);
        assertTrue(securityRequirement.containsKey("Bearer Authentication"));

        context.close();
    }
}