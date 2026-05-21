package com.gateway.gateway.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class UserIdHeaderEnrichmentFilterTest {

    private final UserIdHeaderEnrichmentFilter filter = new UserIdHeaderEnrichmentFilter();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void copiesUserIdClaimToTrustedHeader() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt("user-123")));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/transactions/1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> capturedHeader = new AtomicReference<>();

        filter.doFilter(request, response, captureHeader(capturedHeader));

        assertThat(capturedHeader.get()).isEqualTo("user-123");
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void copiesSubjectClaimWhenUserIdClaimIsAbsent() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwtWithoutUserId("subject-123")));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/transactions/1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> capturedHeader = new AtomicReference<>();

        filter.doFilter(request, response, captureHeader(capturedHeader));

        assertThat(capturedHeader.get()).isEqualTo("subject-123");
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void rejectsJwtWithoutSupportedUserIdentifierClaims() throws ServletException, IOException {
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwtWithoutSupportedUserIdentifier()));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/transactions/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            throw new IllegalStateException("filter chain should not continue");
        });

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getErrorMessage()).isEqualTo("JWT is missing any supported user identifier claim: user_id, sub");
    }

    private static FilterChain captureHeader(AtomicReference<String> capturedHeader) {
        return (servletRequest, servletResponse) ->
                capturedHeader.set(((jakarta.servlet.http.HttpServletRequest) servletRequest).getHeader(UserIdHeaderEnrichmentFilter.USER_ID_HEADER));
    }

    private static Jwt jwt(String userId) {
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("subject")
                .claim(UserIdHeaderEnrichmentFilter.USER_ID_CLAIM, userId)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }

    private static Jwt jwtWithoutSupportedUserIdentifier() {
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }

    private static Jwt jwtWithoutUserId(String subject) {
        return Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject(subject)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
    }
}
