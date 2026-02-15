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
@Schema(description = "Fleet maintenance summary")
public class MaintenanceSummaryResponse {

    @Schema(description = "Total maintenance records", example = "45")
    private Integer totalRecords;

    @Schema(description = "Completed maintenance records", example = "40")
    private Integer completedRecords;

    @Schema(description = "Pending maintenance records", example = "5")
    private Integer pendingRecords;

    @Schema(description = "Overdue maintenance records", example = "2")
    private Integer overdueRecords;

    @Schema(description = "Total maintenance cost", example = "5250.75")
    private BigDecimal totalCost;

    @Schema(description = "Average cost per maintenance", example = "116.68")
    private BigDecimal averageCost;

    @Schema(description = "Vehicles needing maintenance", example = "8")
    private Integer vehiclesNeedingMaintenance;

    @Schema(description = "Fleet health status", example = "GOOD")
    private String overallHealth;

    @Schema(description = "Percentage of compliant vehicles", example = "88.9")
    private Double percentCompliant;
}
