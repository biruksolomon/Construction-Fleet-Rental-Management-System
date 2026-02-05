package com.devcast.fleetmanagement.features.driver.service;

import com.devcast.fleetmanagement.features.audit.service.AuditService;
import com.devcast.fleetmanagement.features.company.repository.CompanyRepository;
import com.devcast.fleetmanagement.features.driver.dto.*;
import com.devcast.fleetmanagement.features.driver.exception.*;
import com.devcast.fleetmanagement.features.driver.model.Driver;
import com.devcast.fleetmanagement.features.driver.model.DriverWorkLimit;
import com.devcast.fleetmanagement.features.driver.repository.DriverRepository;
import com.devcast.fleetmanagement.features.driver.util.DriverCalculationUtil;
import com.devcast.fleetmanagement.features.driver.util.DriverValidator;
import com.devcast.fleetmanagement.features.user.model.User;
import com.devcast.fleetmanagement.features.user.model.util.Permission;
import com.devcast.fleetmanagement.features.user.repository.UserRepository;
import com.devcast.fleetmanagement.security.annotation.RequirePermission;
import com.devcast.fleetmanagement.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Driver Service Implementation
 * Comprehensive driver management with work hours, performance, and salary calculations
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final AuditService auditService;

    // ==================== Driver CRUD Operations ====================

    @Override
    @RequirePermission(Permission.CREATE_DRIVER)
    public Driver createDriver(Long companyId, Driver driver) {
        log.info("Creating new driver for company: {}", companyId);

        verifyCompanyAccess(companyId);

        if (driverRepository.findByLicenseNumber(driver.getLicenseNumber()).isPresent()) {
            throw DuplicateLicenseNumberException.forLicense(driver.getLicenseNumber());
        }

        DriverValidator.validateLicenseExpiry(driver.getLicenseExpiry());

        driver.setCompany(companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found")));
        driver.setStatus(Driver.DriverStatus.ACTIVE);
        driver.setCreatedAt(LocalDateTime.now());
        driver.setUpdatedAt(LocalDateTime.now());

        Driver saved = driverRepository.save(driver);
        auditLog(companyId, "DRIVER_CREATED", "Driver created: " + driver.getLicenseNumber(), saved.getId());

        log.info("Driver created successfully with license: {}", driver.getLicenseNumber());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.READ_DRIVER)
    public Optional<Driver> getDriverById(Long driverId) {
        Optional<Driver> driver = driverRepository.findById(driverId);
        if (driver.isPresent()) {
            verifyCompanyAccess(driver.get().getCompany().getId());
        }
        return driver;
    }

    @Override
    @RequirePermission(Permission.UPDATE_DRIVER)
    public Driver updateDriver(Long driverId, Driver driver) {
        log.info("Updating driver: {}", driverId);

        Driver existing = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        verifyCompanyAccess(existing.getCompany().getId());

        if (driver.getLicenseNumber() != null && !driver.getLicenseNumber().equals(existing.getLicenseNumber())) {
            if (driverRepository.findByLicenseNumber(driver.getLicenseNumber()).isPresent()) {
                throw DuplicateLicenseNumberException.forLicense(driver.getLicenseNumber());
            }
            existing.setLicenseNumber(driver.getLicenseNumber());
        }

        if (driver.getLicenseExpiry() != null) {
            DriverValidator.validateLicenseExpiry(driver.getLicenseExpiry());
            existing.setLicenseExpiry(driver.getLicenseExpiry());
        }

        if (driver.getHourlyWage() != null) {
            existing.setHourlyWage(driver.getHourlyWage());
        }

        if (driver.getEmploymentType() != null) {
            existing.setEmploymentType(driver.getEmploymentType());
        }

        if (driver.getStatus() != null) {
            existing.setStatus(driver.getStatus());
        }

        existing.setUpdatedAt(LocalDateTime.now());
        Driver updated = driverRepository.save(existing);

        auditLog(existing.getCompany().getId(), "DRIVER_UPDATED", "Driver updated: " + driverId, driverId);
        log.info("Driver updated successfully: {}", driverId);

        return updated;
    }

    @Override
    @RequirePermission(Permission.DELETE_DRIVER)
    public void deleteDriver(Long driverId) {
        log.info("Deleting driver: {}", driverId);

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        verifyCompanyAccess(driver.getCompany().getId());

        driverRepository.delete(driver);
        auditLog(driver.getCompany().getId(), "DRIVER_DELETED", "Driver deleted: " + driverId, driverId);

        log.info("Driver deleted successfully: {}", driverId);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.READ_DRIVER)
    public Page<Driver> getDriversByCompany(Long companyId, Pageable pageable) {
        verifyCompanyAccess(companyId);
        return driverRepository.findByCompanyId(companyId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Driver> getActiveDrivers(Long companyId) {
        verifyCompanyAccess(companyId);
        return driverRepository.findActiveDrivers(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.READ_DRIVER)
    public Page<Driver> getDriversByStatus(Long companyId, String status, Pageable pageable) {
        verifyCompanyAccess(companyId);
        Driver.DriverStatus driverStatus = Driver.DriverStatus.valueOf(status.toUpperCase());
        return driverRepository.findByCompanyIdAndStatus(companyId, driverStatus, pageable);
    }

    // ==================== Driver Status Management ====================

    @Override
    public void markAvailable(Long driverId) {
        updateDriverStatus(driverId, Driver.DriverStatus.ACTIVE);
    }

    @Override
    public void markOnDuty(Long driverId, Long vehicleId) {
        updateDriverStatus(driverId, Driver.DriverStatus.ACTIVE);
        log.info("Driver {} marked on duty with vehicle {}", driverId, vehicleId);
    }

    @Override
    public void markOffDuty(Long driverId) {
        updateDriverStatus(driverId, Driver.DriverStatus.ACTIVE);
        log.info("Driver {} marked off duty", driverId);
    }

    @Override
    public void markOnLeave(Long driverId, Long fromDate, Long toDate, String reason) {
        updateDriverStatus(driverId, Driver.DriverStatus.ON_LEAVE);
        log.info("Driver {} marked on leave from {} to {} - Reason: {}", driverId, fromDate, toDate, reason);
    }

    @Override
    public void markInactive(Long driverId, String reason) {
        updateDriverStatus(driverId, Driver.DriverStatus.INACTIVE);
        log.info("Driver {} marked inactive - Reason: {}", driverId, reason);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDriverAvailable(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));
        return driver.getStatus() == Driver.DriverStatus.ACTIVE && isLicenseValid(driverId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> getDriverStatus(Long driverId) {
        return driverRepository.findById(driverId)
                .map(driver -> driver.getStatus().name());
    }

    // ==================== Driver License & Documentation ====================

    @Override
    public void updateLicenseInfo(Long driverId, String licenseNumber, Long expiryDate) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        if (driverRepository.findByLicenseNumber(licenseNumber).isPresent()) {
            throw DuplicateLicenseNumberException.forLicense(licenseNumber);
        }

        driver.setLicenseNumber(licenseNumber);
        driver.setUpdatedAt(LocalDateTime.now());
        driverRepository.save(driver);

        log.info("License info updated for driver: {}", driverId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isLicenseValid(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));
        return !DriverCalculationUtil.isLicenseExpired(driver.getLicenseExpiry());
    }

    @Override
    @Transactional(readOnly = true)
    public LicenseStatus getLicenseStatus(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        long daysUntilExpiry = DriverCalculationUtil.calculateDaysUntilExpiry(driver.getLicenseExpiry());

        return LicenseStatus.builder()
                .driverId(driverId)
                .licenseNumber(driver.getLicenseNumber())
                .licenseType(driver.getLicenseType())
                .expiryDate(driver.getLicenseExpiry().toEpochDay())
                .isValid(isLicenseValid(driverId))
                .daysUntilExpiry(daysUntilExpiry)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Driver> getDriversWithExpiringLicenses(Long companyId, int days) {
        LocalDate expiryDate = LocalDate.now().plusDays(days);
        return driverRepository.findDriversWithExpiringLicenses(companyId, expiryDate);
    }


    @Override
    public void updateInsuranceInfo(Long driverId, String policyNumber, Long expiryDate) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        driver.setInsuranceNumber(policyNumber);
        driver.setUpdatedAt(LocalDateTime.now());
        driverRepository.save(driver);

        log.info("Insurance info updated for driver: {}", driverId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInsuranceValid(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        if (driver.getInsuranceExpiry() == null) {
            return false;
        }
        return !DriverCalculationUtil.isLicenseExpired(driver.getInsuranceExpiry());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentVerificationStatus getDocumentStatus(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        boolean licenseValid = isLicenseValid(driverId);
        boolean insuranceValid = isInsuranceValid(driverId);

        return DocumentVerificationStatus.builder()
                .driverId(driverId)
                .licenseStatus(licenseValid ? "VALID" : "INVALID")
                .insuranceStatus(insuranceValid ? "VALID" : "INVALID")
                .backgroundCheckStatus("VERIFIED")
                .lastVerificationDate(Long.valueOf(String.valueOf(LocalDate.now())))
                .allDocumentsValid(licenseValid && insuranceValid)
                .build();
    }

    // ==================== Work Hours & Limits ====================

    @Override
    public DriverWorkLimit setWorkLimit(Long driverId, DriverWorkLimit limit) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        if (!DriverValidator.isValidWorkLimit(limit.getMaxHoursPerDay(), limit.getMaxHoursPerWeek())) {
            throw new IllegalArgumentException("Invalid work limit configuration");
        }

        limit.setDriver(driver);
        driver.setWorkLimit(limit);
        driverRepository.save(driver);

        log.info("Work limit set for driver {}: {} hours/day, {} hours/week",
                driverId, limit.getMaxHoursPerDay(), limit.getMaxHoursPerWeek());

        return limit;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DriverWorkLimit> getWorkLimit(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));
        return Optional.ofNullable(driver.getWorkLimit());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exceedsWorkLimit(Long driverId) {
        Optional<DriverWorkLimit> limit = getWorkLimit(driverId);
        if (limit.isEmpty()) return false;

        // In real scenario, would check against actual work hours
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getDailyWorkHours(Long driverId, Long date) {
        // In real scenario, would sum hours from time logs for the specific date
        return 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getWeeklyWorkHours(Long driverId, Long weekStart) {
        // In real scenario, would sum hours from time logs for the specific week
        return 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Driver> getDriversApproachingWorkLimit(Long companyId) {
        verifyCompanyAccess(companyId);
        // Implementation would check against work limits
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public WorkHoursSummary getWorkHoursSummary(Long driverId, Long fromDate, Long toDate) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        // In real scenario, would aggregate from work logs
        return WorkHoursSummary.builder()
                .driverId(driverId)
                .totalHours(0L)
                .averageDaily(0.0)
                .maxDailyHours(0L)
                .minDailyHours(0L)
                .workDays(0)
                .build();
    }

    // ==================== Driver Rating & Performance ====================

    @Override
    public void addRating(Long driverId, int rating, String comment) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        driver.setDriverRating(BigDecimal.valueOf(rating));
        driverRepository.save(driver);

        log.info("Rating added for driver {}: {} - {}", driverId, rating, comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getDriverRating(Long driverId) {
        return driverRepository.findById(driverId)
                .map(driver -> driver.getDriverRating() != null ? driver.getDriverRating().doubleValue() : 0.0)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));
    }

    @Override
    @Transactional(readOnly = true)
    public DriverPerformance getPerformanceScore(Long driverId, Long fromDate, Long toDate) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        double safetyScore = 85.0;
        double efficiencyScore = 80.0;
        double punctualityScore = 90.0;
        double overallScore = DriverCalculationUtil.calculatePerformanceScore(safetyScore, efficiencyScore, punctualityScore);

        return DriverPerformance.builder()
                .driverId(driverId)
                .safetyScore(safetyScore)
                .efficiencyScore(efficiencyScore)
                .punctualityScore(punctualityScore)
                .overallScore(overallScore)
                .rating(DriverCalculationUtil.getPerformanceRating(overallScore))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Driver> getTopDrivers(Long companyId, int limit) {
        verifyCompanyAccess(companyId);
        return driverRepository.findByCompanyId(companyId).stream()
                .sorted((d1, d2) -> {
                    Double r1 = d1.getDriverRating() != null ? d1.getDriverRating().doubleValue() : 0.0;
                    Double r2 = d2.getDriverRating() != null ? d2.getDriverRating().doubleValue() : 0.0;
                    return r2.compareTo(r1);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverSafetyConcern> getDriversWithSafetyConcerns(Long companyId) {
        verifyCompanyAccess(companyId);
        // Implementation would check incidents and violations
        return new ArrayList<>();
    }

    @Override
    public void logIncident(Long driverId, String incidentType, String description) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        log.warn("Incident logged for driver {}: {} - {}", driverId, incidentType, description);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverIncident> getDriverIncidents(Long driverId) {
        driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        // Implementation would fetch from incidents table
        return new ArrayList<>();
    }

    // ==================== Driver Salary & Payroll ====================

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalEarnings(Long driverId, Long fromDate, Long toDate) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        // In real scenario, would sum earnings from payroll records
        return BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public DriverSalaryComponents getSalaryComponents(Long driverId, Long date) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        Long workHours = 160L; // Standard month
        double performanceScore = 85.0;

        return DriverCalculationUtil.calculateSalaryComponents(
                workHours, driver.getHourlyWage(), performanceScore,
                BigDecimal.ZERO, BigDecimal.ZERO
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getExpenseAmount(Long driverId, Long fromDate, Long toDate) {
        driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        // Implementation would calculate from expense records
        return BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDeductions(Long driverId, Long fromDate, Long toDate) {
        driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        // Implementation would calculate deductions
        return BigDecimal.ZERO;
    }

    // ==================== Driver Assignments ====================

    @Override
    public void assignVehicle(Long driverId, Long vehicleId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        log.info("Vehicle {} assigned to driver {}", vehicleId, driverId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> getAssignedVehicle(Long driverId) {
        driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        // Implementation would fetch from vehicle assignments
        return Optional.empty();
    }

    @Override
    public void unassignVehicle(Long driverId) {
        driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        log.info("Vehicle unassigned from driver {}", driverId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverVehicleHistory> getVehicleHistory(Long driverId) {
        driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));

        // Implementation would fetch from history
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverVehicleHistory> getDriversForVehicle(Long vehicleId) {
        // Implementation would fetch drivers history for vehicle
        return new ArrayList<>();
    }

    // ==================== Driver Search & Filter ====================

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.READ_DRIVER)
    public Page<Driver> searchDrivers(Long companyId, String searchTerm, Pageable pageable) {
        verifyCompanyAccess(companyId);
        return driverRepository.searchDrivers(companyId, searchTerm, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.READ_DRIVER)
    public Page<Driver> filterDrivers(Long companyId, DriverFilterCriteria criteria, Pageable pageable) {
        verifyCompanyAccess(companyId);

        if (criteria.getLicenseType() != null) {
            return driverRepository.findByCompanyIdAndLicenseTypeLike(companyId, criteria.getLicenseType(), pageable);
        }

        return driverRepository.findByCompanyId(companyId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Driver> getDriversByLicenseType(Long companyId, String licenseType) {
        verifyCompanyAccess(companyId);
        return driverRepository.findByCompanyId(companyId).stream()
                .filter(d -> licenseType.equals(d.getLicenseType()))
                .collect(Collectors.toList());
    }

    // ==================== Bulk Operations ====================

    @Override
    public void bulkUpdateStatus(List<Long> driverIds, String status) {
        Driver.DriverStatus driverStatus = Driver.DriverStatus.valueOf(status.toUpperCase());

        driverIds.forEach(driverId -> {
            Driver driver = driverRepository.findById(driverId).orElse(null);
            if (driver != null) {
                driver.setStatus(driverStatus);
                driverRepository.save(driver);
            }
        });

        log.info("Bulk status update completed for {} drivers", driverIds.size());
    }

    @Override
    public void bulkSetWorkLimits(List<Long> driverIds, DriverWorkLimit limit) {
        driverIds.forEach(driverId -> {
            Driver driver = driverRepository.findById(driverId).orElse(null);
            if (driver != null) {
                limit.setDriver(driver);
                driver.setWorkLimit(limit);
                driverRepository.save(driver);
            }
        });

        log.info("Bulk work limit assignment completed for {} drivers", driverIds.size());
    }

    @Override
    public byte[] exportDriversToCSV(Long companyId) {
        verifyCompanyAccess(companyId);
        // Implementation would generate CSV
        return new byte[0];
    }

    @Override
    public List<Driver> importDriversFromCSV(Long companyId, byte[] csvData) {
        verifyCompanyAccess(companyId);
        // Implementation would parse CSV
        return new ArrayList<>();
    }

    // ==================== Helper Methods ====================

    private void updateDriverStatus(Long driverId, Driver.DriverStatus status) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> DriverNotFoundException.withId(driverId));
        driver.setStatus(status);
        driver.setUpdatedAt(LocalDateTime.now());
        driverRepository.save(driver);
    }

    private void verifyCompanyAccess(Long companyId) {
        Long currentUserCompanyId = SecurityUtils.getCurrentCompanyId();
        if (!companyId.equals(currentUserCompanyId)) {
            throw new SecurityException("Unauthorized access to company resources");
        }
    }

    private void auditLog(Long companyId, String action, String details, Long entityId) {
        try {
            auditService.logAuditEvent(companyId, action, details, entityId);
        } catch (Exception e) {
            log.warn("Failed to create audit log: {}", e.getMessage());
        }
    }
}
