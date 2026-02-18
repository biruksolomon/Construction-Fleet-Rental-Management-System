package com.devcast.fleetmanagement.features.fuel.controller;

import com.devcast.fleetmanagement.features.fuel.dto.*;
import com.devcast.fleetmanagement.features.fuel.model.FuelLog;
import com.devcast.fleetmanagement.features.fuel.service.FuelService;
import com.devcast.fleetmanagement.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Fuel Management Controller
 * Handles all fuel tracking, analysis, and reporting operations
 */
@RestController
@RequestMapping("/api/v1/fuel")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fuel Management", description = "Fuel tracking, consumption analysis, and cost management")
public class FuelController {

    private final FuelService fuelService;

    // ==================== Fuel Log Operations ====================

    @PostMapping("/logs")
    @Operation(summary = "Log fuel entry", description = "Create a new fuel log entry for a vehicle")
    @ApiResponse(responseCode = "201", description = "Fuel entry logged successfully")
    public ResponseEntity<FuelLog> logFuelEntry(
            @RequestParam Long vehicleId,
            @RequestBody FuelLog fuelLog) {
        log.info("POST /fuel/logs - Logging fuel entry for vehicle: {}", vehicleId);
        FuelLog logged = fuelService.logFuelEntry(vehicleId, fuelLog);
        return ResponseEntity.status(HttpStatus.CREATED).body(logged);
    }

