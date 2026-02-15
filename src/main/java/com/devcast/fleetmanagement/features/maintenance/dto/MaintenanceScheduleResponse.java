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
@Schema(description = "Maintenance schedule information")
public class MaintenanceScheduleResponse {

    @Schema(description = "Vehicle ID", example = "1")
    private Long vehicleId;

    @Schema(description = "Vehicle plate number", example = "ABC-1234")
    private String vehiclePlateNumber;

    @Schema(description = "Service type", example = "OIL_CHANGE")
    private String serviceType;

    @Schema(description = "Next due kilometers", example = "50000")
    private Long nextDueKilometers;

    @Schema(description = "Next due date")
    private LocalDate nextDueDate;

    @Schema(description = "Current status", example = "DUE_SOON")
    private String status;

    @Schema(description = "Days until due", example = "15")
    private Integer daysUntilDue;

    @Schema(description = "Priority level", example = "HIGH")
    private String priority;
}
