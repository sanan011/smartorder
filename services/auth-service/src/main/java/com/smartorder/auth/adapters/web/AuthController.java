package com.smartorder.auth.adapters.web;

import com.smartorder.auth.adapters.web.dto.*;
import com.smartorder.auth.domain.model.Role;
import com.smartorder.auth.ports.inbound.*;
import com.smartorder.common.exception.ErrorCode;
import com.smartorder.common.exception.SmartOrderException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST adapter for auth endpoints.
 *
 * All routes are prefixed /api/v1/auth and routed here
 * through the Spring Cloud Gateway.
 *
 * Public  : POST /register, /login, /refresh
 * Protected: POST /logout, /change-password, GET /me
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserUseCase  registerUserUseCase;
    private final LoginUseCase         loginUseCase;
    private final RefreshTokenUseCase  refreshTokenUseCase;
    private final LogoutUseCase        logoutUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;

    // ── POST /register ────────────────────────────────────────

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        // Guard: prevent self-assigning privileged roles
        if (request.getRole() == Role.ADMIN || request.getRole() == Role.SUPPORT) {
            throw new SmartOrderException(
                    ErrorCode.OPERATION_NOT_PERMITTED,
                    "Cannot self-assign ADMIN or SUPPORT roles."
            );
        }

        UUID userId = registerUserUseCase.execute(
                new RegisterUserUseCase.Command(
                        request.getEmail(),
                        request.getPassword(),
                        request.getFirstName(),
                        request.getLastName(),
                        request.getRole()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                RegisterResponse.builder()
                        .userId(userId.toString())
                        .email(request.getEmail())
                        .message("Registration successful. Please verify your email.")
                        .build()
        );
    }

    // ── POST /login ───────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = resolveClientIp(httpRequest);

        LoginUseCase.Result result = loginUseCase.execute(
                new LoginUseCase.Command(
                        request.getEmail(),
                        request.getPassword(),
                        ipAddress
                )
        );

        return ResponseEntity.ok(
                AuthResponse.builder()
                        .accessToken(result.accessToken())
                        .refreshToken(result.refreshToken())
                        .accessTokenExpiresInMs(result.accessTokenExpiresInMs())
                        .userId(result.userId())
                        .email(result.email())
                        .fullName(result.fullName())
                        .role(result.role())
                        .build()
        );
    }

    // ── POST /refresh ─────────────────────────────────────────

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        RefreshTokenUseCase.Result result = refreshTokenUseCase.execute(
                new RefreshTokenUseCase.Command(request.getRefreshToken())
        );

        return ResponseEntity.ok(
                AuthResponse.builder()
                        .accessToken(result.accessToken())
                        .refreshToken(result.refreshToken())
                        .accessTokenExpiresInMs(result.accessTokenExpiresInMs())
                        .build()
        );
    }

    // ── POST /logout ──────────────────────────────────────────

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequest request) {

        logoutUseCase.execute(
                new LogoutUseCase.Command(
                        request.getRefreshToken(),
                        request.isLogoutAllDevices()
                )
        );

        return ResponseEntity.noContent().build();
    }

    // ── POST /change-password ─────────────────────────────────

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal String userId) {

        if (userId == null) {
            throw new SmartOrderException(ErrorCode.ACCESS_DENIED);
        }

        changePasswordUseCase.execute(
                new ChangePasswordUseCase.Command(
                        UUID.fromString(userId),
                        request.getCurrentPassword(),
                        request.getNewPassword()
                )
        );

        return ResponseEntity.noContent().build();
    }

    // ── GET /me ───────────────────────────────────────────────

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(
            HttpServletRequest request) {

        String userId   = request.getHeader("X-Auth-User-Id");
        String username = request.getHeader("X-Auth-Username");
        String role     = request.getHeader("X-Auth-Role");

        if (userId == null) {
            throw new SmartOrderException(ErrorCode.ACCESS_DENIED);
        }

        return ResponseEntity.ok(
                UserProfileResponse.builder()
                        .userId(userId)
                        .email(username)
                        .role(role)
                        .build()
        );
    }

    // ── Helpers ───────────────────────────────────────────────

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp;
        }
        return request.getRemoteAddr();
    }
}