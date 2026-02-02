package com.devcast.fleetmanagement.features.audit.dto;

public record DeletedRecord(
        Long recordId,
        String entityType,
        Long entityId,
        Long userId,
        Long deletionDate,
        String reason
) {}
