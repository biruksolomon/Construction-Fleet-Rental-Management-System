package com.devcast.fleetmanagement.features.driver.service;

import com.devcast.fleetmanagement.features.driver.model.Driver;
import com.devcast.fleetmanagement.features.driver.model.DriverAssignmentHistory;
import com.devcast.fleetmanagement.features.driver.repository.DriverAssignmentHistoryRepository;
import com.devcast.fleetmanagement.features.driver.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Driver Validation Service
 * Handles driver status validation, suspension checks, and assignment conflict detection
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DriverValidationService {

    private final DriverRepository driverRepository;
    private final DriverAssignmentHistoryRepository assignmentHistoryRepository;

    /**
     * Validate that driver can be assigned to a rental
     */
    public void validateDriverForAssignment(Long driverId, LocalDate startDate, LocalDate endDate) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        // Check if driver is suspended
        if (driver.getStatus().equals(Driver.DriverStatus.SUSPENDED) ) {
            throw new IllegalStateException("Driver is suspended and cannot be assigned");
        }

        // Check if driver is active
        if (driver.getStatus() != Driver.DriverStatus.ACTIVE) {
            throw new IllegalStateException("Driver is not in ACTIVE status");
        }

        // Check if license is valid
        if (driver.getLicenseExpiry().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Driver's license has expired");
        }

        // Check for overlapping assignments
        long overlappingCount = assignmentHistoryRepository.countOverlappingAssignments(
                driverId, startDate, endDate
        );

        if (overlappingCount > 0) {
            throw new IllegalStateException("Driver has overlapping rental assignments during this period");
        }

        log.debug("Driver {} validation passed for rental period {} to {}", driverId, startDate, endDate);
    }

    /**
     * Check if driver has overlapping assignments
     */
    public boolean hasOverlappingAssignments(Long driverId, LocalDate startDate, LocalDate endDate) {
        try {
            long count = assignmentHistoryRepository.countOverlappingAssignments(
                    driverId, startDate, endDate
            );
            return count > 0;
        } catch (Exception e) {
            log.error("Error checking overlapping assignments for driver {}", driverId, e);
            return false;
        }
    }

    /**
     * Check if driver is suspended
     */
    public boolean isDriverSuspended(Long driverId) {
        return driverRepository.isDriverSuspended(driverId);
    }

    /**
     * Check if driver is available
     */
    public boolean isDriverAvailable(Long driverId) {
        return driverRepository.isDriverAvailable(driverId);
    }

    /**
     * Get active assignments for a driver
     */
    public List<DriverAssignmentHistory> getActiveAssignments(Long driverId) {
        return assignmentHistoryRepository.findByDriverIdAndStatus(
                driverId, DriverAssignmentHistory.AssignmentStatus.ASSIGNED
        );
    }

    /**
     * Suspend driver with validation
     */
    @Transactional
    public void suspendDriver(Long driverId, String reason) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        if (driver.getStatus().equals(Driver.DriverStatus.SUSPENDED)) {
            throw new IllegalStateException("Driver is already suspended");
        }

        // Check for active assignments
        List<DriverAssignmentHistory> activeAssignments = getActiveAssignments(driverId);
        if (!activeAssignments.isEmpty()) {
            throw new IllegalStateException("Cannot suspend driver with active rental assignments. " +
                    "Total active assignments: " + activeAssignments.size());
        }

        driver.setStatus(Driver.DriverStatus.SUSPENDED);
        driverRepository.save(driver);

        log.info("Driver {} suspended. Reason: {}", driverId, reason);
    }

    /**
     * Resume driver
     */
    @Transactional
    public void resumeDriver(Long driverId) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        if (driver.getStatus().equals(Driver.DriverStatus.SUSPENDED)) {
            throw new IllegalStateException("Driver is not suspended");
        }

        driver.setStatus(Driver.DriverStatus.SUSPENDED);
        driverRepository.save(driver);

        log.info("Driver {} resumed", driverId);
    }
}
