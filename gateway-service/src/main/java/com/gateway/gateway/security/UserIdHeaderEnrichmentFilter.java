package com.gateway.gateway.security;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

final class UserIdHeaderEnrichmentFilter extends OncePerRequestFilter {

    static final String USER_ID_CLAIM = "user_id";
    static final String SUBJECT_CLAIM = "sub";
    static final List<String> USER_ID_CLAIMS = List.of(USER_ID_CLAIM, SUBJECT_CLAIM);
    static final String USER_ID_HEADER = "X-User-Id";

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/v3/api-docs") || path.matches("/api/[^/]+/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        Object userIdClaim = USER_ID_CLAIMS.stream()
                .map(claimName -> jwtAuthenticationToken.getToken().getClaims().get(claimName))
                .filter(Objects::nonNull)
                .filter(claimValue -> !claimValue.toString().isBlank())
                .findFirst()
                .orElse(null);
        if (Objects.isNull(userIdClaim) || userIdClaim.toString().isBlank()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "JWT is missing any supported user identifier claim: user_id, sub");
            return;
        }

        // The gateway validates the JWT once, then forwards the verified user identity as a trusted header.
        filterChain.doFilter(new UserIdHeaderRequestWrapper(request, userIdClaim.toString()), response);
    }
}
