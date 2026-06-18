package com.smartorder.auth.adapters.config;

import com.smartorder.auth.adapters.messaging.AuthEventPublisherAdapter;
import com.smartorder.auth.adapters.persistence.RefreshTokenRedisAdapter;
import com.smartorder.auth.adapters.persistence.UserRepositoryAdapter;
import com.smartorder.auth.adapters.security.BcryptPasswordEncoderAdapter;
import com.smartorder.auth.adapters.security.JwtTokenProviderAdapter;
import com.smartorder.auth.domain.service.*;
import com.smartorder.common.filter.CorrelationIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

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

        // Values serialised as JSON (supports RefreshToken deserialization)
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer();
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
}