package com.smartorder.auth.adapters.config;

import com.smartorder.auth.adapters.messaging.AuthEventPublisherAdapter;
import com.smartorder.auth.adapters.persistence.RefreshTokenRedisAdapter;
import com.smartorder.auth.adapters.persistence.UserRepositoryAdapter;
import com.smartorder.auth.adapters.security.BcryptPasswordEncoderAdapter;
import com.smartorder.auth.adapters.security.JwtTokenProviderAdapter;
import com.smartorder.auth.domain.service.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartorder.auth.domain.model.RefreshToken;
import com.smartorder.common.filter.CorrelationIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Instant;
import java.util.UUID;

/**
 * Main Spring configuration for the Auth Service.
 *
 * This is the Composition Root — the ONLY place where we wire
 * domain services (use cases) to their adapter implementations.
 * The domain services are instantiated here as Spring beans,
 * receiving their port dependencies via constructor injection.
 *
 * This keeps the domain layer free of @Service/@Component annotations.
 */
@Configuration
public class AuthServiceConfig {

    // ── Use-Case Beans ────────────────────────────────────────

    @Bean
    public RegisterUserService registerUserService(
            UserRepositoryAdapter    userRepository,
            BcryptPasswordEncoderAdapter passwordEncoder,
            AuthEventPublisherAdapter    eventPublisher) {
        return new RegisterUserService(userRepository, passwordEncoder, eventPublisher);
    }

    @Bean
    public LoginService loginService(
            UserRepositoryAdapter        userRepository,
            BcryptPasswordEncoderAdapter passwordEncoder,
            JwtTokenProviderAdapter      tokenProvider,
            RefreshTokenRedisAdapter     refreshTokenRepository,
            AuthEventPublisherAdapter    eventPublisher) {
        return new LoginService(
                userRepository, passwordEncoder,
                tokenProvider, refreshTokenRepository, eventPublisher
        );
    }

    @Bean
    public RefreshTokenService refreshTokenService(
            RefreshTokenRedisAdapter refreshTokenRepository,
            UserRepositoryAdapter    userRepository,
            JwtTokenProviderAdapter  tokenProvider) {
        return new RefreshTokenService(
                refreshTokenRepository, userRepository, tokenProvider
        );
    }

    @Bean
    public LogoutService logoutService(
            RefreshTokenRedisAdapter refreshTokenRepository) {
        return new LogoutService(refreshTokenRepository);
    }

    @Bean
    public ChangePasswordService changePasswordService(
            UserRepositoryAdapter        userRepository,
            BcryptPasswordEncoderAdapter passwordEncoder,
            RefreshTokenRedisAdapter     refreshTokenRepository,
            AuthEventPublisherAdapter    eventPublisher) {
        return new ChangePasswordService(
                userRepository, passwordEncoder,
                refreshTokenRepository, eventPublisher
        );
    }

    // ── Redis Template ────────────────────────────────────────

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Keys are plain strings
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Values serialised as JSON (supports RefreshToken deserialization).
        // A plain GenericJackson2JsonRedisSerializer uses an ObjectMapper without
        // the JavaTimeModule, so it fails to serialize java.time.Instant fields
        // (RefreshToken#issuedAt / #expiresAt) with a 500. We supply our own
        // ObjectMapper that registers JavaTimeModule, and we replicate the default
        // polymorphic typing the no-arg serializer enables so the @class type hint
        // is still written and values deserialize back into RefreshToken.
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        // RefreshToken is immutable (no default constructor), and the domain layer
        // is kept free of Jackson annotations — so a mix-in tells Jackson to rebuild
        // it via its all-args constructor when reading back from Redis. Derived
        // getters (expired/valid) are written on serialization but have no
        // constructor parameter, so ignore unknown properties on read.
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.addMixIn(RefreshToken.class, RefreshTokenMixin.class);

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    // ── Correlation ID Filter ─────────────────────────────────

    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter() {
        FilterRegistrationBean<CorrelationIdFilter> registration =
                new FilterRegistrationBean<>();
        registration.setFilter(new CorrelationIdFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        registration.setName("correlationIdFilter");
        return registration;
    }

    // ── Jackson Mix-ins ───────────────────────────────────────

    /**
     * Tells the Redis ObjectMapper how to reconstruct the immutable
     * {@link RefreshToken} via its all-args reconstitution constructor, without
     * polluting the domain model with Jackson annotations.
     */
    abstract static class RefreshTokenMixin {
        @JsonCreator
        RefreshTokenMixin(
                @JsonProperty("tokenValue") String tokenValue,
                @JsonProperty("userId")     UUID userId,
                @JsonProperty("issuedAt")   Instant issuedAt,
                @JsonProperty("expiresAt")  Instant expiresAt,
                @JsonProperty("revoked")    boolean revoked) {
        }
    }
}