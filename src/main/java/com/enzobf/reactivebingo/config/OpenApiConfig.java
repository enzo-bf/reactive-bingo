package com.enzobf.reactivebingo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reactiveBingoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reactive Bingo API")
                        .description("API reativa de Bingo com Spring WebFlux, Project Reactor e MongoDB.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Enzo")
                                .email("enzo@example.com")));
    }
}
