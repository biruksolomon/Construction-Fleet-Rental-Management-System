package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PriceFluctuation {

    private Long logId;
    private BigDecimal pricePerLiter;
    private BigDecimal variance;
    private String status;
}
