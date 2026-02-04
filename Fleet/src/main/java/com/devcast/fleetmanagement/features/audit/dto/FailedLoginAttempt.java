package com.devcast.fleetmanagement.features.audit.dto;

public record FailedLoginAttempt(
        Long attemptId,
        String email,
        Long timestamp,
        String ipAddress,
        String reason
) {}