package com.devcast.fleetmanagement.features.vehicle.dto;

import com.devcast.fleetmanagement.features.vehicle.model.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vehicle Filter Criteria DTO
 * Used for filtering vehicles by type, status, fuel type, and GPS capability
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleFilterCriteria {
    private Vehicle.VehicleType type;
    private Vehicle.VehicleStatus status;
    private Vehicle.FuelType fuelType;
    private Boolean hasGps;
    private Boolean hasFuelSensor;
    private String searchTerm;
}
