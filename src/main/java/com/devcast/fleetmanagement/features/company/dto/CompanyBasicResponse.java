package com.devcast.fleetmanagement.features.company.dto;

import com.devcast.fleetmanagement.features.company.model.Company;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Basic Company Information
 *
 * Lightweight response used in lists, references, and nested responses.
 * Includes only essential company information.
 *
 * Use this when you don't need full company details (e.g., in user responses).
 * Use CompanyResponse for detailed company views.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyBasicResponse {

    private Long id;

    private String name;

    private Company.BusinessType businessType;

    private String currency;

    private Company.CompanyStatus status;

    /**
     * Convert from Company entity to basic DTO
     */
    public static CompanyBasicResponse fromEntity(Company company) {
        return CompanyBasicResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .businessType(company.getBusinessType())
                .currency(company.getCurrency())
                .status(company.getStatus())
                .build();
    }

    /**
     * Convert from Company entity list to basic DTO list
     */
    public static java.util.List<CompanyBasicResponse> fromEntities(java.util.List<Company> companies) {
        return companies.stream()
                .map(CompanyBasicResponse::fromEntity)
                .toList();
    }
}
