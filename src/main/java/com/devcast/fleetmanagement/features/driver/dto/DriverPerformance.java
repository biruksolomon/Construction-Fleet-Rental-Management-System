package com.devcast.fleetmanagement.features.driver.dto;

public record DriverPerformance(
        Long driverId,
        Double safetyScore,
        Double efficiencyScore,
        Double punctualityScore,
        Double overallScore,
        String rating
) {}
