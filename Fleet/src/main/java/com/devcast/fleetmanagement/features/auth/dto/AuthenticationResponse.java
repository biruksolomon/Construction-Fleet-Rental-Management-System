package com.devcast.fleetmanagement.features.auth.dto;

public record AuthenticationResponse(
        String accessToken,
        String refreshToken,
        Long expiresIn,
        String tokenType,
        UserInfo userInfo
    ) {}
