package com.devcast.fleetmanagement.features.audit.dto;

public record DataChangeTrail(
        Long changeId,
        Long userId,
        String fieldName,
        String oldValue,
        String newValue,
        Long timestamp
) {}


