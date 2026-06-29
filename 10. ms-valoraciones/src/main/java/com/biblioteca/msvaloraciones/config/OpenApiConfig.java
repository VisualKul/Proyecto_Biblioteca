package com.biblioteca.msvaloraciones.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bibliotecaAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Biblioteca - Valoraciones Service")
                        .version("1.0")
                        .description("Gestión de valoraciones del sistema de biblioteca (creación, consulta y administración)")
                        .contact(new Contact()
                                .name("Diego Soto")
                                .email("diego@biblioteca.com")));
    }
}