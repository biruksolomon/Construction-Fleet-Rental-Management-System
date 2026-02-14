package com.devcast.fleetmanagement.features.fuel.service;

import com.devcast.fleetmanagement.features.fuel.dto.*;
import com.devcast.fleetmanagement.features.fuel.model.FuelLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Fuel Service Interface
 * Handles fuel tracking, consumption analysis, and cost management
 */
public interface FuelService {

    // ==================== Fuel Log Operations ====================

    /**
     * Log fuel entry
     */
    FuelLog logFuelEntry(Long vehicleId, FuelLog fuelLog);

    /**
     * Get fuel logs for vehicle
     */
    Page<FuelLog> getFuelLogs(Long vehicleId, Pageable pageable);

    /**
     * Get fuel logs by period
     */
    List<FuelLog> getFuelLogsByPeriod(Long vehicleId, Long fromDate, Long toDate);

    /**
     * Update fuel log
     */
    FuelLog updateFuelLog(Long logId, FuelLog fuelLog);

    /**
     * Delete fuel log
     */
    void deleteFuelLog(Long logId);

    /**
     * Get latest fuel log
     */
    Optional<FuelLog> getLatestFuelLog(Long vehicleId);

    // ==================== Fuel Consumption Analysis ====================

    /**
     * Calculate fuel consumption
     */
    BigDecimal calculateConsumption(Long vehicleId, Long fromDate, Long toDate);

    /**
     * Get average fuel consumption
     */
    BigDecimal getAverageConsumption(Long vehicleId);

    /**
     * Get fuel consumption trend
     */
    List<ConsumptionTrend> getFuelTrend(Long vehicleId, Long fromDate, Long toDate);

    /**
     * Compare fuel consumption across vehicles
     */
    List<VehicleFuelComparison> compareFuelConsumption(Long companyId, Long fromDate, Long toDate);

    /**
     * Get fuel efficiency report
     */
    FuelEfficiencyReport getFuelEfficiencyReport(Long vehicleId, Long fromDate, Long toDate);

    // ==================== Fuel Cost Analysis ====================

    /**
     * Calculate total fuel cost
     */
    BigDecimal calculateTotalFuelCost(Long vehicleId, Long fromDate, Long toDate);

    /**
     * Get fuel cost per kilometer
     */
    BigDecimal getFuelCostPerKm(Long vehicleId, Long fromDate, Long toDate);

    /**
     * Get fuel cost per liter
     */
    BigDecimal getAverageFuelPrice(Long vehicleId, Long fromDate, Long toDate);

    /**
     * Detect price fluctuations
     */
    List<PriceFluctuation> detectPriceFluctuations(Long vehicleId, Long fromDate, Long toDate);

    /**
     * Get cheapest fuel vendor
     */
    String getCheapestFuelVendor(Long companyId);

    // ==================== Fuel Anomaly Detection ====================

    /**
     * Detect fuel anomalies
     */
    List<FuelAnomaly> detectAnomalies(Long vehicleId);

    /**
     * Check for sudden consumption spikes
     */
    List<FuelSpike> detectConsumptionSpikes(Long vehicleId);

    /**
     * Alert for suspicious fuel logs
     */
    List<SuspiciousFuelLog> getSuspiciousFuelLogs(Long companyId);

    /**
     * Get fuel theft alerts
     */
    List<FuelTheftAlert> getFuelTheftAlerts(Long companyId);

    // ==================== Fleet Fuel Analysis ====================

    /**
     * Get fleet-wide fuel consumption
     */
    FleetFuelStatistics getFleetFuelStatistics(Long companyId, Long fromDate, Long toDate);

    /**
     * Get fuel consumption by vehicle category
     */
    List<CategoryFuelStats> getFuelStatsByCategory(Long companyId, Long fromDate, Long toDate);

    /**
     * Get vehicles with high fuel consumption
     */
    List<VehicleWithHighConsumption> getHighConsumptionVehicles(Long companyId, double threshold);

    /**
     * Get fuel optimization recommendations
     */
    List<FuelOptimizationTip> getFuelOptimizationTips(Long companyId);

    /**
     * Calculate potential fuel savings
     */
    BigDecimal calculatePotentialSavings(Long companyId);

    // ==================== Fuel Budget Management ====================

    /**
     * Set fuel budget for vehicle
     */
    void setFuelBudget(Long vehicleId, BigDecimal monthlyBudget);

    /**
     * Get fuel budget status
     */
    FuelBudgetStatus getFuelBudgetStatus(Long vehicleId);

    /**
     * Check if over budget
     */
    boolean isOverBudget(Long vehicleId);

    /**
     * Alert vehicles approaching budget limit
     */
    List<Long> getVehiclesApproachingBudgetLimit(Long companyId);

    /**
     * Get fleet-wide budget vs actual
     */
    BudgetComparison getFleetBudgetComparison(Long companyId);

    // ==================== Fuel Vendor Management ====================

    /**
     * Get fuel logs by vendor
     */
    List<FuelLog> getFuelLogsByVendor(Long companyId, String vendor, Long fromDate, Long toDate);

    /**
     * Get vendor fuel statistics
     */
    VendorFuelStatistics getVendorStatistics(Long companyId, String vendor);

    /**
     * Compare fuel vendors
     */
    List<VendorComparison> compareVendors(Long companyId, Long fromDate, Long toDate);

    /**
     * Get vendor performance rating
     */
    Double getVendorRating(String vendor);

    // ==================== Reporting & Export ====================

    /**
     * Generate fuel consumption report
     */
    String generateFuelReport(Long companyId, Long fromDate, Long toDate);

    /**
     * Export fuel logs to CSV
     */
    byte[] exportFuelLogsToCSV(Long vehicleId, Long fromDate, Long toDate);

    /**
     * Generate fuel cost analysis report
     */
    String generateCostAnalysisReport(Long companyId, Long fromDate, Long toDate);

}
