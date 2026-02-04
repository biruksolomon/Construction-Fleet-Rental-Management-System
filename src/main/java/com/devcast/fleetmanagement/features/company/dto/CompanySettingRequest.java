package com.devcast.fleetmanagement.features.company.dto;

import com.devcast.fleetmanagement.features.company.model.CompanySetting;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Company Setting Creation/Update Request
 *
 * Separates API contract from entity persistence.
 * Does not include auto-managed fields like id, timestamps.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanySettingRequest {

    @NotBlank(message = "Setting key is required")
    @Size(min = 1, max = 100, message = "Setting key must be between 1 and 100 characters")
    @Pattern(regexp = "^[a-z_]+$", message = "Setting key must contain only lowercase letters and underscores")
    private String key;

    @NotBlank(message = "Setting value is required")
    @Size(min = 1, max = 1000, message = "Setting value must be between 1 and 1000 characters")
    private String value;

    @NotNull(message = "Data type is required")
    private CompanySetting.DataType dataType;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
