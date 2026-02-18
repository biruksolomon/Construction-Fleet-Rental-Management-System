package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FuelBudgetStatus {

    private Long vehicleId;
    private BigDecimal budget;
    private BigDecimal spent;
    private BigDecimal remaining;
    private BigDecimal percentageUsed;
}
