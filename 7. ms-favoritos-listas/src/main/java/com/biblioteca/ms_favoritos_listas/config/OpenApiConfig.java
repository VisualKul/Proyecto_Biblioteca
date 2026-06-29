package com.biblioteca.ms_favoritos_listas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI favoritosOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Biblioteca API - Favoritos y Listas Service")
                        .version("1.0")
                        .description("Microservicio encargado de administrar las listas de favoritos creadas por los usuarios.")
                        .contact(new Contact()
                                .name("Diego Soto")
                                .email("diego@biblioteca.com")));
    }
}