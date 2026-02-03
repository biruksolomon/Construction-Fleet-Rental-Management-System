package com.devcast.fleetmanagement.features.auth.dto;

/**
 * Request DTO for email verification
 */
public record VerifyEmailRequest(
        String email,
        String code
) {
    public VerifyEmailRequest {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Verification code is required");
        }
    }
}
