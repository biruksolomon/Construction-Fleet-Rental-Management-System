package com.devcast.fleetmanagement.features.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Confirm Password Reset Request DTO
 * Used to confirm and execute password reset with verification code
 */
public record ConfirmPasswordResetRequest(
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Reset code is required")
        String code,

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String newPassword,

        @NotBlank(message = "Password confirmation is required")
        String confirmPassword
) {}
