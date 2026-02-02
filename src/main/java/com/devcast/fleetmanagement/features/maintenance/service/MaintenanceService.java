package com.devcast.fleetmanagement.features.maintenance.service;

import com.devcast.fleetmanagement.features.maintenance.model.MaintenanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Maintenance Service Interface
 * Handles vehicle maintenance scheduling, tracking, and cost management
 */
public interface MaintenanceService {

    // ==================== Maintenance Record Operations ====================

    /**
     * Create maintenance record
     */
    MaintenanceRecord createMaintenanceRecord(Long vehicleId, MaintenanceRecord record);

    /**
     * Get maintenance record
     */
    Optional<MaintenanceRecord> getMaintenanceRecord(Long recordId);

    /**
     * Update maintenance record
     */
    MaintenanceRecord updateMaintenanceRecord(Long recordId, MaintenanceRecord record);

    /**
     * Complete maintenance
     */
    MaintenanceRecord completeMaintenanceRecord(Long recordId, String notes, BigDecimal actualCost);

    /**
     * Cancel maintenance
     */
    void cancelMaintenanceRecord(Long recordId, String reason);

    /**
     * Get maintenance records for vehicle
     */
    Page<MaintenanceRecord> getMaintenanceRecords(Long vehicleId, Pageable pageable);

    /**
     * Get maintenance records by date range
     */
    List<MaintenanceRecord> getMaintenanceRecordsByPeriod(Long vehicleId, Long fromDate, Long toDate);

    /**
     * Get all pending maintenance
     */
    List<MaintenanceRecord> getPendingMaintenance(Long companyId);

    /**
     * Get maintenance records by status
     */
    Page<MaintenanceRecord> getMaintenanceByStatus(Long companyId, String status, Pageable pageable);

    // ==================== Maintenance Scheduling ====================

    /**
     * Get maintenance schedule for vehicle
     */
    List<MaintenanceSchedule> getMaintenanceSchedule(Long vehicleId);

    /**
     * Get overdue maintenance items
     */
    List<OverdueMaintenanceItem> getOverdueMaintenanceItems(Long companyId);

    /**
     * Get maintenance due soon
     */
    List<UpcomingMaintenanceItem> getUpcomingMaintenanceItems(Long companyId, int daysAhead);

    /**
     * Create preventive maintenance
     */
    MaintenanceRecord schedulePreventiveMaintenance(Long vehicleId, String serviceType);

    /**
     * Get vehicles needing maintenance
     */
    List<Long> getVehiclesNeedingMaintenance(Long companyId);

    /**
     * Check vehicle maintenance status
     */
    MaintenanceStatus getMaintenanceStatus(Long vehicleId);

    // ==================== Service Types Management ====================

    /**
     * Get available service types
     */
    List<ServiceType> getAvailableServiceTypes();

    /**
     * Get service maintenance interval
     */
    Optional<Integer> getMaintenanceInterval(String serviceType);

    /**
     * Get service cost estimate
     */
    BigDecimal getServiceCostEstimate(String serviceType);

    // ==================== Maintenance Cost Analysis ====================

    /**
     * Get total maintenance cost for vehicle
     */
    BigDecimal getTotalMaintenanceCost(Long vehicleId, Long fromDate, Long toDate);

    /**
     * Get maintenance cost per kilometer
     */
    BigDecimal getMaintenanceCostPerKm(Long vehicleId);

    /**
     * Get cost breakdown by service type
     */
    List<ServiceCostBreakdown> getCostBreakdownByServiceType(Long vehicleId);

    /**
     * Get average maintenance cost
     */
    BigDecimal getAverageMaintenanceCost(Long companyId);

    /**
     * Get highest maintenance cost vehicles
     */
    List<VehicleMaintenanceCost> getHighestMaintenanceCostVehicles(Long companyId, int limit);

    /**
     * Detect unusual maintenance costs
     */
    List<UnusualMaintenanceCost> detectUnusualCosts(Long companyId);

    /**
     * Compare maintenance costs across vehicles
     */
    List<MaintenanceCostComparison> compareMaintenanceCosts(Long companyId);

    // ==================== Parts & Inventory ====================

    /**
     * Add parts to maintenance record
     */
    void addPartToMaintenance(Long recordId, String partName, int quantity, BigDecimal cost);

    /**
     * Get parts used in maintenance
     */
    List<MaintenancePart> getMaintenanceParts(Long recordId);

    /**
     * Remove part from maintenance
     */
    void removePartFromMaintenance(Long recordId, Long partId);

    /**
     * Get parts inventory status
     */
    PartsInventoryStatus getPartsInventoryStatus(Long companyId);

    // ==================== Vendor Management ====================

    /**
     * Get maintenance vendors
     */
    List<MaintenanceVendor> getMaintenanceVendors(Long companyId);

    /**
     * Assign vendor to maintenance
     */
    void assignVendor(Long recordId, String vendorName);

    /**
     * Get vendor performance rating
     */
    VendorPerformance getVendorPerformance(String vendorName);

    /**
     * Compare vendors
     */
    List<VendorComparison> compareVendors(Long companyId);

