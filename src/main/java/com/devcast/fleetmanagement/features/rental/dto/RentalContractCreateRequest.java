package com.devcast.fleetmanagement.features.rental.dto;

import com.devcast.fleetmanagement.features.rental.model.RentalContract;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/**
 * Rental Contract Create Request DTO
 * Used for creating new rental contracts with client and vehicle details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalContractCreateRequest {

    @NotNull(message = "Client ID is required")
    @Positive(message = "Client ID must be a positive number")
    private Long clientId;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @NotNull(message = "Include driver flag is required")
    private Boolean includeDriver;

    @NotNull(message = "Pricing model is required")
    private RentalContract.PricingModel pricingModel;

    @NotEmpty(message = "At least one vehicle must be included")
    private List<RentalVehicleRequest> vehicles;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RentalVehicleRequest {
        @NotNull(message = "Vehicle ID is required")
        @Positive(message = "Vehicle ID must be a positive number")
        private Long vehicleId;

        @DecimalMin(value = "0.0", inclusive = false, message = "Agreed rate must be greater than 0")
        @DecimalMax(value = "50000.00", message = "Agreed rate must not exceed 50000")
        private java.math.BigDecimal agreedRate;

        @Positive(message = "Driver ID must be a positive number")
        private Long driverId;
    }
}
