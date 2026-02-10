package com.devcast.fleetmanagement.features.vehicle.controller;

import com.devcast.fleetmanagement.features.vehicle.dto.*;
import com.devcast.fleetmanagement.features.vehicle.model.Vehicle;
import com.devcast.fleetmanagement.features.vehicle.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
            @ApiResponse(responseCode = "201", description = "Vehicle created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "Plate number already exists")
    })
    public ResponseEntity<VehicleResponse> createVehicle(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @Valid @RequestBody VehicleCreateRequest request) {
        log.info("Creating vehicle for company: {}", companyId);
        VehicleResponse response = vehicleService.createVehicle(companyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID", description = "Retrieve vehicle details by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle found"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<VehicleResponse> getVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        log.info("Retrieving vehicle: {}", id);
        return vehicleService.getVehicleById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/plate/{plateNumber}")
    @Operation(summary = "Get vehicle by plate number", description = "Retrieve vehicle by license plate number")
    @ApiResponse(responseCode = "200", description = "Vehicle found")
    public ResponseEntity<VehicleResponse> getVehicleByPlateNumber(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @Parameter(description = "License plate number") @PathVariable String plateNumber) {
        log.info("Retrieving vehicle by plate: {}", plateNumber);
        return vehicleService.getVehicleByPlateNumber(companyId, plateNumber)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vehicle", description = "Update vehicle details (partial update supported)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vehicle updated successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<VehicleResponse> updateVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            @Valid @RequestBody VehicleUpdateRequest request) {
        log.info("Updating vehicle: {}", id);
        VehicleResponse response = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vehicle", description = "Delete vehicle permanently")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Vehicle deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Void> deleteVehicle(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        log.info("Deleting vehicle: {}", id);
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== List & Filter ====================

    @GetMapping
    @Operation(summary = "Get vehicles by company", description = "Retrieve all vehicles for a company with pagination")
    @ApiResponse(responseCode = "200", description = "Vehicles retrieved successfully")
    public ResponseEntity<Page<VehicleResponse>> getVehicles(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            Pageable pageable) {
        log.info("Retrieving vehicles for company: {}", companyId);
        Page<VehicleResponse> vehicles = vehicleService.getVehiclesByCompany(companyId, pageable);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active vehicles", description = "Retrieve all available vehicles in a company")
    @ApiResponse(responseCode = "200", description = "Active vehicles retrieved")
    public ResponseEntity<Page<VehicleResponse>> getActiveVehicles(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            Pageable pageable) {
        log.info("Retrieving active vehicles for company: {}", companyId);
        Page<VehicleResponse> vehicles = vehicleService.getVehiclesByCompany(companyId, pageable);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get vehicles by status", description = "Retrieve vehicles filtered by status")
    @ApiResponse(responseCode = "200", description = "Vehicles retrieved successfully")
    public ResponseEntity<Page<VehicleResponse>> getVehiclesByStatus(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @Parameter(description = "Vehicle status (AVAILABLE, RENTED, MAINTENANCE, INACTIVE)") @PathVariable Vehicle.VehicleStatus status,
            Pageable pageable) {
        log.info("Retrieving vehicles by status: {}", status);
        Page<VehicleResponse> vehicles = vehicleService.getVehiclesByStatus(companyId, status, pageable);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/search")
    @Operation(summary = "Search vehicles", description = "Search vehicles by plate number or asset code")
    @ApiResponse(responseCode = "200", description = "Search results retrieved")
    public ResponseEntity<Page<VehicleResponse>> searchVehicles(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @Parameter(description = "Search term") @RequestParam String searchTerm,
            Pageable pageable) {
        log.info("Searching vehicles with term: {}", searchTerm);
        Page<VehicleResponse> vehicles = vehicleService.searchVehicles(companyId, searchTerm, pageable);
        return ResponseEntity.ok(vehicles);
    }

    @PostMapping("/filter")
    @Operation(summary = "Filter vehicles", description = "Filter vehicles by multiple criteria")
    @ApiResponse(responseCode = "200", description = "Filtered results retrieved")
    public ResponseEntity<Page<VehicleResponse>> filterVehicles(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @Valid @RequestBody VehicleFilterCriteria criteria,
            Pageable pageable) {
        log.info("Filtering vehicles with criteria");
        Page<VehicleResponse> vehicles = vehicleService.filterVehicles(companyId, criteria, pageable);
        return ResponseEntity.ok(vehicles);
    }

    // ==================== Status Management ====================

    @PutMapping("/{id}/mark-available")
    @Operation(summary = "Mark vehicle as available", description = "Change vehicle status to available")
    @ApiResponse(responseCode = "200", description = "Vehicle marked as available")
    public ResponseEntity<Void> markAvailable(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        log.info("Marking vehicle as available: {}", id);
        vehicleService.markAvailable(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/mark-rented")
    @Operation(summary = "Mark vehicle as rented", description = "Change vehicle status to rented")
    @ApiResponse(responseCode = "200", description = "Vehicle marked as rented")
    public ResponseEntity<Void> markRented(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        log.info("Marking vehicle as rented: {}", id);
        vehicleService.markRented(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/mark-maintenance")
    @Operation(summary = "Mark vehicle for maintenance", description = "Change vehicle status to maintenance")
    @ApiResponse(responseCode = "200", description = "Vehicle marked for maintenance")
    public ResponseEntity<Void> markForMaintenance(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            @Parameter(description = "Reason for maintenance") @RequestParam(required = false) String reason) {
        log.info("Marking vehicle for maintenance: {}", id);
        vehicleService.markForMaintenance(id, reason);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/mark-inactive")
    @Operation(summary = "Mark vehicle as inactive", description = "Change vehicle status to inactive")
    @ApiResponse(responseCode = "200", description = "Vehicle marked as inactive")
    public ResponseEntity<Void> markInactive(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            @Parameter(description = "Reason for inactivity") @RequestParam(required = false) String reason) {
        log.info("Marking vehicle as inactive: {}", id);
        vehicleService.markInactive(id, reason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "Get vehicle status", description = "Retrieve current vehicle status")
    @ApiResponse(responseCode = "200", description = "Vehicle status retrieved")
    public ResponseEntity<Vehicle.VehicleStatus> getStatus(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        log.info("Getting vehicle status: {}", id);
        return vehicleService.getVehicleStatus(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Check vehicle availability", description = "Check if vehicle is available for rental")
    @ApiResponse(responseCode = "200", description = "Availability status")
    public ResponseEntity<Boolean> checkAvailability(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        log.info("Checking vehicle availability: {}", id);
        boolean available = vehicleService.isVehicleAvailable(id);
        return ResponseEntity.ok(available);
    }

    // ==================== GPS & Tracking ====================

    @GetMapping("/{id}/location")
    @Operation(summary = "Get vehicle current location", description = "Retrieve real-time GPS location of vehicle")
    @ApiResponse(responseCode = "200", description = "Current location retrieved")
    public ResponseEntity<?> getCurrentLocation(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        log.info("Getting current location for vehicle: {}", id);
        var location = vehicleService.getCurrentLocation(id);
        return ResponseEntity.ok(location);
    }

    @PostMapping("/{id}/location")
    @Operation(summary = "Log GPS location", description = "Log GPS coordinates for vehicle")
    @ApiResponse(responseCode = "201", description = "Location logged successfully")
    public ResponseEntity<?> logLocation(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            @Parameter(description = "Latitude") @RequestParam Double latitude,
            @Parameter(description = "Longitude") @RequestParam Double longitude) {
        log.info("Logging location for vehicle {}: ({}, {})", id, latitude, longitude);
        var log = vehicleService.logLocation(id, latitude, longitude);
        return ResponseEntity.status(HttpStatus.CREATED).body(log);
    }

    @GetMapping("/{id}/tracking")
    @Operation(summary = "Get real-time tracking info", description = "Get current tracking information for vehicle")
    @ApiResponse(responseCode = "200", description = "Tracking info retrieved")
    public ResponseEntity<?> getTrackingInfo(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        log.info("Getting tracking info for vehicle: {}", id);
        var trackingInfo = vehicleService.getRealtimeTracking(id);
        return ResponseEntity.ok(trackingInfo);
    }

    // ==================== Time Tracking ====================

    @PostMapping("/{id}/time-log/start")
    @Operation(summary = "Log vehicle start time", description = "Record when vehicle starts being used")
    @ApiResponse(responseCode = "201", description = "Start time logged")
    public ResponseEntity<?> logStartTime(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            @Parameter(description = "Driver ID") @RequestParam Long driverId) {
        log.info("Logging start time for vehicle: {}", id);
        var timeLog = vehicleService.logStartTime(id, driverId);
        return ResponseEntity.status(HttpStatus.CREATED).body(timeLog);
    }

    @PostMapping("/{id}/time-log/end")
    @Operation(summary = "Log vehicle end time", description = "Record when vehicle stops being used")
    @ApiResponse(responseCode = "200", description = "End time logged")
    public ResponseEntity<?> logEndTime(
            @Parameter(description = "Vehicle ID") @PathVariable Long id) {
        log.info("Logging end time for vehicle: {}", id);
        var timeLog = vehicleService.logEndTime(id);
        return ResponseEntity.ok(timeLog);
    }

    // ==================== Fuel Management ====================

    @GetMapping("/{id}/fuel-logs")
    @Operation(summary = "Get fuel logs", description = "Retrieve fuel consumption logs for vehicle")
    @ApiResponse(responseCode = "200", description = "Fuel logs retrieved")
    public ResponseEntity<?> getFuelLogs(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            Pageable pageable) {
        log.info("Getting fuel logs for vehicle: {}", id);
        var fuelLogs = vehicleService.getFuelLogs(id, pageable);
        return ResponseEntity.ok(fuelLogs);
    }

    // ==================== Maintenance ====================

    @GetMapping("/{id}/maintenance-records")
    @Operation(summary = "Get maintenance records", description = "Retrieve maintenance history for vehicle")
    @ApiResponse(responseCode = "200", description = "Maintenance records retrieved")
    public ResponseEntity<?> getMaintenanceRecords(
            @Parameter(description = "Vehicle ID") @PathVariable Long id,
            Pageable pageable) {
        log.info("Getting maintenance records for vehicle: {}", id);
        var records = vehicleService.getMaintenanceRecords(id, pageable);
        return ResponseEntity.ok(records);
    }

    // ==================== Maintenance Management ====================

    @GetMapping("/maintenance-needed")
    @Operation(summary = "Get vehicles needing maintenance", description = "List all vehicles requiring maintenance")
    @ApiResponse(responseCode = "200", description = "Vehicles retrieved")
    public ResponseEntity<?> getVehiclesNeedingMaintenance(
            @Parameter(description = "Company ID") @RequestParam Long companyId) {
        log.info("Getting vehicles needing maintenance for company: {}", companyId);
        var vehicles = vehicleService.getVehiclesNeedingMaintenance(companyId);
        return ResponseEntity.ok(vehicles);
    }
}
