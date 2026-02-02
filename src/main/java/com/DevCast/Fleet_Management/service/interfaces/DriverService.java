package com.DevCast.Fleet_Management.service.interfaces;


import com.DevCast.Fleet_Management.model.Driver;
import com.DevCast.Fleet_Management.model.DriverWorkLimit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Driver Service Interface
 * Handles driver management, work hours, ratings, and performance tracking
 */
public interface DriverService {

    // ==================== Driver CRUD Operations ====================

    /**
     * Create new driver
     */
    Driver createDriver(Long companyId, Driver driver);

    /**
     * Get driver by ID
     */
    Optional<Driver> getDriverById(Long driverId);

    /**
     * Update driver details
     */
    Driver updateDriver(Long driverId, Driver driver);

    /**
     * Delete driver
     */
    void deleteDriver(Long driverId);

    /**
     * Get all drivers in company
     */
    Page<Driver> getDriversByCompany(Long companyId, Pageable pageable);

    /**
     * Get active drivers
     */
    List<Driver> getActiveDrivers(Long companyId);

    /**
     * Get drivers by status
     */
    Page<Driver> getDriversByStatus(Long companyId, String status, Pageable pageable);

    // ==================== Driver Status Management ====================

    /**
     * Mark driver as available
     */
    void markAvailable(Long driverId);

    /**
     * Mark driver as on-duty
     */
    void markOnDuty(Long driverId, Long vehicleId);

    /**
     * Mark driver as off-duty
     */
    void markOffDuty(Long driverId);

    /**
     * Mark driver as on-leave
     */
    void markOnLeave(Long driverId, Long fromDate, Long toDate, String reason);

    /**
     * Mark driver as inactive
     */
    void markInactive(Long driverId, String reason);

    /**
     * Check if driver is available
     */
    boolean isDriverAvailable(Long driverId);

    /**
     * Get current driver status
     */
    Optional<String> getDriverStatus(Long driverId);

    // ==================== Driver License & Documentation ====================

    /**
     * Update license information
     */
    void updateLicenseInfo(Long driverId, String licenseNumber, Long expiryDate);

    /**
     * Check license validity
     */
    boolean isLicenseValid(Long driverId);

    /**
     * Get license expiry status
     */
    LicenseStatus getLicenseStatus(Long driverId);

    /**
     * Get drivers with expiring licenses
     */
    List<Driver> getDriversWithExpiringLicenses(Long companyId, int daysFromNow);

    /**
     * Update insurance information
     */
    void updateInsuranceInfo(Long driverId, String policyNumber, Long expiryDate);

    /**
     * Check insurance validity
     */
    boolean isInsuranceValid(Long driverId);

    /**
     * Get document verification status
     */
    DocumentVerificationStatus getDocumentStatus(Long driverId);

    // ==================== Work Hours & Limits ====================

    /**
     * Set work hours limit
     */
    DriverWorkLimit setWorkLimit(Long driverId, DriverWorkLimit limit);

    /**
     * Get work hours limit
     */
    Optional<DriverWorkLimit> getWorkLimit(Long driverId);

    /**
     * Check if driver exceeds work limit
     */
    boolean exceedsWorkLimit(Long driverId);

    /**
     * Get daily work hours
     */
    Long getDailyWorkHours(Long driverId, Long date);

    /**
     * Get weekly work hours
     */
    Long getWeeklyWorkHours(Long driverId, Long weekStart);

    /**
     * Alert drivers approaching work limit
     */
    List<Driver> getDriversApproachingWorkLimit(Long companyId);

    /**
     * Get work hours summary
     */
    WorkHoursSummary getWorkHoursSummary(Long driverId, Long fromDate, Long toDate);

    // ==================== Driver Rating & Performance ====================

    /**
     * Add rating to driver
     */
    void addRating(Long driverId, int rating, String comment);

    /**
     * Get driver rating
     */
    Double getDriverRating(Long driverId);

    /**
     * Get driver performance score
     */
    DriverPerformance getPerformanceScore(Long driverId, Long fromDate, Long toDate);

    /**
     * Get top drivers by rating
     */
    List<Driver> getTopDrivers(Long companyId, int limit);

