package com.devcast.fleetmanagement.features.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Driver Safety Concern DTO
 * Identifies drivers with safety-related concerns requiring attention or remediation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverSafetyConcern {
    private Long driverId;
    private String driverName;
    private int incidentCount;
    private String recentIncident;
    private String recommendedAction;
}
