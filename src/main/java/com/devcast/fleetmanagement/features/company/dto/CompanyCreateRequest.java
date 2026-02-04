package com.devcast.fleetmanagement.features.company.dto;

import com.devcast.fleetmanagement.features.company.model.Company;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Company Creation Request
 *
 * This DTO is used when clients want to create a new company.
 * It does NOT include:
 * - id (auto-generated)
 * - createdAt/updatedAt (server-managed)
 * - status (defaults to ACTIVE)
 *
 * This ensures clients cannot manipulate server-managed fields
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyCreateRequest {

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 255, message = "Company name must be between 2 and 255 characters")
    private String name;

    @NotNull(message = "Business type is required")
    private Company.BusinessType businessType;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters (ISO 4217)")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a valid ISO 4217 code")
    private String currency;

    @NotBlank(message = "Timezone is required")
    @Size(min = 3, max = 50, message = "Timezone must be valid")
    private String timezone;

    @NotBlank(message = "Language is required")
    @Size(min = 2, max = 5, message = "Language code must be valid")
    private String language;

    // Note: status is NOT included - defaults to ACTIVE server-side
    // Note: createdAt and updatedAt are NOT included - set by server
}