    /**
     * Get drivers with safety concerns
     */
    List<DriverSafetyConcern> getDriversWithSafetyConcerns(Long companyId);

    /**
     * Log driver incident
     */
    void logIncident(Long driverId, String incidentType, String description);

    /**
     * Get driver incidents
     */
    List<DriverIncident> getDriverIncidents(Long driverId);

    // ==================== Driver Salary & Payroll ====================

    /**
     * Get driver total earnings
     */
    BigDecimal getTotalEarnings(Long driverId, Long fromDate, Long toDate);

    /**
     * Get driver salary components
     */
    DriverSalaryComponents getSalaryComponents(Long driverId, Long date);

    /**
     * Get driver expense report
     */
    BigDecimal getExpenseAmount(Long driverId, Long fromDate, Long toDate);

    /**
     * Calculate driver deductions
     */
    BigDecimal calculateDeductions(Long driverId, Long fromDate, Long toDate);

    // ==================== Driver Assignments ====================

    /**
     * Assign vehicle to driver
     */
    void assignVehicle(Long driverId, Long vehicleId);

    /**
     * Get assigned vehicle
     */
    Optional<Long> getAssignedVehicle(Long driverId);

    /**
     * Unassign vehicle
     */
    void unassignVehicle(Long driverId);

    /**
     * Get driver vehicle history
     */
    List<DriverVehicleHistory> getVehicleHistory(Long driverId);

    /**
     * Get vehicle drivers history
     */
    List<DriverVehicleHistory> getDriversForVehicle(Long vehicleId);

    // ==================== Driver Search & Filter ====================

    /**
     * Search drivers
     */
    Page<Driver> searchDrivers(Long companyId, String searchTerm, Pageable pageable);

    /**
     * Filter drivers by criteria
     */
    Page<Driver> filterDrivers(Long companyId, DriverFilterCriteria criteria, Pageable pageable);

    /**
     * Get drivers by license type
     */
    List<Driver> getDriversByLicenseType(Long companyId, String licenseType);

    // ==================== Bulk Operations ====================

    /**
     * Bulk update driver status
     */
    void bulkUpdateStatus(List<Long> driverIds, String status);

    /**
     * Bulk assign work limits
     */
    void bulkSetWorkLimits(List<Long> driverIds, DriverWorkLimit limit);

    /**
     * Export drivers to CSV
     */
    byte[] exportDriversToCSV(Long companyId);

    /**
     * Import drivers from file
     */
    List<Driver> importDriversFromCSV(Long companyId, byte[] csvData);

    // Data Transfer Objects

    record LicenseStatus(
            Long driverId,
            String licenseNumber,
            String licenseType,
            Long expiryDate,
            boolean isValid,
            Long daysUntilExpiry
    ) {}

    record DocumentVerificationStatus(
            Long driverId,
            String licenseStatus,
            String insuranceStatus,
            String backgroundCheckStatus,
            Long lastVerificationDate,
            boolean allDocumentsValid
    ) {}

    record WorkHoursSummary(
            Long driverId,
            Long totalHours,
            Double averageDaily,
            Long maxDailyHours,
            Long minDailyHours,
            int workDays
    ) {}

    record DriverPerformance(
            Long driverId,
            Double safetyScore,
            Double efficiencyScore,
            Double punctualityScore,
            Double overallScore,
            String rating
    ) {}

    record DriverSafetyConcern(
            Long driverId,
            String driverName,
            int incidentCount,
            String recentIncident,
            String recommendedAction
    ) {}

    record DriverIncident(
            Long incidentId,
            Long driverId,
            String type,
            String description,
            Long date,
            String severity
    ) {}

    record DriverSalaryComponents(
            Long driverId,
            BigDecimal baseSalary,
            BigDecimal workHourBonus,
            BigDecimal performanceBonus,
            BigDecimal incentives,
            BigDecimal deductions,
            BigDecimal netSalary
    ) {}

    record DriverVehicleHistory(
            Long driverId,
            String driverName,
            Long vehicleId,
            String registrationNumber,
            Long assignmentDate,
            Long endDate,
            String status
    ) {}

    record DriverFilterCriteria(
            String status,
            String licenseType,
            Double minRating,
            Long fromDate,
            Long toDate
    ) {}
}
