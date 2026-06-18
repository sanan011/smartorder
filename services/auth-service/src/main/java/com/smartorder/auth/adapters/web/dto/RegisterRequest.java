package com.smartorder.auth.adapters.web.dto;

import com.smartorder.auth.domain.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Inbound DTO for POST /api/v1/auth/register
 */
@Getter
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    /**
     * Optional — defaults to CUSTOMER in the use case.
     * ADMIN and SUPPORT roles cannot be self-assigned;
     * that validation is enforced in the controller.
     */
    private Role role;
}