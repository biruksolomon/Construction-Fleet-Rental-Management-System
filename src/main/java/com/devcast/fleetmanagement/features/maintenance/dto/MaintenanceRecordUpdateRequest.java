package com.devcast.fleetmanagement.features.maintenance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update a maintenance record")
public class MaintenanceRecordUpdateRequest {

    @Schema(description = "Type of maintenance (SERVICE or REPAIR)", example = "SERVICE")
    private String maintenanceType;

    @Positive(message = "Cost must be positive")
    @Schema(description = "Updated maintenance cost", example = "175.50")
    private BigDecimal cost;

    @Schema(description = "Date of maintenance")
    private LocalDate maintenanceDate;

    @Positive(message = "Next due hours must be positive")
    @Schema(description = "Updated hours until next maintenance", example = "600")
    private Integer nextDueHours;

    @Schema(description = "Additional notes")
    private String notes;

    @Schema(description = "Service type")
    private String serviceType;

    @Schema(description = "Maintenance status (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED)")
    private String status;

    @Schema(description = "Vendor/Mechanic name")
    private String vendorName;
}
