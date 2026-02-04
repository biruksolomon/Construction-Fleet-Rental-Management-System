package com.devcast.fleetmanagement.features.vehicle.dto;

import com.devcast.fleetmanagement.features.vehicle.model.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Vehicle Response DTO
 *
 * Complete vehicle information returned to clients.
 * Includes all immutable fields and computed properties.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleResponse {

    private Long id;
    private Long companyId;
    private String plateNumber;
    private String assetCode;
    private Vehicle.VehicleType type;
    private Vehicle.FuelType fuelType;
    private BigDecimal hourlyRate;
    private BigDecimal dailyRate;
    private Vehicle.VehicleStatus status;
    private Boolean hasGps;
    private Boolean hasFuelSensor;
    private String description;
    private String licensePlateRegion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * Convert Vehicle entity to VehicleResponse DTO
     */
    public static VehicleResponse fromEntity(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }

        return VehicleResponse.builder()
                .id(vehicle.getId())
                .companyId(vehicle.getCompany().getId())
                .plateNumber(vehicle.getPlateNumber())
                .assetCode(vehicle.getAssetCode())
                .type(vehicle.getType())
                .fuelType(vehicle.getFuelType())
                .hourlyRate(vehicle.getHourlyRate())
                .dailyRate(vehicle.getDailyRate())
                .status(vehicle.getStatus())
                .hasGps(vehicle.getHasGps())
                .hasFuelSensor(vehicle.getHasFuelSensor())
                .description(vehicle.getDescription())
                .createdAt(vehicle.getCreatedAt())
                .updatedAt(vehicle.getUpdatedAt())
                .build();
    }
}
