package com.devcast.fleetmanagement.features.user.dto;

public record UserActivity(
        Long userId,
        String userName,
        String email,
        Long lastLogin,
        Long loginCount,
        String status
) {}