package com.devcast.fleetmanagement.features.fuel.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FuelAnomaly {

    private Long logId;
    private String anomalyType;
    private BigDecimal expectedConsumption;
    private BigDecimal actualConsumption;
    private String severity;
}
