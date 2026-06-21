package com.smartorder.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints — no token required
                        .pathMatchers(
                                "/api/v1/auth/**",
                                "/api/v1/products/**",       // read-only product browsing
                                "/api/v1/search/**",
                                // Cart supports guests; real auth (JWT validation +
                                // identity-header injection) is enforced by the
                                // JwtAuthenticationFilter GlobalFilter, not by this
                                // reactive security layer (which never populates an
                                // Authentication, so authenticated() is unsatisfiable).
                                "/api/v1/cart/**",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()
                        // Everything else must pass JWT filter
                        .anyExchange().authenticated()
                )
                // JWT validation is handled by our custom WebFilter (not OAuth2)
                // so we disable the default bearer token resolver here
                .securityContextRepository(
                        org.springframework.security.web.server.context
                                .NoOpServerSecurityContextRepository.getInstance()
                )
                .build();
    }
}