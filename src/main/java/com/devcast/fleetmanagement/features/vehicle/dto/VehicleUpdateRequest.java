package com.devcast.fleetmanagement.features.vehicle.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Vehicle Update Request DTO
 *
 * Used for updating existing vehicles. All fields are optional to support partial updates.
 * No ID, timestamps, or critical fields can be modified through this DTO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleUpdateRequest {

    @Size(min = 2, max = 50, message = "Plate number must be between 2 and 50 characters")
    private String plateNumber;

    @Size(min = 2, max = 100, message = "Asset code must be between 2 and 100 characters")
    private String assetCode;

    @DecimalMin(value = "0.0", inclusive = false, message = "Hourly rate must be greater than 0")
    @DecimalMax(value = "10000.00", message = "Hourly rate must not exceed 10000")
    private BigDecimal hourlyRate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Daily rate must be greater than 0")
    @DecimalMax(value = "50000.00", message = "Daily rate must not exceed 50000")
    private BigDecimal dailyRate;

    private Boolean hasGps;

    private Boolean hasFuelSensor;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Pattern(regexp = "^[A-Z]{2}$", message = "License plate region must be 2 uppercase letters")
    private String licensePlateRegion;
}
