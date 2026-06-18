package com.smartorder.auth.adapters.security;

import com.smartorder.auth.domain.model.User;
import com.smartorder.auth.ports.outbound.TokenProviderPort;
import com.smartorder.common.exception.ErrorCode;
import com.smartorder.common.exception.SmartOrderException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * JWT adapter implementing {@link TokenProviderPort}.
 * Uses JJWT 0.12.x API with HMAC-SHA256 signing.
 */
@Slf4j
@Component
public class JwtTokenProviderAdapter implements TokenProviderPort {

    private final SecretKey secretKey;
    private final long      accessTokenExpiryMs;

    public JwtTokenProviderAdapter(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry-ms}") long accessTokenExpiryMs) {
        this.secretKey           = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMs = accessTokenExpiryMs;
    }

    @Override
    public String generateAccessToken(User user) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiryMs);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getEmail())
                .claim("role",     user.getRole().name())
                .claim("fullName", user.fullName())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public UUID validateAccessToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return UUID.fromString(claims.getSubject());
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            throw new SmartOrderException(ErrorCode.TOKEN_EXPIRED);
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException e) {
            log.warn("JWT token invalid: {}", e.getMessage());
            throw new SmartOrderException(ErrorCode.TOKEN_INVALID);
        }
    }

    @Override
    public UUID extractUserIdUnchecked(String token) {
        try {
            Claims claims = parseClaims(token);
            return UUID.fromString(claims.getSubject());
        } catch (ExpiredJwtException e) {
            // Expired but we can still read the subject
            return UUID.fromString(e.getClaims().getSubject());
        } catch (Exception e) {
            throw new SmartOrderException(ErrorCode.TOKEN_INVALID, e.getMessage());
        }
    }

    @Override
    public long getAccessTokenExpiryMs() {
        return accessTokenExpiryMs;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}