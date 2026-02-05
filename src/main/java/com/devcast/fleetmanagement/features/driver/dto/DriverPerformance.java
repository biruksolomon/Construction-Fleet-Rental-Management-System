package com.devcast.fleetmanagement.features.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Driver Performance DTO
 * Contains comprehensive performance metrics including safety, efficiency, and punctuality
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverPerformance {
    private Long driverId;
    private Double safetyScore;
    private Double efficiencyScore;
    private Double punctualityScore;
    private Double overallScore;
    private String rating;
}
