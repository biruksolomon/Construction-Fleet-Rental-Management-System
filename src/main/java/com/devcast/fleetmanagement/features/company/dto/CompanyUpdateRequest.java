package com.devcast.fleetmanagement.features.company.dto;

import com.devcast.fleetmanagement.features.company.model.Company;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Company Update Request
 *
 * Allows partial updates of company information.
 * All fields are optional, allowing clients to update only what they need.
 *
 * Does NOT include:
 * - id (immutable)
 * - status (controlled via separate endpoints)
 * - createdAt (immutable)
 * - updatedAt (auto-managed)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyUpdateRequest {

    @Size(min = 2, max = 255, message = "Company name must be between 2 and 255 characters")
    private String name;

    private Company.BusinessType businessType;

    @Size(min = 3, max = 3, message = "Currency code must be 3 characters (ISO 4217)")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be a valid ISO 4217 code")
    private String currency;

    @Size(min = 3, max = 50, message = "Timezone must be valid")
    private String timezone;

    @Size(min = 2, max = 5, message = "Language code must be valid")
    private String language;

    // Note: No status field - status changes use dedicated endpoints
    // Note: No id, timestamps - these are immutable/auto-managed
}
