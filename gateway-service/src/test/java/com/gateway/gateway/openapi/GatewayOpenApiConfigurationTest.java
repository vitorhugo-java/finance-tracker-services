package com.gateway.gateway.openapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class GatewayOpenApiConfigurationTest {

    @Autowired
    private OpenAPI openAPI;

    @Test
    void registersKeycloakAuthorizationCodeSecurityScheme() {
        assertThat(openAPI.getComponents()).isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("keycloak-oauth2");
        assertThat(openAPI.getComponents().getSecuritySchemes().get("keycloak-oauth2").getFlows().getAuthorizationCode())
                .isNotNull();
        assertThat(openAPI.getComponents().getSecuritySchemes().get("keycloak-oauth2")
                .getFlows().getAuthorizationCode().getAuthorizationUrl())
                .isEqualTo("http://localhost:8080/realms/finance-tracker-realm/protocol/openid-connect/auth");
        assertThat(openAPI.getComponents().getSecuritySchemes().get("keycloak-oauth2")
                .getFlows().getAuthorizationCode().getTokenUrl())
                .isEqualTo("http://localhost:8080/realms/finance-tracker-realm/protocol/openid-connect/token");
    }
}
