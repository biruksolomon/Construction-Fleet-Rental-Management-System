package com.devcast.fleetmanagement.features.audit.dto;

public record EntityChangeRecord(
        Long changeId,
        String entityType,
        Long entityId,
        Long userId,
        String action,
        String oldValue,
        String newValue,
        Long timestamp
) {}
