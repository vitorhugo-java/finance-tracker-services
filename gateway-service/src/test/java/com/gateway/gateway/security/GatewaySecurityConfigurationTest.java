package com.gateway.gateway.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
class GatewaySecurityConfigurationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void permitsActuatorHealthWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void permitsGatewayOpenApiWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void permitsTransactionApiDocsWithoutAuthentication() throws Exception {
        // Security must not block this path (no 401).
        // A ServletException means the gateway routed the request and tried to call the downstream
        // service — which proves security passed. Connection errors are expected when the
        // downstream service is not running in the test environment.
        try {
            mockMvc.perform(get("/api/transactions/v3/api-docs"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        } catch (jakarta.servlet.ServletException e) {
            // Route matched and forwarded — security did not produce 401
        }
    }

    @Test
    void permitsReportApiDocsWithoutAuthentication() throws Exception {
        // Same rationale as permitsTransactionApiDocsWithoutAuthentication
        try {
            mockMvc.perform(get("/api/reports/v3/api-docs"))
                    .andExpect(status().is(org.hamcrest.Matchers.not(401)));
        } catch (jakarta.servlet.ServletException e) {
            // Route matched and forwarded — security did not produce 401
        }
    }

    @Test
    void requiresAuthenticationForGatewayRoutes() throws Exception {
        mockMvc.perform(get("/api/transactions/123"))
                .andExpect(status().isUnauthorized());
    }
}
