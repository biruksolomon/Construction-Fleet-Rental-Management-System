package com.devcast.fleetmanagement.features.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleComparison {
    private Long vehicleId;
    private String plateNumber;
    private BigDecimal revenue;
    private BigDecimal cost;
    private BigDecimal profit;
    private Double profitMargin;
    private Long usageHours;
}
