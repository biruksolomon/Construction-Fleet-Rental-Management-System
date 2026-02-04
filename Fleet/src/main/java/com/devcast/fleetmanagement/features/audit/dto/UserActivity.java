package com.devcast.fleetmanagement.features.audit.dto;

public record UserActivity(
        Long userId,
        String userName,
        String action,
        String entityType,
        Long entityId,
        Long timestamp,
        String ipAddress,
        String userAgent
) {}