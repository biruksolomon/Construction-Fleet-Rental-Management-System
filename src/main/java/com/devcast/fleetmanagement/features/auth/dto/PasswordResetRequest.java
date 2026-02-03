package com.devcast.fleetmanagement.features.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Password Reset Request DTO
 * Two purposes:
 * 1. Request password reset: Only email field
 * 2. Confirm password reset: email, code, newPassword, confirmPassword fields
 */
public record PasswordResetRequest(
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        String email,

        String code,

        @Size(min = 8, message = "Password must be at least 8 characters")
        String newPassword,

        String confirmPassword
) {}
