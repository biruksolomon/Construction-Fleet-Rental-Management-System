package com.devcast.fleetmanagement.features.company.dto;

import com.devcast.fleetmanagement.features.company.model.Company;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Company Response
 *
 * This DTO represents company information returned to clients.
 * Includes all relevant information for displaying company details.
 *
 * Uses @JsonInclude to exclude null values from JSON serialization,
 * keeping responses clean and reducing payload size.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyResponse {

    private Long id;

    private String name;

    private Company.BusinessType businessType;

    private String currency;

    private String timezone;

    private String language;

    private Company.CompanyStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Convert from Company entity to DTO
     */
    public static CompanyResponse fromEntity(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .businessType(company.getBusinessType())
                .currency(company.getCurrency())
                .timezone(company.getTimezone())
                .language(company.getLanguage())
                .status(company.getStatus())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }

    /**
     * Convert from Company entity list to DTO list
     */
    public static java.util.List<CompanyResponse> fromEntities(java.util.List<Company> companies) {
        return companies.stream()
                .map(CompanyResponse::fromEntity)
                .toList();
    }
}
