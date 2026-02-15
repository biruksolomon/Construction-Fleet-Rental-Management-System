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
@Schema(description = "Vendor performance metrics")
public class VendorPerformanceResponse {

    @Schema(description = "Vendor name", example = "John's Auto Service")
    private String vendorName;

    @Schema(description = "Specialization", example = "Heavy Equipment")
    private String specialization;

    @Schema(description = "Quality rating (0-100)", example = "92.5")
    private Double qualityRating;

    @Schema(description = "Timeliness rating (0-100)", example = "88.0")
    private Double timelinessRating;

    @Schema(description = "Cost efficiency rating (0-100)", example = "85.5")
    private Double costEfficiencyRating;

    @Schema(description = "Overall rating (0-100)", example = "88.7")
    private Double overallRating;

    @Schema(description = "Total jobs completed", example = "45")
    private Integer jobsCompleted;

    @Schema(description = "Average cost per job", example = "250.00")
    private BigDecimal averageCost;

    @Schema(description = "Recommendation", example = "RECOMMENDED")
    private String recommendation;
}
