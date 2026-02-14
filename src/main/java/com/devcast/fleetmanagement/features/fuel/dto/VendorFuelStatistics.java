package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class VendorFuelStatistics {

    private String vendor;
    private int logCount;
    private BigDecimal totalLiters;
    private BigDecimal avgPrice;
    private BigDecimal totalCost;
}
