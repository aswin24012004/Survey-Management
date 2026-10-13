package com.healthcare.survey.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI surveyOpenAPI() {
        final String securitySchemeName = "BearerAuthentication";
        return new OpenAPI()
                .info(new Info()
                        .title("Healthcare QMS - Dynamic Survey Management API")
                        .description("Enterprise REST API for dynamic healthcare survey creation, publishing, response collection, and analytics.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Healthcare Quality Management System")
                                .email("qms-support@healthcare.com")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Provide JWT access token for admin endpoints.")));
    }
}
