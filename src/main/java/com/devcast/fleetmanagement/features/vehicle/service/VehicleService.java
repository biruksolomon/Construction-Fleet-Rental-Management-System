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
 * Vehicle Service Interface
 * Handles vehicle management, maintenance, fuel tracking, and analytics
 */
public interface VehicleService {

    // ==================== Vehicle CRUD Operations ====================

    /**
     * Create new vehicle
     */
    Vehicle createVehicle(Long companyId, Vehicle vehicle);

    /**
     * Get vehicle by ID
     */
    Optional<Vehicle> getVehicleById(Long vehicleId);

    /**
     * Update vehicle details
     */
    Vehicle updateVehicle(Long vehicleId, Vehicle vehicle);

    /**
     * Delete vehicle
     */
    void deleteVehicle(Long vehicleId);

    /**
     * Get all vehicles in company
     */
    Page<Vehicle> getVehiclesByCompany(Long companyId, Pageable pageable);

    /**
     * Get active vehicles
     */
    List<Vehicle> getActiveVehicles(Long companyId);

    /**
     * Get vehicles by status
     */
    Page<Vehicle> getVehiclesByStatus(Long companyId, String status, Pageable pageable);

    // ==================== Vehicle Status Management ====================

    /**
     * Mark vehicle as available
     */
    void markAvailable(Long vehicleId);

    /**
     * Mark vehicle as in-use
     */
    void markInUse(Long vehicleId);

    /**
     * Mark vehicle for maintenance
     */
    void markForMaintenance(Long vehicleId, String reason);

    /**
     * Mark vehicle as inactive
     */
    void markInactive(Long vehicleId, String reason);

    /**
     * Check vehicle availability
     */
    boolean isVehicleAvailable(Long vehicleId);

    /**
     * Get vehicle current status
     */
    Optional<String> getVehicleStatus(Long vehicleId);

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
    List<Vehicle> getVehiclesNeedingMaintenance(Long companyId);

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
