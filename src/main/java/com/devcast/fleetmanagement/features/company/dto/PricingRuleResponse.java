package com.devcast.fleetmanagement.features.company.dto;

import com.devcast.fleetmanagement.features.company.model.PricingRule;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Pricing Rule Response
 *
 * Returns pricing rule details to clients.
 * Includes all information needed for display and management.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PricingRuleResponse {

    private Long id;

    private Long companyId;

    private PricingRule.AppliesToType appliesToType;

    private PricingRule.PricingType pricingType;

    private BigDecimal rate;

    private BigDecimal overtimeRate;

    private String currency;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Convert from PricingRule entity to DTO
     */
    public static PricingRuleResponse fromEntity(PricingRule rule) {
        return PricingRuleResponse.builder()
                .id(rule.getId())
                .companyId(rule.getCompany().getId())
                .appliesToType(rule.getAppliesToType())
                .pricingType(rule.getPricingType())
                .rate(rule.getRate())
                .overtimeRate(rule.getOvertimeRate())
                .currency(rule.getCurrency())
                .active(rule.getActive())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }

    /**
     * Convert from PricingRule entity list to DTO list
     */
    public static java.util.List<PricingRuleResponse> fromEntities(java.util.List<PricingRule> rules) {
        return rules.stream()
                .map(PricingRuleResponse::fromEntity)
                .toList();
    }
}
