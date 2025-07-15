package com.cybernerd.agriConnect_APIBackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("AgriConnect API")
                .version("1.0.0")
                .description("API REST pour la marketplace agricole AgriConnect. " +
                           "Cette API permet la gestion des produits agricoles, " +
                           "des commandes, des utilisateurs et des notifications.")
                .contact(new Contact()
                    .name("DAGBOH Francis")
                    .email("dagbohfrancis@gmail.com")
                    .url("https://github.com/cybernerd"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("http://www.apache.org/licenses/LICENSE-2.0.txt")))
            .servers(List.of(
                new Server().url("http://localhost:8080/api/v1").description("Serveur de développement"),
                new Server().url("https://api.agriconnect.com/api/v1").description("Serveur de production")
            ))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT token d'authentification")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
} 