package com.biblioteca.ms_prestamos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI prestamosServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Biblioteca API - Prestamos Service")
                        .version("1.0")
                        .description("Microservicio encargado de la administración de préstamos de libros.")
                        .contact(new Contact()
                                .name("Diego Soto")
                                .email("diego@biblioteca.com")));
    }
}