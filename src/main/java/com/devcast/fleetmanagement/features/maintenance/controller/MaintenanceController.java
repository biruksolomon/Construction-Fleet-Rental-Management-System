package com.devcast.fleetmanagement.features.maintenance.controller;

import com.devcast.fleetmanagement.features.auth.dto.ApiResponse;
import com.devcast.fleetmanagement.features.maintenance.dto.*;
import com.devcast.fleetmanagement.features.maintenance.model.MaintenanceRecord;
import com.devcast.fleetmanagement.features.maintenance.service.MaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Maintenance Management REST Controller
 * Handles vehicle maintenance scheduling, tracking, and cost analysis
 *
 * Base Path: /maintenance (context path /api is already set)
 * Multi-tenant: Enforced at service level
 */
@RestController
@RequestMapping("/maintenance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Maintenance", description = "Vehicle maintenance scheduling, tracking, and analytics endpoints")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    // ==================== Maintenance Record CRUD Operations ====================

    /**
     * Create a new maintenance record
     * POST /api/maintenance
     */
    @PostMapping
    @Operation(summary = "Create maintenance record", description = "Create a new maintenance record for a vehicle")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Maintenance record created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> createMaintenanceRecord(
            @Valid @RequestBody MaintenanceRecordCreateRequest request
    ) {
        try {
            log.info("Creating maintenance record for vehicle: {}", request.getVehicleId());

            MaintenanceRecord record = MaintenanceRecord.builder()
                    .maintenanceType(MaintenanceRecord.MaintenanceType.valueOf(request.getMaintenanceType().toUpperCase()))
                    .cost(request.getCost())
                    .maintenanceDate(request.getMaintenanceDate())
                    .nextDueHours(request.getNextDueHours())
                    .notes(request.getNotes())
                    .serviceType(request.getServiceType())
                    .vendorName(request.getVendorName())
                    .build();

            MaintenanceRecord created = maintenanceService.createMaintenanceRecord(request.getVehicleId(), record);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(MaintenanceRecordResponse.fromEntity(created), "Maintenance record created successfully"));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid input for maintenance record creation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid input: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating maintenance record", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create maintenance record: " + e.getMessage()));
        }
    }

    /**
     * Get maintenance record by ID
     * GET /api/maintenance/{recordId}
     */
    @GetMapping("/{recordId}")
    @Operation(summary = "Get maintenance record", description = "Retrieve maintenance record details by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Maintenance record found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Maintenance record not found")
    })
    public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> getMaintenanceRecord(
            @PathVariable Long recordId
    ) {
        try {
            log.info("Retrieving maintenance record: {}", recordId);

            return maintenanceService.getMaintenanceRecord(recordId)
                    .map(record -> ResponseEntity.ok(ApiResponse.success(MaintenanceRecordResponse.fromEntity(record), "Maintenance record retrieved successfully")))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving maintenance record", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve maintenance record: " + e.getMessage()));
        }
    }

    /**
     * Get maintenance records for a vehicle
     * GET /api/maintenance/vehicle/{vehicleId}
     */
    @GetMapping("/vehicle/{vehicleId}")
    @Operation(summary = "Get vehicle maintenance records", description = "Retrieve all maintenance records for a specific vehicle")
    public ResponseEntity<ApiResponse<Page<MaintenanceRecordResponse>>> getVehicleMaintenanceRecords(
            @PathVariable Long vehicleId,
            Pageable pageable
    ) {
        try {
            log.info("Retrieving maintenance records for vehicle: {}", vehicleId);

            Page<MaintenanceRecord> records = maintenanceService.getMaintenanceRecords(vehicleId, pageable);
            Page<MaintenanceRecordResponse> responses = records.map(MaintenanceRecordResponse::fromEntity);

            return ResponseEntity.ok(ApiResponse.success(responses, "Maintenance records retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving vehicle maintenance records", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve maintenance records: " + e.getMessage()));
        }
    }

    /**
     * Update maintenance record
     * PUT /api/maintenance/{recordId}
     */
    @PutMapping("/{recordId}")
    @Operation(summary = "Update maintenance record", description = "Update an existing maintenance record")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Maintenance record updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Maintenance record not found")
    })
    public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> updateMaintenanceRecord(
            @PathVariable Long recordId,
            @Valid @RequestBody MaintenanceRecordUpdateRequest request
    ) {
        try {
            log.info("Updating maintenance record: {}", recordId);

            MaintenanceRecord updateData = MaintenanceRecord.builder()
                    .maintenanceType(request.getMaintenanceType() != null ? 
                        MaintenanceRecord.MaintenanceType.valueOf(request.getMaintenanceType().toUpperCase()) : null)
                    .cost(request.getCost())
                    .maintenanceDate(request.getMaintenanceDate())
                    .nextDueHours(request.getNextDueHours())
                    .notes(request.getNotes())
                    .serviceType(request.getServiceType())
                    .vendorName(request.getVendorName())
                    .status(request.getStatus() != null ? 
                        MaintenanceRecord.MaintenanceStatus.valueOf(request.getStatus().toUpperCase()) : null)
                    .build();

            MaintenanceRecord updated = maintenanceService.updateMaintenanceRecord(recordId, updateData);
            return ResponseEntity.ok(ApiResponse.success(MaintenanceRecordResponse.fromEntity(updated), "Maintenance record updated successfully"));
        } catch (IllegalArgumentException e) {
            log.warn("Maintenance record not found: {}", recordId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating maintenance record", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update maintenance record: " + e.getMessage()));
        }
    }

    /**
     * Complete maintenance record
     * POST /api/maintenance/{recordId}/complete
     */
    @PostMapping("/{recordId}/complete")
    @Operation(summary = "Complete maintenance", description = "Mark a maintenance record as completed")
    public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> completeMaintenanceRecord(
            @PathVariable Long recordId,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) java.math.BigDecimal actualCost
    ) {
        try {
            log.info("Completing maintenance record: {}", recordId);

            MaintenanceRecord completed = maintenanceService.completeMaintenanceRecord(recordId, notes, actualCost);
            return ResponseEntity.ok(ApiResponse.success(MaintenanceRecordResponse.fromEntity(completed), "Maintenance record completed successfully"));
        } catch (IllegalArgumentException e) {
            log.warn("Maintenance record not found: {}", recordId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error completing maintenance record", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to complete maintenance record: " + e.getMessage()));
        }
    }

    /**
     * Cancel maintenance record
     * POST /api/maintenance/{recordId}/cancel
     */
    @PostMapping("/{recordId}/cancel")
    @Operation(summary = "Cancel maintenance", description = "Cancel a maintenance record")
    public ResponseEntity<ApiResponse<String>> cancelMaintenanceRecord(
            @PathVariable Long recordId,
            @RequestParam String reason
    ) {
        try {
            log.info("Cancelling maintenance record: {}", recordId);

            maintenanceService.cancelMaintenanceRecord(recordId, reason);
            return ResponseEntity.ok(ApiResponse.success("Maintenance record cancelled successfully"));
        } catch (IllegalArgumentException e) {
            log.warn("Maintenance record not found: {}", recordId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error cancelling maintenance record", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to cancel maintenance record: " + e.getMessage()));
        }
    }

    // ==================== Maintenance Scheduling & Status ====================

    /**
     * Get pending maintenance
     * GET /api/maintenance/company/{companyId}/pending
     */
    @GetMapping("/company/{companyId}/pending")
    @Operation(summary = "Get pending maintenance", description = "Retrieve all pending maintenance for a company")
    public ResponseEntity<ApiResponse<List<MaintenanceRecordResponse>>> getPendingMaintenance(
            @PathVariable Long companyId
    ) {
        try {
            log.info("Retrieving pending maintenance for company: {}", companyId);

            List<MaintenanceRecord> records = maintenanceService.getPendingMaintenance(companyId);
            List<MaintenanceRecordResponse> responses = records.stream()
                    .map(MaintenanceRecordResponse::fromEntity)
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(responses, "Pending maintenance retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving pending maintenance", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve pending maintenance: " + e.getMessage()));
        }
    }

    /**
     * Get overdue maintenance
     * GET /api/maintenance/company/{companyId}/overdue
     */
    @GetMapping("/company/{companyId}/overdue")
    @Operation(summary = "Get overdue maintenance", description = "Retrieve all overdue maintenance for a company")
    public ResponseEntity<ApiResponse<List<OverdueMaintenanceResponse>>> getOverdueMaintenanceItems(
            @PathVariable Long companyId
    ) {
        try {
            log.info("Retrieving overdue maintenance for company: {}", companyId);

            List<MaintenanceService.OverdueMaintenanceItem> items = maintenanceService.getOverdueMaintenanceItems(companyId);
            List<OverdueMaintenanceResponse> responses = items.stream()
                    .map(item -> OverdueMaintenanceResponse.builder()
                            .vehicleId(item.vehicleId())
                            .registrationNumber(item.registrationNumber())
                            .serviceType(item.serviceType())
                            .overdueSince(java.time.LocalDate.ofEpochDay(item.overdueSince()))
                            .urgency(item.urgency())
                            .build())
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(responses, "Overdue maintenance retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving overdue maintenance", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve overdue maintenance: " + e.getMessage()));
        }
    }

    /**
     * Get upcoming maintenance
     * GET /api/maintenance/company/{companyId}/upcoming
     */
    @GetMapping("/company/{companyId}/upcoming")
    @Operation(summary = "Get upcoming maintenance", description = "Retrieve upcoming maintenance items due soon")
    public ResponseEntity<ApiResponse<List<MaintenanceScheduleResponse>>> getUpcomingMaintenanceItems(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "30") int daysAhead
    ) {
        try {
            log.info("Retrieving upcoming maintenance for company: {}", companyId);

            List<MaintenanceService.UpcomingMaintenanceItem> items = maintenanceService.getUpcomingMaintenanceItems(companyId, daysAhead);
            List<MaintenanceScheduleResponse> responses = items.stream()
                    .map(item -> MaintenanceScheduleResponse.builder()
                            .vehicleId(item.vehicleId())
                            .vehiclePlateNumber(item.registrationNumber())
                            .serviceType(item.serviceType())
                            .nextDueDate(java.time.LocalDate.ofEpochDay(item.dueDate()))
                            .daysUntilDue(item.daysUntilDue())
                            .status(item.daysUntilDue() <= 7 ? "DUE_SOON" : "UPCOMING")
                            .priority(item.daysUntilDue() <= 7 ? "HIGH" : "MEDIUM")
                            .build())
                    .toList();

            return ResponseEntity.ok(ApiResponse.success(responses, "Upcoming maintenance retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving upcoming maintenance", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve upcoming maintenance: " + e.getMessage()));
        }
    }

    /**
     * Get maintenance status for vehicle
     * GET /api/maintenance/{vehicleId}/status
     */
    @GetMapping("/{vehicleId}/status")
    @Operation(summary = "Get vehicle maintenance status", description = "Retrieve maintenance status and summary for a vehicle")
    public ResponseEntity<ApiResponse<Object>> getMaintenanceStatus(
            @PathVariable Long vehicleId
    ) {
        try {
            log.info("Retrieving maintenance status for vehicle: {}", vehicleId);

            MaintenanceService.MaintenanceStatus status = maintenanceService.getMaintenanceStatus(vehicleId);
            return ResponseEntity.ok(ApiResponse.success(status, "Maintenance status retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving maintenance status", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve maintenance status: " + e.getMessage()));
        }
    }

    /**
     * Get vehicles needing maintenance
     * GET /api/maintenance/company/{companyId}/vehicles-needing-maintenance
     */
    @GetMapping("/company/{companyId}/vehicles-needing-maintenance")
    @Operation(summary = "Get vehicles needing maintenance", description = "Retrieve list of vehicles that need maintenance")
    public ResponseEntity<ApiResponse<List<Long>>> getVehiclesNeedingMaintenance(
            @PathVariable Long companyId
    ) {
        try {
            log.info("Retrieving vehicles needing maintenance for company: {}", companyId);

            List<Long> vehicleIds = maintenanceService.getVehiclesNeedingMaintenance(companyId);
            return ResponseEntity.ok(ApiResponse.success(vehicleIds, "Vehicles needing maintenance retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving vehicles needing maintenance", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve vehicles needing maintenance: " + e.getMessage()));
        }
    }

    // ==================== Maintenance Cost Analysis ====================

    /**
     * Get maintenance summary
     * GET /api/maintenance/company/{companyId}/summary
     */
    @GetMapping("/company/{companyId}/summary")
    @Operation(summary = "Get maintenance summary", description = "Retrieve maintenance summary statistics for a company")
    public ResponseEntity<ApiResponse<MaintenanceSummaryResponse>> getMaintenanceSummary(
            @PathVariable Long companyId,
            @RequestParam Long fromDate,
            @RequestParam Long toDate
    ) {
        try {
            log.info("Retrieving maintenance summary for company: {}", companyId);

            MaintenanceService.MaintenanceSummaryReport report = maintenanceService.getMaintenanceSummary(companyId, fromDate, toDate);
            
            MaintenanceSummaryResponse response = MaintenanceSummaryResponse.builder()
                    .totalRecords(report.totalRecords())
                    .completedRecords(report.completedRecords())
                    .pendingRecords(report.pendingRecords())
                    .totalCost(report.totalCost())
                    .averageCost(report.averageCost())
                    .overallHealth("GOOD")
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response, "Maintenance summary retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving maintenance summary", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve maintenance summary: " + e.getMessage()));
        }
    }

    /**
     * Get maintenance cost analysis
     * GET /api/maintenance/{vehicleId}/cost-analysis
     */
    @GetMapping("/{vehicleId}/cost-analysis")
    @Operation(summary = "Get maintenance cost analysis", description = "Retrieve detailed maintenance cost analysis for a vehicle")
    public ResponseEntity<ApiResponse<MaintenanceCostAnalysisResponse>> getMaintenanceCostAnalysis(
            @PathVariable Long vehicleId
    ) {
        try {
            log.info("Retrieving maintenance cost analysis for vehicle: {}", vehicleId);

            java.math.BigDecimal totalCost = maintenanceService.getTotalMaintenanceCost(vehicleId, 0L, System.currentTimeMillis() / 86400000);
            java.math.BigDecimal costPerKm = maintenanceService.getMaintenanceCostPerKm(vehicleId);

            MaintenanceCostAnalysisResponse response = MaintenanceCostAnalysisResponse.builder()
                    .vehicleId(vehicleId)
                    .totalCost(totalCost)
                    .costPerKilometer(costPerKm)
                    .trend("STABLE")
                    .recommendation("MONITOR_CLOSELY")
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response, "Cost analysis retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving cost analysis", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve cost analysis: " + e.getMessage()));
        }
    }

    /**
     * Get available service types
     * GET /api/maintenance/service-types
     */
    @GetMapping("/service-types")
    @Operation(summary = "Get service types", description = "Retrieve all available maintenance service types")
    public ResponseEntity<ApiResponse<List<?>>> getAvailableServiceTypes() {
        try {
            log.info("Retrieving available service types");

            List<MaintenanceService.ServiceType> serviceTypes = maintenanceService.getAvailableServiceTypes();
            return ResponseEntity.ok(ApiResponse.success(serviceTypes, "Service types retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving service types", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve service types: " + e.getMessage()));
        }
    }

    /**
     * Get fleet maintenance health
     * GET /api/maintenance/company/{companyId}/fleet-health
     */
    @GetMapping("/company/{companyId}/fleet-health")
    @Operation(summary = "Get fleet maintenance health", description = "Retrieve overall fleet maintenance health status")
    public ResponseEntity<ApiResponse<Object>> getFleetMaintenanceHealth(
            @PathVariable Long companyId
    ) {
        try {
            log.info("Retrieving fleet maintenance health for company: {}", companyId);

            MaintenanceService.FleetMaintenanceHealth health = maintenanceService.getFleetMaintenanceHealth(companyId);
            return ResponseEntity.ok(ApiResponse.success(health, "Fleet health retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving fleet health", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve fleet health: " + e.getMessage()));
        }
    }

    /**
     * Assign vendor to maintenance
     * POST /api/maintenance/{recordId}/assign-vendor
     */
    @PostMapping("/{recordId}/assign-vendor")
    @Operation(summary = "Assign vendor", description = "Assign a maintenance vendor to a maintenance record")
    public ResponseEntity<ApiResponse<String>> assignVendor(
            @PathVariable Long recordId,
            @RequestParam String vendorName
    ) {
        try {
            log.info("Assigning vendor to maintenance record: {}", recordId);

            maintenanceService.assignVendor(recordId, vendorName);
            return ResponseEntity.ok(ApiResponse.success("Vendor assigned successfully"));
        } catch (Exception e) {
            log.error("Error assigning vendor", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to assign vendor: " + e.getMessage()));
        }
    }

    /**
     * Get vendor performance
     * GET /api/maintenance/vendor/{vendorName}/performance
     */
    @GetMapping("/vendor/{vendorName}/performance")
    @Operation(summary = "Get vendor performance", description = "Retrieve performance metrics for a maintenance vendor")
    public ResponseEntity<ApiResponse<VendorPerformanceResponse>> getVendorPerformance(
            @PathVariable String vendorName
    ) {
        try {
            log.info("Retrieving vendor performance for: {}", vendorName);

            MaintenanceService.VendorPerformance performance = maintenanceService.getVendorPerformance(vendorName);
            
            VendorPerformanceResponse response = VendorPerformanceResponse.builder()
                    .vendorName(performance.vendorName())
                    .qualityRating(performance.quality())
                    .timelinessRating(performance.timeliness())
                    .costEfficiencyRating(performance.costEfficiency())
                    .overallRating(performance.overallRating())
                    .jobsCompleted(performance.jobsCompleted())
                    .recommendation("RECOMMENDED")
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response, "Vendor performance retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving vendor performance", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve vendor performance: " + e.getMessage()));
        }
    }
}
