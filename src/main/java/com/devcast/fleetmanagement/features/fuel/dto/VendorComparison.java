package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class VendorComparison {

    private String vendor;
    private BigDecimal avgPrice;
    private int transactionCount;
    private Double quality;
    private String recommendation;
}
