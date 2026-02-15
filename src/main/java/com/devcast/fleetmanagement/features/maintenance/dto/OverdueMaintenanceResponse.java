package com.devcast.fleetmanagement.features.maintenance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Overdue maintenance information")
public class OverdueMaintenanceResponse {

    @Schema(description = "Vehicle ID", example = "1")
    private Long vehicleId;

    @Schema(description = "Vehicle plate number", example = "ABC-1234")
    private String registrationNumber;

    @Schema(description = "Service type", example = "INSPECTION")
    private String serviceType;

    @Schema(description = "Date when it became overdue")
    private LocalDate overdueSince;

    @Schema(description = "Days overdue", example = "30")
    private Integer daysOverdue;

    @Schema(description = "Urgency level", example = "CRITICAL")
    private String urgency;

    @Schema(description = "Last maintenance date")
    private LocalDate lastMaintenanceDate;
}
