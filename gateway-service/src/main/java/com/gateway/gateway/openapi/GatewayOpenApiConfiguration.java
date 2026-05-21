package com.gateway.gateway.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayOpenApiConfiguration {

    private static final String SECURITY_SCHEME_NAME = "keycloak-oauth2";

    @Bean
    OpenAPI gatewayOpenApi(
            @Value("${spring.security.oauth2.client.provider.keycloak.authorization-uri}") String authorizationUrl,
            @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}") String tokenUrl) {
        OAuthFlow authorizationCodeFlow = new OAuthFlow()
                .authorizationUrl(authorizationUrl)
                .tokenUrl(tokenUrl)
                .scopes(new Scopes()
                        .addString("openid", "OpenID Connect scope")
                        .addString("profile", "Access to profile claims")
                        .addString("email", "Access to email claims"));

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .description("Keycloak OAuth2 Authorization Code flow with PKCE for the Finance Tracker gateway.")
                .flows(new OAuthFlows().authorizationCode(authorizationCodeFlow));

        return new OpenAPI()
                .info(new Info()
                        .title("Finance Tracker Gateway API")
                        .version("v1")
                        .description("""
                                Unified OpenAPI entry point for the Finance Tracker platform.

                                This gateway exposes its own API description and aggregates downstream OpenAPI documents
                                from transaction-service and report-service through routed /v3/api-docs endpoints.
                                Swagger UI is configured for OAuth2 Authorization Code with PKCE against Keycloak.
                                """)
                        .contact(new Contact()
                                .name("Finance Tracker Team")))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }
}
