package com.devcast.fleetmanagement.features.driver.dto;

public record WorkHoursSummary(
        Long driverId,
        Long totalHours,
        Double averageDaily,
        Long maxDailyHours,
        Long minDailyHours,
        int workDays
) {}

