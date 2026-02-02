package com.devcast.fleetmanagement.features.audit.dto;

public record UnauthorizedAccess(
        Long accessId,
        Long userId,
        String attemptedResource,
        Long timestamp,
        String ipAddress,
        String permission
) {}
