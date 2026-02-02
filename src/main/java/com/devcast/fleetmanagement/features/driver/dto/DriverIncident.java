package com.devcast.fleetmanagement.features.driver.dto;

public record DriverIncident(
        Long incidentId,
        Long driverId,
        String type,
        String description,
        Long date,
        String severity
) {}