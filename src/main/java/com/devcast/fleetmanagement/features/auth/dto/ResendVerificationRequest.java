package com.devcast.fleetmanagement.features.auth.dto;

/**
 * Request DTO for resending verification code
 */
public record ResendVerificationRequest(
        String email
) {
    public ResendVerificationRequest {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
    }
}
