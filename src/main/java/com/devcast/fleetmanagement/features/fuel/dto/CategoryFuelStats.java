package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CategoryFuelStats {

    private String category;
    private BigDecimal totalConsumption;
    private BigDecimal avgConsumption;
    private BigDecimal totalCost;
}
