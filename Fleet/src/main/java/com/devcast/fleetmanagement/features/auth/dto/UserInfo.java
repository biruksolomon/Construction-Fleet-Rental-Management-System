package com.devcast.fleetmanagement.features.auth.dto;

public record UserInfo(
        Long userId,
        String email,
        String fullName,
        String role,
        Long companyId
) {}
