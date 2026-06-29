package com.biblioteca.ms_multas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI multasOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Biblioteca API - Multas Service")
                        .version("1.0")
                        .description("Microservicio encargado del cálculo, administración y pago de multas generadas por préstamos vencidos.")
                        .contact(new Contact()
                                .name("Diego Soto")
                                .email("diego@biblioteca.com")));
    }
}