    @GetMapping("/vehicles/{vehicleId}/logs")
    @Operation(summary = "Get fuel logs for vehicle", description = "Retrieve paginated fuel logs for a specific vehicle")
    @ApiResponse(responseCode = "200", description = "Fuel logs retrieved successfully")
    public ResponseEntity<Page<FuelLog>> getFuelLogs(
            @PathVariable Long vehicleId,
            @ParameterObject Pageable pageable) {
        log.info("GET /fuel/vehicles/{}/logs - Retrieving fuel logs", vehicleId);
        Page<FuelLog> logs = fuelService.getFuelLogs(vehicleId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/vehicles/{vehicleId}/logs/period")
    @Operation(summary = "Get fuel logs by period", description = "Retrieve fuel logs within a date range")
    @ApiResponse(responseCode = "200", description = "Fuel logs retrieved successfully")
    public ResponseEntity<List<FuelLog>> getFuelLogsByPeriod(
            @PathVariable Long vehicleId,
            @RequestParam Long fromDate,
            @RequestParam Long toDate) {
        log.info("GET /fuel/vehicles/{}/logs/period - Retrieving fuel logs for period", vehicleId);
        List<FuelLog> logs = fuelService.getFuelLogsByPeriod(vehicleId, fromDate, toDate);
        return ResponseEntity.ok(logs);
    }

    @PutMapping("/logs/{logId}")
    @Operation(summary = "Update fuel log", description = "Update an existing fuel log entry")
    @ApiResponse(responseCode = "200", description = "Fuel log updated successfully")
    public ResponseEntity<FuelLog> updateFuelLog(
            @PathVariable Long logId,
            @RequestBody FuelLog fuelLog) {
        log.info("PUT /fuel/logs/{} - Updating fuel log", logId);
        FuelLog updated = fuelService.updateFuelLog(logId, fuelLog);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/logs/{logId}")
    @Operation(summary = "Delete fuel log", description = "Delete a fuel log entry")
    @ApiResponse(responseCode = "204", description = "Fuel log deleted successfully")
    public ResponseEntity<Void> deleteFuelLog(@PathVariable Long logId) {
        log.info("DELETE /fuel/logs/{} - Deleting fuel log", logId);
        fuelService.deleteFuelLog(logId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vehicles/{vehicleId}/logs/latest")
    @Operation(summary = "Get latest fuel log", description = "Retrieve the most recent fuel log for a vehicle")
    @ApiResponse(responseCode = "200", description = "Latest fuel log retrieved successfully")
    public ResponseEntity<?> getLatestFuelLog(@PathVariable Long vehicleId) {
        log.info("GET /fuel/vehicles/{}/logs/latest - Retrieving latest fuel log", vehicleId);
        return fuelService.getLatestFuelLog(vehicleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== Fuel Consumption Analysis ====================

    @GetMapping("/vehicles/{vehicleId}/consumption")
    @Operation(summary = "Calculate fuel consumption", description = "Calculate total fuel consumption for a vehicle in a period")
    @ApiResponse(responseCode = "200", description = "Consumption calculated successfully")
    public ResponseEntity<BigDecimal> calculateConsumption(
            @PathVariable Long vehicleId,
            @RequestParam Long fromDate,
            @RequestParam Long toDate) {
        log.info("GET /fuel/vehicles/{}/consumption - Calculating fuel consumption", vehicleId);
        BigDecimal consumption = fuelService.calculateConsumption(vehicleId, fromDate, toDate);
        return ResponseEntity.ok(consumption);
    }

    @GetMapping("/vehicles/{vehicleId}/consumption/average")
    @Operation(summary = "Get average fuel consumption", description = "Retrieve average fuel consumption for a vehicle")
    @ApiResponse(responseCode = "200", description = "Average consumption retrieved successfully")
    public ResponseEntity<BigDecimal> getAverageConsumption(@PathVariable Long vehicleId) {
        log.info("GET /fuel/vehicles/{}/consumption/average - Retrieving average consumption", vehicleId);
        BigDecimal avgConsumption = fuelService.getAverageConsumption(vehicleId);
        return ResponseEntity.ok(avgConsumption);
    }

    @GetMapping("/vehicles/{vehicleId}/trends")
    @Operation(summary = "Get fuel consumption trend", description = "Retrieve fuel consumption trends for a vehicle")
    @ApiResponse(responseCode = "200", description = "Consumption trend retrieved successfully")
    public ResponseEntity<List<ConsumptionTrend>> getFuelTrend(
            @PathVariable Long vehicleId,
            @RequestParam Long fromDate,
            @RequestParam Long toDate) {
        log.info("GET /fuel/vehicles/{}/trends - Retrieving fuel consumption trends", vehicleId);
        List<ConsumptionTrend> trends = fuelService.getFuelTrend(vehicleId, fromDate, toDate);
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/companies/{companyId}/comparison")
    @Operation(summary = "Compare fuel consumption", description = "Compare fuel consumption across fleet vehicles")
    @ApiResponse(responseCode = "200", description = "Comparison retrieved successfully")
    public ResponseEntity<List<VehicleFuelComparison>> compareFuelConsumption(
            @PathVariable Long companyId,
            @RequestParam Long fromDate,
            @RequestParam Long toDate) {
        log.info("GET /fuel/companies/{}/comparison - Comparing fuel consumption", companyId);
        List<VehicleFuelComparison> comparison = fuelService.compareFuelConsumption(companyId, fromDate, toDate);
        return ResponseEntity.ok(comparison);
    }

    @GetMapping("/vehicles/{vehicleId}/efficiency-report")
    @Operation(summary = "Get fuel efficiency report", description = "Generate fuel efficiency report for a vehicle")
    @ApiResponse(responseCode = "200", description = "Efficiency report generated successfully")
    public ResponseEntity<FuelEfficiencyReport> getFuelEfficiencyReport(
            @PathVariable Long vehicleId,
            @RequestParam Long fromDate,
            @RequestParam Long toDate) {
        log.info("GET /fuel/vehicles/{}/efficiency-report - Generating efficiency report", vehicleId);
        FuelEfficiencyReport report = fuelService.getFuelEfficiencyReport(vehicleId, fromDate, toDate);
        return ResponseEntity.ok(report);
    }

    // ==================== Fuel Cost Analysis ====================

    @GetMapping("/vehicles/{vehicleId}/cost")
    @Operation(summary = "Calculate total fuel cost", description = "Calculate total fuel cost for a vehicle in a period")
    @ApiResponse(responseCode = "200", description = "Cost calculated successfully")
    public ResponseEntity<BigDecimal> calculateTotalFuelCost(
            @PathVariable Long vehicleId,
            @RequestParam Long fromDate,
            @RequestParam Long toDate) {
        log.info("GET /fuel/vehicles/{}/cost - Calculating total fuel cost", vehicleId);
        BigDecimal cost = fuelService.calculateTotalFuelCost(vehicleId, fromDate, toDate);
        return ResponseEntity.ok(cost);
    }

    @GetMapping("/vehicles/{vehicleId}/cost-per-km")
    @Operation(summary = "Get fuel cost per km", description = "Calculate fuel cost per kilometer for a vehicle")
    @ApiResponse(responseCode = "200", description = "Cost per km calculated successfully")
    public ResponseEntity<BigDecimal> getFuelCostPerKm(
            @PathVariable Long vehicleId,
            @RequestParam Long fromDate,
            @RequestParam Long toDate) {
        log.info("GET /fuel/vehicles/{}/cost-per-km - Calculating cost per km", vehicleId);
        BigDecimal costPerKm = fuelService.getFuelCostPerKm(vehicleId, fromDate, toDate);
        return ResponseEntity.ok(costPerKm);
    }

    @GetMapping("/vehicles/{vehicleId}/average-price")
    @Operation(summary = "Get average fuel price", description = "Retrieve average fuel price per liter for a vehicle")
    @ApiResponse(responseCode = "200", description = "Average price retrieved successfully")
    public ResponseEntity<BigDecimal> getAverageFuelPrice(
            @PathVariable Long vehicleId,
            @RequestParam Long fromDate,
            @RequestParam Long toDate) {
        log.info("GET /fuel/vehicles/{}/average-price - Retrieving average fuel price", vehicleId);
        BigDecimal avgPrice = fuelService.getAverageFuelPrice(vehicleId, fromDate, toDate);
        return ResponseEntity.ok(avgPrice);
    }

    @GetMapping("/vehicles/{vehicleId}/price-fluctuations")
    @Operation(summary = "Detect price fluctuations", description = "Identify fuel price fluctuations for a vehicle")
    @ApiResponse(responseCode = "200", description = "Price fluctuations detected successfully")
    public ResponseEntity<List<PriceFluctuation>> detectPriceFluctuations(
            @PathVariable Long vehicleId,
            @RequestParam Long fromDate,
            @RequestParam Long toDate) {
        log.info("GET /fuel/vehicles/{}/price-fluctuations - Detecting price fluctuations", vehicleId);
        List<PriceFluctuation> fluctuations = fuelService.detectPriceFluctuations(vehicleId, fromDate, toDate);
        return ResponseEntity.ok(fluctuations);
    }

    @GetMapping("/companies/{companyId}/cheapest-vendor")
    @Operation(summary = "Get cheapest fuel vendor", description = "Identify the cheapest fuel vendor for a company")
    @ApiResponse(responseCode = "200", description = "Vendor retrieved successfully")
    public ResponseEntity<String> getCheapestFuelVendor(@PathVariable Long companyId) {
        log.info("GET /fuel/companies/{}/cheapest-vendor - Retrieving cheapest vendor", companyId);
        String vendor = fuelService.getCheapestFuelVendor(companyId);
        return ResponseEntity.ok(vendor);
    }

    // ==================== Fuel Anomaly Detection ====================

    @GetMapping("/vehicles/{vehicleId}/anomalies")
    @Operation(summary = "Detect fuel anomalies", description = "Identify fuel consumption anomalies for a vehicle")
    @ApiResponse(responseCode = "200", description = "Anomalies detected successfully")
    public ResponseEntity<List<FuelAnomaly>> detectAnomalies(@PathVariable Long vehicleId) {
        log.info("GET /fuel/vehicles/{}/anomalies - Detecting fuel anomalies", vehicleId);
        List<FuelAnomaly> anomalies = fuelService.detectAnomalies(vehicleId);
        return ResponseEntity.ok(anomalies);
    }

    @GetMapping("/vehicles/{vehicleId}/spikes")
    @Operation(summary = "Detect consumption spikes", description = "Identify sudden fuel consumption spikes for a vehicle")
    @ApiResponse(responseCode = "200", description = "Spikes detected successfully")
    public ResponseEntity<List<FuelSpike>> detectConsumptionSpikes(@PathVariable Long vehicleId) {
        log.info("GET /fuel/vehicles/{}/spikes - Detecting consumption spikes", vehicleId);
        List<FuelSpike> spikes = fuelService.detectConsumptionSpikes(vehicleId);
        return ResponseEntity.ok(spikes);
    }

    @GetMapping("/companies/{companyId}/suspicious-logs")
    @Operation(summary = "Get suspicious fuel logs", description = "Retrieve suspicious fuel logs for a company")
    @ApiResponse(responseCode = "200", description = "Suspicious logs retrieved successfully")
    public ResponseEntity<List<SuspiciousFuelLog>> getSuspiciousFuelLogs(@PathVariable Long companyId) {
        log.info("GET /fuel/companies/{}/suspicious-logs - Retrieving suspicious fuel logs", companyId);
        List<SuspiciousFuelLog> suspiciousLogs = fuelService.getSuspiciousFuelLogs(companyId);
        return ResponseEntity.ok(suspiciousLogs);
    }

    @GetMapping("/companies/{companyId}/theft-alerts")
    @Operation(summary = "Get fuel theft alerts", description = "Retrieve fuel theft alerts for a company")
    @ApiResponse(responseCode = "200", description = "Theft alerts retrieved successfully")
    public ResponseEntity<List<FuelTheftAlert>> getFuelTheftAlerts(@PathVariable Long companyId) {
        log.info("GET /fuel/companies/{}/theft-alerts - Retrieving fuel theft alerts", companyId);
        List<FuelTheftAlert> alerts = fuelService.getFuelTheftAlerts(companyId);
        return ResponseEntity.ok(alerts);
    }

    // ==================== Fleet Fuel Analysis ====================

    @GetMapping("/companies/{companyId}/statistics")
    @Operation(summary = "Get fleet fuel statistics", description = "Retrieve comprehensive fuel statistics for a fleet")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    public ResponseEntity<FleetFuelStatistics> getFleetFuelStatistics(
            @PathVariable Long companyId,
            @RequestParam Long fromDate,
            @RequestParam Long toDate) {
        log.info("GET /fuel/companies/{}/statistics - Retrieving fleet fuel statistics", companyId);
        FleetFuelStatistics stats = fuelService.getFleetFuelStatistics(companyId, fromDate, toDate);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/companies/{companyId}/statistics-by-category")
    @Operation(summary = "Get fuel stats by category", description = "Retrieve fuel statistics grouped by vehicle category")
    @ApiResponse(responseCode = "200", description = "Category statistics retrieved successfully")
    public ResponseEntity<List<CategoryFuelStats>> getFuelStatsByCategory(
            @PathVariable Long companyId,
            @RequestParam Long fromDate,
            @RequestParam Long toDate) {
        log.info("GET /fuel/companies/{}/statistics-by-category - Retrieving category statistics", companyId);
        List<CategoryFuelStats> stats = fuelService.getFuelStatsByCategory(companyId, fromDate, toDate);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/companies/{companyId}/high-consumption")
    @Operation(summary = "Get high consumption vehicles", description = "Retrieve vehicles with high fuel consumption")
    @ApiResponse(responseCode = "200", description = "High consumption vehicles retrieved successfully")
    public ResponseEntity<List<VehicleWithHighConsumption>> getHighConsumptionVehicles(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "15") double threshold) {
        log.info("GET /fuel/companies/{}/high-consumption - Retrieving high consumption vehicles", companyId);
        List<VehicleWithHighConsumption> vehicles = fuelService.getHighConsumptionVehicles(companyId, threshold);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/companies/{companyId}/optimization-tips")
    @Operation(summary = "Get fuel optimization tips", description = "Retrieve fuel optimization recommendations")
    @ApiResponse(responseCode = "200", description = "Optimization tips retrieved successfully")
    public ResponseEntity<List<FuelOptimizationTip>> getFuelOptimizationTips(@PathVariable Long companyId) {
        log.info("GET /fuel/companies/{}/optimization-tips - Retrieving optimization tips", companyId);
        List<FuelOptimizationTip> tips = fuelService.getFuelOptimizationTips(companyId);
        return ResponseEntity.ok(tips);
    }

    @GetMapping("/companies/{companyId}/potential-savings")
    @Operation(summary = "Calculate potential savings", description = "Calculate potential fuel savings for a fleet")
    @ApiResponse(responseCode = "200", description = "Potential savings calculated successfully")
    public ResponseEntity<BigDecimal> calculatePotentialSavings(@PathVariable Long companyId) {
        log.info("GET /fuel/companies/{}/potential-savings - Calculating potential savings", companyId);
        BigDecimal savings = fuelService.calculatePotentialSavings(companyId);
        return ResponseEntity.ok(savings);
    }

    // ==================== Fuel Budget Management ====================

    @PostMapping("/vehicles/{vehicleId}/budget")
    @Operation(summary = "Set fuel budget", description = "Set monthly fuel budget for a vehicle")
    @ApiResponse(responseCode = "201", description = "Budget set successfully")
    public ResponseEntity<Void> setFuelBudget(
            @PathVariable Long vehicleId,
            @RequestParam BigDecimal monthlyBudget) {
        log.info("POST /fuel/vehicles/{}/budget - Setting fuel budget", vehicleId);
        fuelService.setFuelBudget(vehicleId, monthlyBudget);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/vehicles/{vehicleId}/budget-status")
    @Operation(summary = "Get fuel budget status", description = "Retrieve fuel budget status for a vehicle")
    @ApiResponse(responseCode = "200", description = "Budget status retrieved successfully")
    public ResponseEntity<FuelBudgetStatus> getFuelBudgetStatus(@PathVariable Long vehicleId) {
        log.info("GET /fuel/vehicles/{}/budget-status - Retrieving budget status", vehicleId);
        FuelBudgetStatus status = fuelService.getFuelBudgetStatus(vehicleId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/vehicles/{vehicleId}/over-budget")
    @Operation(summary = "Check if over budget", description = "Check if vehicle is over fuel budget")
    @ApiResponse(responseCode = "200", description = "Budget check completed successfully")
    public ResponseEntity<Boolean> isOverBudget(@PathVariable Long vehicleId) {
        log.info("GET /fuel/vehicles/{}/over-budget - Checking budget status", vehicleId);
        boolean overBudget = fuelService.isOverBudget(vehicleId);
        return ResponseEntity.ok(overBudget);
    }

    @GetMapping("/companies/{companyId}/approaching-budget-limit")
    @Operation(summary = "Get vehicles approaching budget limit", description = "Retrieve vehicles approaching fuel budget limit")
    @ApiResponse(responseCode = "200", description = "Vehicles retrieved successfully")
    public ResponseEntity<List<Long>> getVehiclesApproachingBudgetLimit(@PathVariable Long companyId) {
        log.info("GET /fuel/companies/{}/approaching-budget-limit - Retrieving vehicles approaching budget", companyId);
        List<Long> vehicles = fuelService.getVehiclesApproachingBudgetLimit(companyId);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/companies/{companyId}/budget-comparison")
    @Operation(summary = "Get fleet budget comparison", description = "Retrieve fleet-wide fuel budget vs actual comparison")
    @ApiResponse(responseCode = "200", description = "Budget comparison retrieved successfully")
    public ResponseEntity<BudgetComparison> getFleetBudgetComparison(@PathVariable Long companyId) {
        log.info("GET /fuel/companies/{}/budget-comparison - Retrieving fleet budget comparison", companyId);
        BudgetComparison comparison = fuelService.getFleetBudgetComparison(companyId);
        return ResponseEntity.ok(comparison);
    }

    // ==================== Fuel Vendor Management ====================

    @GetMapping("/companies/{companyId}/vendors/{vendor}/statistics")
    @Operation(summary = "Get vendor statistics", description = "Retrieve fuel statistics for a specific vendor")
    @ApiResponse(responseCode = "200", description = "Vendor statistics retrieved successfully")
    public ResponseEntity<VendorFuelStatistics> getVendorStatistics(
            @PathVariable Long companyId,
            @PathVariable String vendor) {
        log.info("GET /fuel/companies/{}/vendors/{}/statistics - Retrieving vendor statistics", companyId, vendor);
        VendorFuelStatistics stats = fuelService.getVendorStatistics(companyId, vendor);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/companies/{companyId}/vendor-comparison")
    @Operation(summary = "Compare fuel vendors", description = "Compare fuel vendors for a company")
    @ApiResponse(responseCode = "200", description = "Vendor comparison retrieved successfully")
    public ResponseEntity<List<VendorComparison>> compareVendors(
            @PathVariable Long companyId,
            @RequestParam Long fromDate,
            @RequestParam Long toDate) {
        log.info("GET /fuel/companies/{}/vendor-comparison - Comparing vendors", companyId);
        List<VendorComparison> comparison = fuelService.compareVendors(companyId, fromDate, toDate);
        return ResponseEntity.ok(comparison);
    }

    @GetMapping("/vendors/{vendor}/rating")
    @Operation(summary = "Get vendor rating", description = "Retrieve performance rating for a fuel vendor")
    @ApiResponse(responseCode = "200", description = "Vendor rating retrieved successfully")
    public ResponseEntity<Double> getVendorRating(@PathVariable String vendor) {
        log.info("GET /fuel/vendors/{}/rating - Retrieving vendor rating", vendor);
        Double rating = fuelService.getVendorRating(vendor);
        return ResponseEntity.ok(rating);
    }

    // ==================== Reporting & Export ====================

    @GetMapping("/companies/{companyId}/report")
    @Operation(summary = "Generate fuel report", description = "Generate comprehensive fuel consumption report")
    @ApiResponse(responseCode = "200", description = "Report generated successfully")
    public ResponseEntity<String> generateFuelReport(
            @PathVariable Long companyId,
            @RequestParam Long fromDate,
            @RequestParam Long toDate) {
        log.info("GET /fuel/companies/{}/report - Generating fuel report", companyId);
        String report = fuelService.generateFuelReport(companyId, fromDate, toDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/vehicles/{vehicleId}/export-csv")
    @Operation(summary = "Export fuel logs to CSV", description = "Export fuel logs for a vehicle as CSV file")
    @ApiResponse(responseCode = "200", description = "CSV exported successfully")
    public ResponseEntity<byte[]> exportFuelLogsToCSV(
            @PathVariable Long vehicleId,
            @RequestParam Long fromDate,
            @RequestParam Long toDate) {
        log.info("GET /fuel/vehicles/{}/export-csv - Exporting fuel logs to CSV", vehicleId);
        byte[] csv = fuelService.exportFuelLogsToCSV(vehicleId, fromDate, toDate);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"fuel_logs.csv\"")
                .body(csv);
    }

    @GetMapping("/companies/{companyId}/cost-analysis-report")
    @Operation(summary = "Generate cost analysis report", description = "Generate fuel cost analysis report")
    @ApiResponse(responseCode = "200", description = "Cost analysis report generated successfully")
    public ResponseEntity<String> generateCostAnalysisReport(
            @PathVariable Long companyId,
            @RequestParam Long fromDate,
            @RequestParam Long toDate) {
        log.info("GET /fuel/companies/{}/cost-analysis-report - Generating cost analysis report", companyId);
        String report = fuelService.generateCostAnalysisReport(companyId, fromDate, toDate);
        return ResponseEntity.ok(report);
    }
}
