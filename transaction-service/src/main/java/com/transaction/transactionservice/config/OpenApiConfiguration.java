package com.transaction.transactionservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI transactionOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Finance Tracker Transaction Service")
                        .version("1.0.0")
                        .description("Manages authenticated user financial transactions. User identity is supplied by the Gateway headers."));
    }
}
