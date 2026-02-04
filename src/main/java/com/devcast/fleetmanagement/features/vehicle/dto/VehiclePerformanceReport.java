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
public class VehiclePerformanceReport {
    private Long vehicleId;
    private BigDecimal efficiency;
    private BigDecimal reliability;
    private BigDecimal utilization;
    private BigDecimal profitability;
    private String overallRating;
}
