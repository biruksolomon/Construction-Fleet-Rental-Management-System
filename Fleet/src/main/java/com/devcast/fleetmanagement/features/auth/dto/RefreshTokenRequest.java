package com.devcast.fleetmanagement.features.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Refresh Token Request DTO
 * Contains refresh token for obtaining new access token
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {}
