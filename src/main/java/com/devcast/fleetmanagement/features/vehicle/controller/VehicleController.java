package com.devcast.fleetmanagement.features.vehicle.controller;

import com.devcast.fleetmanagement.features.auth.dto.ApiResponse;
import com.devcast.fleetmanagement.features.vehicle.dto.*;
import com.devcast.fleetmanagement.features.vehicle.model.Vehicle;
import com.devcast.fleetmanagement.features.vehicle.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

/**
 * Vehicle Controller
 *
 * Provides REST endpoints for vehicle management, including CRUD operations,
 * status management, time tracking, GPS logging, and analytics.
 *
 * Base path: /v1/vehicles (context path /api is already set)
 * All endpoints require authentication and proper RBAC permissions.
 */
@RestController
@RequestMapping("/v1/vehicles")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Vehicle management APIs")
public class VehicleController {

    private final VehicleService vehicleService;

    // ==================== CRUD Operations ====================

    @PostMapping
    @Operation(summary = "Create new vehicle", description = "Create a new vehicle with the provided details")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Vehicle created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Plate number already exists")
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @Valid @RequestBody VehicleCreateRequest request) {
        try {
            log.info("Creating vehicle for company: {}", companyId);
            VehicleResponse response = vehicleService.createVehicle(companyId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Vehicle created successfully"));
        } catch (Exception e) {
            log.error("Error creating vehicle", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create vehicle: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID", description = "Retrieve vehicle details by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicle found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        try {
            log.info("Retrieving vehicle: {}", id);
            return vehicleService.getVehicleById(id)
                    .map(v -> ResponseEntity.ok(ApiResponse.success(v, "Vehicle retrieved successfully")))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve vehicle: " + e.getMessage()));
        }
    }

    @GetMapping("/plate/{plateNumber}")
    @Operation(summary = "Get vehicle by plate number", description = "Retrieve vehicle by license plate number")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicle found")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleByPlateNumber(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @Parameter(description = "License plate number") @PathVariable String plateNumber) {
        try {
            log.info("Retrieving vehicle by plate: {}", plateNumber);
            return vehicleService.getVehicleByPlateNumber(companyId, plateNumber)
                    .map(v -> ResponseEntity.ok(ApiResponse.success(v, "Vehicle retrieved successfully")))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve vehicle: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle", description = "Update vehicle details (partial update supported)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicle updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            @Valid @RequestBody VehicleUpdateRequest request) {
        try {
            log.info("Updating vehicle: {}", id);
            VehicleResponse response = vehicleService.updateVehicle(id, request);
            return ResponseEntity.ok(ApiResponse.success(response, "Vehicle updated successfully"));
        } catch (Exception e) {
            log.error("Error updating vehicle", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update vehicle: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle", description = "Delete vehicle permanently")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Vehicle deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        try {
            log.info("Deleting vehicle: {}", id);
            vehicleService.deleteVehicle(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Vehicle deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting vehicle", e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete vehicle: " + e.getMessage()));
        }
    }

    // ==================== List & Filter ====================

    @GetMapping
    @Operation(summary = "Get vehicles by company", description = "Retrieve all vehicles for a company with pagination")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicles retrieved successfully")
    public ResponseEntity<ApiResponse<Page<VehicleResponse>>> getVehicles(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            Pageable pageable) {
        try {
            log.info("Retrieving vehicles for company: {}", companyId);
            Page<VehicleResponse> vehicles = vehicleService.getVehiclesByCompany(companyId, pageable);
            return ResponseEntity.ok(ApiResponse.success(vehicles, "Vehicles retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve vehicles: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    @Operation(summary = "Get active vehicles", description = "Retrieve all available vehicles in a company")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Active vehicles retrieved")
    public ResponseEntity<ApiResponse<Page<VehicleResponse>>> getActiveVehicles(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            Pageable pageable) {
        try {
            log.info("Retrieving active vehicles for company: {}", companyId);
            Page<VehicleResponse> vehicles = vehicleService.getVehiclesByCompany(companyId, pageable);
            return ResponseEntity.ok(ApiResponse.success(vehicles, "Active vehicles retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve active vehicles: " + e.getMessage()));
        }
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get vehicles by status", description = "Retrieve vehicles filtered by status")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicles retrieved successfully")
    public ResponseEntity<ApiResponse<Page<VehicleResponse>>> getVehiclesByStatus(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @Parameter(description = "Vehicle status (AVAILABLE, RENTED, MAINTENANCE, INACTIVE)") @PathVariable Vehicle.VehicleStatus status,
            Pageable pageable) {
        try {
            log.info("Retrieving vehicles by status: {}", status);
            Page<VehicleResponse> vehicles = vehicleService.getVehiclesByStatus(companyId, status, pageable);
            return ResponseEntity.ok(ApiResponse.success(vehicles, "Vehicles retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve vehicles by status: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search vehicles", description = "Search vehicles by plate number or asset code")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search results retrieved")
    public ResponseEntity<ApiResponse<Page<VehicleResponse>>> searchVehicles(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            Pageable pageable) {
        try {
            log.info("Searching vehicles with term: {}", searchTerm);
            Page<VehicleResponse> vehicles = vehicleService.searchVehicles(companyId, searchTerm, pageable);
            return ResponseEntity.ok(ApiResponse.success(vehicles, "Search results retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to search vehicles: " + e.getMessage()));
        }
    }

    @PostMapping("/filter")
    @Operation(summary = "Filter vehicles", description = "Filter vehicles by multiple criteria")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Filtered results retrieved")
    public ResponseEntity<ApiResponse<Page<VehicleResponse>>> filterVehicles(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @Valid @RequestBody VehicleFilterCriteria criteria,
            Pageable pageable) {
        try {
            log.info("Filtering vehicles with criteria");
            Page<VehicleResponse> vehicles = vehicleService.filterVehicles(companyId, criteria, pageable);
            return ResponseEntity.ok(ApiResponse.success(vehicles, "Filtered results retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to filter vehicles: " + e.getMessage()));
        }
    }

    // ==================== Status Management ====================

    @PutMapping("/{id}/mark-available")
    @Operation(summary = "Mark vehicle as available", description = "Change vehicle status to available")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicle marked as available")
    public ResponseEntity<ApiResponse<Void>> markAvailable(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        try {
            log.info("Marking vehicle as available: {}", id);
            vehicleService.markAvailable(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Vehicle marked as available"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to mark vehicle as available: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/mark-rented")
    @Operation(summary = "Mark vehicle as rented", description = "Change vehicle status to rented")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicle marked as rented")
    public ResponseEntity<ApiResponse<Void>> markRented(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        try {
            log.info("Marking vehicle as rented: {}", id);
            vehicleService.markRented(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Vehicle marked as rented"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to mark vehicle as rented: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/mark-maintenance")
    @Operation(summary = "Mark vehicle for maintenance", description = "Change vehicle status to maintenance")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicle marked for maintenance")
    public ResponseEntity<ApiResponse<Void>> markForMaintenance(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            @Parameter(description = "Reason for maintenance") @RequestParam(required = false) String reason) {
        try {
            log.info("Marking vehicle for maintenance: {}", id);
            vehicleService.markForMaintenance(id, reason);
            return ResponseEntity.ok(ApiResponse.success(null, "Vehicle marked for maintenance"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to mark vehicle for maintenance: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/mark-inactive")
    @Operation(summary = "Mark vehicle as inactive", description = "Change vehicle status to inactive")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicle marked as inactive")
    public ResponseEntity<ApiResponse<Void>> markInactive(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            @Parameter(description = "Reason for inactivity") @RequestParam(required = false) String reason) {
        try {
            log.info("Marking vehicle as inactive: {}", id);
            vehicleService.markInactive(id, reason);
            return ResponseEntity.ok(ApiResponse.success(null, "Vehicle marked as inactive"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to mark vehicle as inactive: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get vehicle status", description = "Retrieve current vehicle status")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicle status retrieved")
    public ResponseEntity<ApiResponse<Vehicle.VehicleStatus>> getStatus(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        try {
            log.info("Getting vehicle status: {}", id);
            return vehicleService.getVehicleStatus(id)
                    .map(status -> ResponseEntity.ok(ApiResponse.success(status, "Vehicle status retrieved successfully")))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve vehicle status: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Check vehicle availability", description = "Check if vehicle is available for rental")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Availability status")
    public ResponseEntity<ApiResponse<Boolean>> checkAvailability(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        try {
            log.info("Checking vehicle availability: {}", id);
            boolean available = vehicleService.isVehicleAvailable(id);
            return ResponseEntity.ok(ApiResponse.success(available, "Availability status retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to check availability: " + e.getMessage()));
        }
    }

    // ==================== GPS & Tracking ====================

    @GetMapping("/{id}/location")
    @Operation(summary = "Get vehicle current location", description = "Retrieve real-time GPS location of vehicle")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Current location retrieved")
    public ResponseEntity<ApiResponse<?>> getCurrentLocation(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        try {
            log.info("Getting current location for vehicle: {}", id);
            var location = vehicleService.getCurrentLocation(id);
            return ResponseEntity.ok(ApiResponse.success(location, "Current location retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve location: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/location")
    @Operation(summary = "Log GPS location", description = "Log GPS coordinates for vehicle")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Location logged successfully")
    public ResponseEntity<ApiResponse<?>> logLocation(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            @Parameter(description = "Latitude") @RequestParam Double latitude,
            @Parameter(description = "Longitude") @RequestParam Double longitude) {
        try {
            log.info("Logging location for vehicle {}: ({}, {})", id, latitude, longitude);
            var log = vehicleService.logLocation(id, latitude, longitude);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(log, "Location logged successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to log location: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/tracking")
    @Operation(summary = "Get real-time tracking info", description = "Get current tracking information for vehicle")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tracking info retrieved")
    public ResponseEntity<ApiResponse<?>> getTrackingInfo(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        try {
            log.info("Getting tracking info for vehicle: {}", id);
            var trackingInfo = vehicleService.getRealtimeTracking(id);
            return ResponseEntity.ok(ApiResponse.success(trackingInfo, "Tracking info retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve tracking info: " + e.getMessage()));
        }
    }

    // ==================== Time Tracking ====================

    @PostMapping("/{id}/time-log/start")
    @Operation(summary = "Log vehicle start time", description = "Record when vehicle starts being used")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Start time logged")
    public ResponseEntity<ApiResponse<?>> logStartTime(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            @Parameter(description = "Driver ID") @RequestParam Long driverId) {
        try {
            log.info("Logging start time for vehicle: {}", id);
            var timeLog = vehicleService.logStartTime(id, driverId);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(timeLog, "Start time logged successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to log start time: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/time-log/end")
    @Operation(summary = "Log vehicle end time", description = "Record when vehicle stops being used")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "End time logged")
    public ResponseEntity<ApiResponse<?>> logEndTime(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        try {
            log.info("Logging end time for vehicle: {}", id);
            var timeLog = vehicleService.logEndTime(id);
            return ResponseEntity.ok(ApiResponse.success(timeLog, "End time logged successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to log end time: " + e.getMessage()));
        }
    }

    // ==================== Fuel Management ====================

    @GetMapping("/{id}/fuel-logs")
    @Operation(summary = "Get fuel logs", description = "Retrieve fuel consumption logs for vehicle")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Fuel logs retrieved")
    public ResponseEntity<ApiResponse<?>> getFuelLogs(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            Pageable pageable) {
        try {
            log.info("Getting fuel logs for vehicle: {}", id);
            var fuelLogs = vehicleService.getFuelLogs(id, pageable);
            return ResponseEntity.ok(ApiResponse.success(fuelLogs, "Fuel logs retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve fuel logs: " + e.getMessage()));
        }
    }

    // ==================== Maintenance ====================

    @GetMapping("/{id}/maintenance-records")
    @Operation(summary = "Get maintenance records", description = "Retrieve maintenance history for vehicle")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Maintenance records retrieved")
    public ResponseEntity<ApiResponse<?>> getMaintenanceRecords(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            Pageable pageable) {
        try {
            log.info("Getting maintenance records for vehicle: {}", id);
            var records = vehicleService.getMaintenanceRecords(id, pageable);
            return ResponseEntity.ok(ApiResponse.success(records, "Maintenance records retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve maintenance records: " + e.getMessage()));
        }
    }

    // ==================== Maintenance Management ====================

    @GetMapping("/maintenance-needed")
    @Operation(summary = "Get vehicles needing maintenance", description = "List all vehicles requiring maintenance")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Vehicles retrieved")
    public ResponseEntity<ApiResponse<?>> getVehiclesNeedingMaintenance(
            @Parameter(description = "Company ID") @RequestParam Long companyId) {
        try {
            log.info("Getting vehicles needing maintenance for company: {}", companyId);
            var vehicles = vehicleService.getVehiclesNeedingMaintenance(companyId);
            return ResponseEntity.ok(ApiResponse.success(vehicles, "Vehicles needing maintenance retrieved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve vehicles needing maintenance: " + e.getMessage()));
        }
    }
}
