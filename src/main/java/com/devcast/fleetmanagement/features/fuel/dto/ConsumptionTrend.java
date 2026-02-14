package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ConsumptionTrend {

    private Long date;
    private BigDecimal consumption;
    private BigDecimal cost;
    private Long mileage;
}
