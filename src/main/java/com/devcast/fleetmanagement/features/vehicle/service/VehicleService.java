package com.devcast.fleetmanagement.features.vehicle.service;


import com.devcast.fleetmanagement.features.fuel.model.FuelLog;
import com.devcast.fleetmanagement.features.maintenance.model.MaintenanceRecord;
import com.devcast.fleetmanagement.features.vehicle.dto.*;
import com.devcast.fleetmanagement.features.vehicle.model.GpsLog;
import com.devcast.fleetmanagement.features.vehicle.model.Vehicle;
import com.devcast.fleetmanagement.features.vehicle.model.VehicleTimeLog;
import com.devcast.fleetmanagement.features.vehicle.model.VehicleUsageLimit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Vehicle Service Interface (DTO-Based)
 *
 * Defines contract for vehicle management using DTOs to separate API contracts from entities.
 *
 * Design Principles:
 * 1. All requests use *Request DTOs (no IDs, timestamps, or status)
 * 2. All responses use *Response DTOs (complete vehicle information)
 * 3. Service never exposes raw entities through API contracts
 * 4. RBAC checks performed in implementation
 *
 * Includes:
 * - Vehicle CRUD operations
 * - Vehicle status management
 * - Time tracking and GPS logging
 * - Fuel management and analysis
 * - Maintenance scheduling
 * - Vehicle analytics and reporting
 * - Usage limits and monitoring
 */
public interface VehicleService {

    // ==================== Vehicle CRUD Operations ====================

    /**
     * Create new vehicle
     * Request: VehicleCreateRequest (no id, timestamps, status)
     * Response: VehicleResponse (complete vehicle representation)
     * RBAC: OWNER, ADMIN, FLEET_MANAGER
     */
    VehicleResponse createVehicle(Long companyId, VehicleCreateRequest request);

    /**
     * Get vehicle by ID with multi-tenant check
     * Response: VehicleResponse (complete vehicle details)
     * RBAC: OWNER, ADMIN, FLEET_MANAGER
     */
    Optional<VehicleResponse> getVehicleById(Long vehicleId);

    /**
     * Get vehicle by plate number
     * Response: VehicleResponse
     * RBAC: Multi-tenant check enforced
     */
    Optional<VehicleResponse> getVehicleByPlateNumber(Long companyId, String plateNumber);

    /**
     * Update vehicle details
     * Request: VehicleUpdateRequest (all fields optional, no id/timestamps)
     * Response: VehicleResponse (updated vehicle representation)
     * RBAC: OWNER, ADMIN, FLEET_MANAGER
     */
    VehicleResponse updateVehicle(Long vehicleId, VehicleUpdateRequest request);

    /**
     * Delete vehicle permanently
     * RBAC: OWNER, ADMIN only
     */
    void deleteVehicle(Long vehicleId);

    /**
     * Get all vehicles in company with pagination
     * Response: Page<VehicleResponse> (paginated vehicle list)
     * RBAC: Multi-tenant check enforced
     */
    Page<VehicleResponse> getVehiclesByCompany(Long companyId, Pageable pageable);

    /**
     * Get active vehicles in company
     * Response: List<VehicleResponse> (available vehicles only)
     * RBAC: Multi-tenant check enforced
     */
    List<VehicleResponse> getActiveVehicles(Long companyId);

    /**
     * Get vehicles by status
     * Response: Page<VehicleResponse> (filtered by status)
     * RBAC: Multi-tenant check enforced
     */
    Page<VehicleResponse> getVehiclesByStatus(Long companyId, Vehicle.VehicleStatus status, Pageable pageable);

    // ==================== Vehicle Status Management ====================

    /**
     * Mark vehicle as available
     * RBAC: OWNER, ADMIN, FLEET_MANAGER
     */
    void markAvailable(Long vehicleId);

    /**
     * Mark vehicle as rented
     * RBAC: OWNER, ADMIN, FLEET_MANAGER
     */
    void markRented(Long vehicleId);

    /**
     * Mark vehicle for maintenance
     * Request: Reason for maintenance
     * RBAC: OWNER, ADMIN, FLEET_MANAGER
     */
    void markForMaintenance(Long vehicleId, String reason);

    /**
     * Mark vehicle as inactive
     * Request: Reason for inactivity
     * RBAC: OWNER, ADMIN only
     */
    void markInactive(Long vehicleId, String reason);

    /**
     * Check vehicle availability
     * Response: boolean
     */
    boolean isVehicleAvailable(Long vehicleId);

    /**
     * Get vehicle current status
     * Response: VehicleStatus enum
     */
    Optional<Vehicle.VehicleStatus> getVehicleStatus(Long vehicleId);

    // ==================== Vehicle Usage Limits ====================

    /**
     * Set vehicle usage limits
     */
    VehicleUsageLimit setUsageLimit(Long vehicleId, VehicleUsageLimit limit);

    /**
     * Get vehicle usage limits
     */
    Optional<VehicleUsageLimit> getUsageLimit(Long vehicleId);

