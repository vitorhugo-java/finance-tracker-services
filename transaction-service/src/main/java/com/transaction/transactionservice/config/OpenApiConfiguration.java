package com.transaction.transactionservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Value("${application.gateway-url:http://localhost:8080}")
    private String gatewayUrl;

    @Bean
    OpenAPI transactionOpenApi() {
        return new OpenAPI()
                .addServersItem(new Server().url(gatewayUrl + "/api/transactions").description("Gateway"))
                .info(new Info()
                        .title("Finance Tracker Transaction Service")
                        .version("1.0.0")
                        .description("Manages authenticated user financial transactions. User identity is supplied by the Gateway headers."));
    }
}
