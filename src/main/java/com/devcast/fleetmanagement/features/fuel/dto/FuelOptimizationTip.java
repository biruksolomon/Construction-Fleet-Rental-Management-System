package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FuelOptimizationTip {

    private String category;
    private String tip;
    private BigDecimal potentialSavings;
    private String priority;
}

