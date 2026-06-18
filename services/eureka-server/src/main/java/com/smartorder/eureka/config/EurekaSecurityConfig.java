package com.smartorder.eureka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class EurekaSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        // Eureka peer replication uses PUT/DELETE — must disable CSRF
                        .ignoringRequestMatchers("/eureka/**")
                )
                .authorizeHttpRequests(auth -> auth
                        // Actuator health endpoint is open for load-balancer probes
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Everything else (dashboard + /eureka/**) requires auth
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {});  // Enable HTTP Basic

        return http.build();
    }
}