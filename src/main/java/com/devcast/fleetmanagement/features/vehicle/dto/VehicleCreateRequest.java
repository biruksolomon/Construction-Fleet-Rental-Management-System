package com.devcast.fleetmanagement.features.vehicle.dto;

import com.devcast.fleetmanagement.features.vehicle.model.Vehicle;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Vehicle Create Request DTO
 *
 * Used for creating new vehicles. All fields are immutable from client perspective.
 * No ID, timestamps, or status are provided by the client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleCreateRequest {

    @NotBlank(message = "Plate number is required")
    @Size(min = 2, max = 50, message = "Plate number must be between 2 and 50 characters")
    private String plateNumber;

    @NotBlank(message = "Asset code is required")
    @Size(min = 2, max = 100, message = "Asset code must be between 2 and 100 characters")
    private String assetCode;

    @NotNull(message = "Vehicle type is required")
    private Vehicle.VehicleType type;

    @NotNull(message = "Fuel type is required")
    private Vehicle.FuelType fuelType;

    @DecimalMin(value = "0.0", inclusive = false, message = "Hourly rate must be greater than 0")
    @DecimalMax(value = "10000.00", message = "Hourly rate must not exceed 10000")
    private BigDecimal hourlyRate;

    @DecimalMin(value = "0.0", inclusive = false, message = "Daily rate must be greater than 0")
    @DecimalMax(value = "50000.00", message = "Daily rate must not exceed 50000")
    private BigDecimal dailyRate;

    @NotNull(message = "GPS capability must be specified")
    private Boolean hasGps;

    @NotNull(message = "Fuel sensor capability must be specified")
    private Boolean hasFuelSensor;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Pattern(regexp = "^[A-Z]{2}$", message = "License plate region must be 2 uppercase letters")
    private String licensePlateRegion;
}
