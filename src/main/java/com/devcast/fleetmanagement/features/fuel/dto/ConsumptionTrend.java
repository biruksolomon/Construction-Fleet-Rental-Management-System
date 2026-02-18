package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class ConsumptionTrend {

    private LocalDate date;
    private BigDecimal liters;
    private BigDecimal cost;
    private BigDecimal pricePerLiter;
}
