package com.devcast.fleetmanagement.features.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Work Hours Summary DTO
 * Provides comprehensive work hours analytics for a driver within a time period
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkHoursSummary {
    private Long driverId;
    private Long totalHours;
    private Double averageDaily;
    private Long maxDailyHours;
    private Long minDailyHours;
    private int workDays;
}
