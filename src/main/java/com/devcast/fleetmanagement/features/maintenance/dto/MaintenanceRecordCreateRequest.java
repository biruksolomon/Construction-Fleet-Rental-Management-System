package com.devcast.fleetmanagement.features.maintenance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Request to create a new maintenance record")
public class MaintenanceRecordCreateRequest {

    @NotNull(message = "Vehicle ID is required")
    @Schema(description = "Vehicle ID", example = "1")
    private Long vehicleId;

    @NotNull(message = "Maintenance type is required")
    @Schema(description = "Type of maintenance (SERVICE or REPAIR)", example = "SERVICE")
    private String maintenanceType;

    @NotNull(message = "Cost is required")
    @Positive(message = "Cost must be positive")
    @Schema(description = "Maintenance cost", example = "150.50")
    private BigDecimal cost;

    @NotNull(message = "Maintenance date is required")
    @Schema(description = "Date of maintenance", example = "2026-02-20")
    private LocalDate maintenanceDate;

    @NotNull(message = "Next due hours is required")
    @Positive(message = "Next due hours must be positive")
    @Schema(description = "Hours until next maintenance is due", example = "500")
    private Integer nextDueHours;

    @Schema(description = "Additional notes about the maintenance", example = "Oil change and filter replacement")
    private String notes;

    @Schema(description = "Service type", example = "OIL_CHANGE")
    private String serviceType;

    @Schema(description = "Maintenance status", example = "SCHEDULED")
    private String status;

    @Schema(description = "Vendor/Mechanic name", example = "John's Auto Service")
    private String vendorName;
}