    /**
     * Get vendor cost comparison
     */
    List<VendorCostComparison> getVendorCostComparison(String serviceType);

    // ==================== Compliance & Certifications ====================

    /**
     * Check vehicle compliance
     */
    ComplianceStatus checkVehicleCompliance(Long vehicleId);

    /**
     * Get compliance inspection records
     */
    List<ComplianceInspection> getComplianceInspections(Long vehicleId);

    /**
     * Generate compliance certificate
     */
    byte[] generateComplianceCertificate(Long vehicleId);

    /**
     * Get vehicles with compliance issues
     */
    List<Long> getVehiclesWithComplianceIssues(Long companyId);

    // ==================== Reporting & Analytics ====================

    /**
     * Get maintenance summary report
     */
    MaintenanceSummaryReport getMaintenanceSummary(Long companyId, Long fromDate, Long toDate);

    /**
     * Get maintenance trend analysis
     */
    List<MaintenanceTrend> getMaintenanceTrends(Long vehicleId, int months);

    /**
     * Get fleet maintenance health
     */
    FleetMaintenanceHealth getFleetMaintenanceHealth(Long companyId);

    /**
     * Get maintenance recommendations
     */
    List<MaintenanceRecommendation> getMaintenanceRecommendations(Long companyId);

    /**
     * Export maintenance records to CSV
     */
    byte[] exportMaintenanceToCSV(Long companyId, Long fromDate, Long toDate);

    /**
     * Generate maintenance report
     */
    String generateMaintenanceReport(Long companyId, Long fromDate, Long toDate);

    // Data Transfer Objects

    record MaintenanceSchedule(
            Long vehicleId,
            String serviceType,
            Long nextDueKilometers,
            Long nextDueDate,
            String status
    ) {}

    record OverdueMaintenanceItem(
            Long vehicleId,
            String registrationNumber,
            String serviceType,
            Long overdueSince,
            String urgency
    ) {}

    record UpcomingMaintenanceItem(
            Long vehicleId,
            String registrationNumber,
            String serviceType,
            Long dueDate,
            int daysUntilDue
    ) {}

    record MaintenanceStatus(
            Long vehicleId,
            String overallStatus,
            List<String> overdueServices,
            List<String> upcomingServices,
            int totalPendingItems
    ) {}

    record ServiceType(
            String serviceTypeId,
            String serviceName,
            String description,
            int intervalMonths,
            int intervalKilometers,
            BigDecimal estimatedCost
    ) {}

    record ServiceCostBreakdown(
            String serviceType,
            BigDecimal totalCost,
            int serviceCount,
            BigDecimal averageCost
    ) {}

    record VehicleMaintenanceCost(
            Long vehicleId,
            String registrationNumber,
            BigDecimal totalCost,
            int serviceCount,
            BigDecimal costPerKm
    ) {}

    record UnusualMaintenanceCost(
            Long vehicleId,
            String registrationNumber,
            String serviceType,
            BigDecimal cost,
            BigDecimal variance,
            String reason
    ) {}

    record MaintenanceCostComparison(
            Long vehicleId,
            String model,
            BigDecimal cost,
            BigDecimal avgCost,
            String status
    ) {}

    record MaintenancePart(
            Long partId,
            String partName,
            int quantity,
            BigDecimal unitCost,
            BigDecimal totalCost
    ) {}

    record PartsInventoryStatus(
            Long companyId,
            int totalParts,
            int lowStockParts,
            BigDecimal inventoryValue
    ) {}

    record MaintenanceVendor(
            String vendorId,
            String vendorName,
            String specialization,
            Double rating,
            int jobsCompleted
    ) {}

    record VendorPerformance(
            String vendorName,
            Double quality,
            Double timeliness,
            Double costEfficiency,
            Double overallRating,
            int jobsCompleted
    ) {}

    record VendorComparison(
            String vendor,
            BigDecimal avgCost,
            Double quality,
            Double timeliness,
            String recommendation
    ) {}

    record VendorCostComparison(
            String vendor,
            String serviceType,
            BigDecimal cost,
            Double quality
    ) {}

    record ComplianceStatus(
            Long vehicleId,
            boolean isCompliant,
            Long lastInspectionDate,
            Long nextInspectionDate,
            List<String> issues
    ) {}

    record ComplianceInspection(
            Long inspectionId,
            Long vehicleId,
            Long inspectionDate,
            String status,
            String remarks
    ) {}

    record MaintenanceSummaryReport(
            Long companyId,
            int totalRecords,
            int completedRecords,
            int pendingRecords,
            BigDecimal totalCost,
            BigDecimal averageCost
    ) {}

    record MaintenanceTrend(
            Long date,
            BigDecimal cost,
            int serviceCount,
            String trend
    ) {}

    record FleetMaintenanceHealth(
            Long companyId,
            String overallHealth,
            Double percentCompliant,
            int vehiclesNeedingService,
            BigDecimal totalPendingCost
    ) {}

    record MaintenanceRecommendation(
            Long vehicleId,
            String serviceType,
            String priority,
            String reason,
            Long estimatedCost
    ) {}
}
