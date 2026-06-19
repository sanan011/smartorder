package com.smartorder.product.adapters.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            GatewayHeaderAuthFilter gatewayHeaderAuthFilter) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public read endpoints
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/products",
                                "/api/v1/products/**",
                                "/api/v1/products/search",
                                "/api/v1/products/autocomplete",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // All write operations require auth
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        gatewayHeaderAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}