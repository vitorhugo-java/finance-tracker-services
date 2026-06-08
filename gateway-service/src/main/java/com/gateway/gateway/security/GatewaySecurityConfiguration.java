package com.gateway.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class GatewaySecurityConfiguration {

    /**
     * Open chain: no oauth2ResourceServer → no BearerTokenAuthenticationFilter → cannot produce 401.
     * Uses a lambda RequestMatcher on getRequestURI() to bypass PathPatternRequestMatcher issues
     * that occur under real Tomcat in Spring Security 7 (@Order(1)).
     */
    @Bean
    @Order(1)
    SecurityFilterChain openApiFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(request -> {
                    String uri = request.getRequestURI();
                    return uri.startsWith("/swagger-ui")
                            || uri.startsWith("/v3/api-docs")
                            || uri.equals("/api/transactions/v3/api-docs")
                            || uri.equals("/api/reports/v3/api-docs")
                            || uri.startsWith("/actuator")
                            || uri.startsWith("/auth")
                            || uri.equals("/error");
                })
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    /**
     * Main chain: all remaining requests require a valid JWT (@Order(2)).
     */
    @Bean
    @Order(2)
    SecurityFilterChain mainFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
                .addFilterAfter(new UserIdHeaderEnrichmentFilter(), BearerTokenAuthenticationFilter.class)
                .build();
    }
}
