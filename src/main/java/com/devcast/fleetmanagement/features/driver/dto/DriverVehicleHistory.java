package com.devcast.fleetmanagement.features.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Driver Vehicle History DTO
 * Maintains history of vehicle assignments to drivers
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverVehicleHistory {
    private Long driverId;
    private String driverName;
    private Long vehicleId;
    private String registrationNumber;
    private Long assignmentDate;
    private Long endDate;
    private String status;
}
