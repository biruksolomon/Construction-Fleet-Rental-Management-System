package com.devcast.fleetmanagement.features.audit.dto;

public record SuspiciousActivity(
        Long activityId,
        Long userId,
        String activityType,
        String description,
        String severity,
        Long timestamp,
        String recommendation
) {}

