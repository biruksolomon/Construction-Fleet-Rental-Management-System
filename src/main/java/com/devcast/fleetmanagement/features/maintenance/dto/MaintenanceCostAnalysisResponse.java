package com.devcast.fleetmanagement.features.maintenance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Maintenance cost analysis for a vehicle")
public class MaintenanceCostAnalysisResponse {

    @Schema(description = "Vehicle ID", example = "1")
    private Long vehicleId;

    @Schema(description = "Vehicle plate number", example = "ABC-1234")
    private String vehiclePlateNumber;

    @Schema(description = "Vehicle type", example = "TRUCK")
    private String vehicleType;

    @Schema(description = "Total maintenance cost", example = "2500.50")
    private BigDecimal totalCost;

    @Schema(description = "Average cost per month", example = "250.05")
    private BigDecimal monthlyAverageCost;

    @Schema(description = "Cost per kilometer", example = "0.25")
    private BigDecimal costPerKilometer;

    @Schema(description = "Cost per operating hour", example = "5.50")
    private BigDecimal costPerHour;

    @Schema(description = "Total service count", example = "12")
    private Integer serviceCount;

    @Schema(description = "Service cost breakdown")
    private String costBreakdownByType;

    @Schema(description = "Trend (UP, DOWN, STABLE)", example = "UP")
    private String trend;

    @Schema(description = "Recommendation", example = "MONITOR_CLOSELY")
    private String recommendation;
}