    /**
     * Check if vehicle exceeds usage limit
     */
    boolean exceedsUsageLimit(Long vehicleId);

    /**
     * Get usage statistics
     */
    VehicleUsageStats getUsageStatistics(Long vehicleId);

    /**
     * Get vehicles approaching limit
     */
    List<Vehicle> getVehiclesApproachingLimit(Long companyId);

    // ==================== Time Tracking ====================

    /**
     * Log vehicle start time
     */
    VehicleTimeLog logStartTime(Long vehicleId, Long driverId);

    /**
     * Log vehicle end time
     */
    VehicleTimeLog logEndTime(Long vehicleId);

    /**
     * Get time logs for vehicle
     */
    Page<VehicleTimeLog> getTimeLogs(Long vehicleId, Pageable pageable);

    /**
     * Get time logs for period
     */
    List<VehicleTimeLog> getTimeLogsByPeriod(Long vehicleId, Long fromDate, Long toDate);

    /**
     * Get total usage time in period
     */
    Long getTotalUsageTime(Long vehicleId, Long fromDate, Long toDate);

    // ==================== GPS Tracking ====================

    /**
     * Log GPS location
     */
    GpsLog logLocation(Long vehicleId, Double latitude, Double longitude);

    /**
     * Get current vehicle location
     */
    Optional<GpsLog> getCurrentLocation(Long vehicleId);

    /**
     * Get GPS trail for period
     */
    List<GpsLog> getGpsTrail(Long vehicleId, Long fromDate, Long toDate);

    /**
     * Get vehicle route
     */
    VehicleRoute getVehicleRoute(Long vehicleId, Long fromDate, Long toDate);

    /**
     * Track vehicle real-time
     */
    VehicleTrackingInfo getRealtimeTracking(Long vehicleId);

    /**
     * Generate geofence alert
     */
    void checkGeofenceViolation(Long vehicleId, Double latitude, Double longitude);

    // ==================== Fuel Management ====================

    /**
     * Log fuel entry
     */
    FuelLog logFuel(Long vehicleId, FuelLog fuelLog);

    /**
     * Get fuel logs
     */
    Page<FuelLog> getFuelLogs(Long vehicleId, Pageable pageable);

    /**
     * Get fuel consumption analysis
     */
    FuelConsumptionAnalysis getFuelAnalysis(Long vehicleId, Long fromDate, Long toDate);

    /**
     * Get average fuel consumption
     */
    BigDecimal getAverageFuelConsumption(Long vehicleId);

    /**
     * Alert for unusual fuel consumption
     */
    List<FuelAnomaly> detectFuelAnomalies(Long vehicleId);

    // ==================== Maintenance ====================

    /**
     * Log maintenance record
     */
    MaintenanceRecord logMaintenance(Long vehicleId, MaintenanceRecord record);

    /**
     * Get maintenance records
     */
    Page<MaintenanceRecord> getMaintenanceRecords(Long vehicleId, Pageable pageable);

    /**
     * Get pending maintenance
     */
    List<MaintenanceRecord> getPendingMaintenance(Long vehicleId);

    /**
     * Get maintenance schedule
     */
    List<MaintenanceSchedule> getMaintenanceSchedule(Long vehicleId);

    /**
     * Complete maintenance
     */
    MaintenanceRecord completeMaintenance(Long recordId, String notes);

    /**
     * Get vehicles needing maintenance
     */
    List<VehicleResponse> getVehiclesNeedingMaintenance(Long companyId);

    // ==================== Vehicle Search & Filter ====================

    /**
     * Search vehicles by plate number or asset code
     * Response: Page<VehicleResponse> (matching vehicles)
     * RBAC: Multi-tenant check enforced
     */
    Page<VehicleResponse> searchVehicles(Long companyId, String searchTerm, Pageable pageable);

    /**
     * Filter vehicles by multiple criteria
     * Response: Page<VehicleResponse> (filtered vehicles)
     * RBAC: Multi-tenant check enforced
     */
    Page<VehicleResponse> filterVehicles(Long companyId, VehicleFilterCriteria criteria, Pageable pageable);

    /**
     * Get total maintenance cost
     */
    BigDecimal getTotalMaintenanceCost(Long vehicleId, Long fromDate, Long toDate);

    // ==================== Vehicle Analytics ====================

    /**
     * Get vehicle performance report
     */
    VehiclePerformanceReport getPerformanceReport(Long vehicleId, Long fromDate, Long toDate);

    /**
     * Get fleet health status
     */
    FleetHealthStatus getFleetHealth(Long companyId);

    /**
     * Get cost per kilometer
     */
    BigDecimal getCostPerKm(Long vehicleId, Long fromDate, Long toDate);

    /**
     * Get revenue per kilometer
     */
    BigDecimal getRevenuePerKm(Long vehicleId, Long fromDate, Long toDate);

    /**
     * Get vehicle profitability
     */
    VehicleProfitability getVehicleProfitability(Long vehicleId, Long fromDate, Long toDate);

    /**
     * Get fleet comparison
     */
    List<VehicleComparison> getFleetComparison(Long companyId);
}
