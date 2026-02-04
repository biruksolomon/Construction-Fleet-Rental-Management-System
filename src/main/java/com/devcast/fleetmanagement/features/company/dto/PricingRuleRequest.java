package com.devcast.fleetmanagement.features.company.dto;

import com.devcast.fleetmanagement.features.company.model.PricingRule;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Pricing Rule Creation/Update Request
 *
 * Handles pricing rule configuration.
 * Does not include auto-managed fields or system-managed boolean flags.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingRuleRequest {

    @NotNull(message = "Pricing applies to (VEHICLE or DRIVER) is required")
    private PricingRule.AppliesToType appliesToType;

    @NotNull(message = "Pricing type (HOURLY, DAILY, WEEKLY, PROJECT) is required")
    private PricingRule.PricingType pricingType;

    @NotNull(message = "Rate is required")
    @DecimalMin(value = "0.01", message = "Rate must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Rate cannot exceed 999999.99")
    @Digits(integer = 6, fraction = 2, message = "Rate must have at most 6 integer digits and 2 decimal places")
    private BigDecimal rate;

    @DecimalMin(value = "0.01", message = "Overtime rate must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Overtime rate cannot exceed 999999.99")
    @Digits(integer = 6, fraction = 2, message = "Overtime rate must have at most 6 integer digits and 2 decimal places")
    private BigDecimal overtimeRate;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a valid ISO 4217 code")
    private String currency;

    // Note: 'active' flag is NOT included - controlled via separate endpoints
    // This ensures consistent state management through dedicated lifecycle endpoints
}
