package com.devcast.fleetmanagement.features.rental.dto;

import com.devcast.fleetmanagement.features.rental.model.RentalContract;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Rental Contract Update Request DTO
 * Used for updating existing rental contracts with optional fields
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalContractUpdateRequest {

    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    private Boolean includeDriver;

    private RentalContract.PricingModel pricingModel;

    @DecimalMin(value = "0.0", inclusive = false, message = "Discount must be greater than 0")
    @DecimalMax(value = "50000.00", message = "Discount must not exceed 50000")
    private BigDecimal discount;

    @DecimalMin(value = "0.0", inclusive = false, message = "Additional charge must be greater than 0")
    @DecimalMax(value = "50000.00", message = "Additional charge must not exceed 50000")
    private BigDecimal additionalCharge;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
