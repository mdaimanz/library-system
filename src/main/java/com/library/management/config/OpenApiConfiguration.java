package com.library.management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI libraryManagementOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library Management API")
                        .version("v1")
                        .description("REST API for registering borrowers and managing the library book catalogue."));
    }
}
