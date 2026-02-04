package com.devcast.fleetmanagement.features.vehicle.dto;

import com.devcast.fleetmanagement.features.vehicle.model.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Vehicle Basic Response DTO
 *
 * Minimal vehicle information for list views and embedded responses.
 * Contains only essential fields to reduce payload size.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleBasicResponse {

    private Long id;
    private String plateNumber;
    private String assetCode;
    private Vehicle.VehicleType type;
    private Vehicle.VehicleStatus status;
    private BigDecimal dailyRate;
    private Boolean hasGps;

    /**
     * Convert Vehicle entity to VehicleBasicResponse DTO
     */
    public static VehicleBasicResponse fromEntity(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        return VehicleBasicResponse.builder()
                .id(vehicle.getId())
                .plateNumber(vehicle.getPlateNumber())
                .assetCode(vehicle.getAssetCode())
                .type(vehicle.getType())
                .status(vehicle.getStatus())
                .dailyRate(vehicle.getDailyRate())
                .hasGps(vehicle.getHasGps())
                .build();
    }
}
