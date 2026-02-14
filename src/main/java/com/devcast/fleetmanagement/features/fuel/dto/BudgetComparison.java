package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BudgetComparison {

    private Long companyId;
    private BigDecimal totalBudget;
    private BigDecimal totalSpent;
    private BigDecimal variance;
    private String status;
}
