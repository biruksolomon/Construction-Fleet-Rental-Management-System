package com.devcast.fleetmanagement.features.company.dto;

import com.devcast.fleetmanagement.features.company.model.CompanySetting;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Company Setting Response
 *
 * Returns setting information to clients.
 * Includes all relevant details for displaying and managing settings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanySettingResponse {

    private Long id;

    private Long companyId;

    private String key;

    private String value;

    private CompanySetting.DataType dataType;

    private String description;

    /**
     * Convert from CompanySetting entity to DTO
     */
    public static CompanySettingResponse fromEntity(CompanySetting setting) {
        return CompanySettingResponse.builder()
                .id(setting.getId())
                .companyId(setting.getCompany().getId())
                .key(setting.getSettingKey())
                .value(setting.getSettingValue())
                .dataType(setting.getDataType())
                .description(setting.getDescription())
                .build();
    }

    /**
     * Convert from CompanySetting entity list to DTO list
     */
    public static java.util.List<CompanySettingResponse> fromEntities(java.util.List<CompanySetting> settings) {
        return settings.stream()
                .map(CompanySettingResponse::fromEntity)
                .toList();
    }
}
