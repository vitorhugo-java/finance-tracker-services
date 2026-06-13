package com.transaction.transactionservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    private static final String SECURITY_SCHEME_NAME = "keycloak-oauth2";

    @Value("${application.gateway-url:http://localhost:8080}")
    private String gatewayUrl;

    @Value("${application.security.keycloak.public-base-url}")
    private String keycloakBaseUrl;

    @Value("${application.security.keycloak.realm}")
    private String keycloakRealm;

    @Bean
    OpenAPI transactionOpenApi() {
        String authorizationUrl = keycloakBaseUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/auth";
        String tokenUrl = keycloakBaseUrl + "/realms/" + keycloakRealm + "/protocol/openid-connect/token";

        OAuthFlow authorizationCodeFlow = new OAuthFlow()
                .authorizationUrl(authorizationUrl)
                .tokenUrl(tokenUrl)
                .scopes(new Scopes()
                        .addString("openid", "OpenID Connect scope")
                        .addString("profile", "Access to profile claims")
                        .addString("email", "Access to email claims"));

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("Keycloak OAuth2 Authorization Code flow with PKCE.")
                .flows(new OAuthFlows().authorizationCode(authorizationCodeFlow));

        return new OpenAPI()
                .addServersItem(new Server().url(gatewayUrl + "/api/transactions").description("Gateway"))
                .info(new Info()
                        .title("Finance Tracker Transaction Service")
                        .version("1.0.0")
                        .description("Manages authenticated user financial transactions. User identity is supplied by the Gateway headers."))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
