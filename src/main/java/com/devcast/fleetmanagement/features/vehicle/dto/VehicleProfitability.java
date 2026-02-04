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
public class VehicleProfitability {
    private Long vehicleId;
    private BigDecimal totalRevenue;
    private BigDecimal totalCost;
    private BigDecimal profit;
    private BigDecimal profitMargin;
}
