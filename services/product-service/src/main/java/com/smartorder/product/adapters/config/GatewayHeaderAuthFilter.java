package com.smartorder.product.adapters.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Reads the X-Auth-* headers forwarded by the API Gateway
 * and populates the Spring Security context.
 *
 * The Gateway already validated the JWT and decoded the claims
 * into these headers — so we trust them directly here without
 * re-parsing the token.
 */
@Slf4j
@Component
public class GatewayHeaderAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader("X-Auth-User-Id");
        String role   = request.getHeader("X-Auth-Role");

        if (userId != null && !userId.isBlank()
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String grantedRole = (role != null && !role.isBlank())
                    ? "ROLE_" + role
                    : "ROLE_CUSTOMER";

            var authorities = List.of(new SimpleGrantedAuthority(grantedRole));
            var auth = new UsernamePasswordAuthenticationToken(
                    userId, null, authorities
            );
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.debug("Security context set for userId={}, role={}", userId, role);
        }

        filterChain.doFilter(request, response);
    }
}
