package com.devcast.fleetmanagement.features.maintenance.dto;

import com.devcast.fleetmanagement.features.maintenance.model.MaintenanceRecord;
import com.devcast.fleetmanagement.features.vehicle.dto.VehicleBasicResponse;
import com.devcast.fleetmanagement.features.vehicle.model.Vehicle;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Maintenance record response")
public class MaintenanceRecordResponse {

    @Schema(description = "Maintenance record ID", example = "1")
    private Long id;

    @Schema(description = "Associated vehicle")
    private VehicleBasicResponse vehicle;

    @Schema(description = "Type of maintenance (SERVICE or REPAIR)", example = "SERVICE")
    private String maintenanceType;

    @Schema(description = "Maintenance cost", example = "150.50")
    private BigDecimal cost;

    @Schema(description = "Date of maintenance")
    private LocalDate maintenanceDate;

    @Schema(description = "Hours until next maintenance is due", example = "500")
    private Integer nextDueHours;

    @Schema(description = "Additional notes")
    private String notes;

    @Schema(description = "Service type", example = "OIL_CHANGE")
    private String serviceType;

    @Schema(description = "Maintenance status", example = "COMPLETED")
    private String status;

    @Schema(description = "Vendor/Mechanic name")
    private String vendorName;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    public static MaintenanceRecordResponse fromEntity(MaintenanceRecord record) {
        return MaintenanceRecordResponse.builder()
                .id(record.getId())
                .vehicle(record.getVehicle() != null ? 
                    VehicleBasicResponse.builder()
                        .id(record.getVehicle().getId())
                        .plateNumber(record.getVehicle().getPlateNumber())
                        .type(Vehicle.VehicleType.valueOf(record.getVehicle().getType().toString()))
                        .status(Vehicle.VehicleStatus.valueOf(record.getVehicle().getStatus().toString()))
                        .build()
                    : null)
                .maintenanceType(record.getMaintenanceType().toString())
                .cost(record.getCost())
                .maintenanceDate(record.getMaintenanceDate())
                .nextDueHours(record.getNextDueHours())
                .notes(record.getNotes())
                .build();
    }
}
