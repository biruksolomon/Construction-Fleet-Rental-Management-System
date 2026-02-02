package com.devcast.fleetmanagement.features.driver.dto;

public record DriverSafetyConcern(
        Long driverId,
        String driverName,
        int incidentCount,
        String recentIncident,
        String recommendedAction
) {}
