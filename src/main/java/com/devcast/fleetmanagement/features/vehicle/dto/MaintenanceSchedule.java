package com.devcast.fleetmanagement.features.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceSchedule {
    private Long vehicleId;
    private String serviceType;
    private Long dueMileage;
    private Long dueDate;
    private String status;
}
