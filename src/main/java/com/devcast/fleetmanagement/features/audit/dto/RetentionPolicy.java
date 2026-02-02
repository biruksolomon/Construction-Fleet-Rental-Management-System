package com.devcast.fleetmanagement.features.audit.dto;

public record RetentionPolicy(
        int retentionDays,
        boolean autoArchive,
        boolean autoDelete,
        String archiveLocation,
        String deletionApprovalRequired
) {}
