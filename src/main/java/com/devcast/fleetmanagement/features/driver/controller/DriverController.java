package com.devcast.fleetmanagement.features.driver.controller;

import com.devcast.fleetmanagement.features.auth.dto.ApiResponse;
import com.devcast.fleetmanagement.features.driver.dto.*;
import com.devcast.fleetmanagement.features.driver.exception.*;
import com.devcast.fleetmanagement.features.driver.model.Driver;
import com.devcast.fleetmanagement.features.driver.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Driver Management REST Controller
 * Comprehensive driver management endpoints including CRUD, assignments, and performance tracking
 *
 * Base Path: /drivers (context path /api is already set)
 * Multi-tenant: Enforced at service level
 */
@RestController
@RequestMapping("/drivers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Drivers", description = "Driver management, performance, and work hours endpoints")
public class DriverController {

    private final DriverService driverService;

    // ==================== Driver CRUD Operations ====================

    /**
     * Create a new driver
     * POST /api/drivers
     */
    @PostMapping
    @Operation(summary = "Create driver", description = "Create a new driver in the company")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Driver created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Duplicate license number")
    })
    public ResponseEntity<ApiResponse<DriverResponse>> createDriver(
            @RequestParam Long companyId,
            @Valid @RequestBody DriverCreateRequest request
    ) {
        try {
            log.info("Creating driver for company: {}", companyId);

            Driver driver = Driver.builder()
                    .licenseNumber(request.getLicenseNumber())
                    .licenseExpiry(request.getLicenseExpiry())
                    .licenseType(request.getLicenseType())
                    .hourlyWage(request.getHourlyWage())
                    .employmentType(Driver.EmploymentType.valueOf(request.getEmploymentType()))
                    .insuranceNumber(request.getInsuranceNumber())
                    .insuranceExpiry(request.getInsuranceExpiry())
                    .build();

            Driver created = driverService.createDriver(companyId, driver);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(DriverResponse.fromEntity(created), "Driver created successfully"));
        } catch (DuplicateLicenseNumberException e) {
            log.warn("Duplicate license: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Duplicate license number"));
        } catch (Exception e) {
            log.error("Error creating driver", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create driver: " + e.getMessage()));
        }
    }

    /**
     * Get driver by ID
     * GET /api/drivers/{driverId}
     */
    @GetMapping("/{driverId}")
    @Operation(summary = "Get driver", description = "Retrieve driver details by ID")
    public ResponseEntity<ApiResponse<DriverResponse>> getDriver(
            @PathVariable Long driverId
    ) {
        try {
            return driverService.getDriverById(driverId)
                    .map(driver -> ResponseEntity.ok(ApiResponse.<DriverResponse>success(DriverResponse.fromEntity(driver), "Driver retrieved successfully")))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving driver", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<DriverResponse>error("Failed to retrieve driver: " + e.getMessage()));
        }
    }

    /**
     * Update driver
     * PUT /api/drivers/{driverId}
     */
    @PutMapping("/{driverId}")
    @Operation(summary = "Update driver", description = "Update driver information")
    public ResponseEntity<ApiResponse<DriverResponse>> updateDriver(
            @PathVariable Long driverId,
            @Valid @RequestBody DriverUpdateRequest request
    ) {
        try {
            Driver driver = Driver.builder()
                    .licenseNumber(request.getLicenseNumber())
                    .licenseExpiry(request.getLicenseExpiry())
                    .hourlyWage(request.getHourlyWage())
                    .licenseType(request.getLicenseType())
                    .employmentType(request.getEmploymentType() != null ?
                            Driver.EmploymentType.valueOf(request.getEmploymentType()) : null)
                    .status(request.getStatus() != null ?
                            Driver.DriverStatus.valueOf(request.getStatus()) : null)
                    .insuranceNumber(request.getInsuranceNumber())
                    .insuranceExpiry(request.getInsuranceExpiry())
                    .build();

            Driver updated = driverService.updateDriver(driverId, driver);
            return ResponseEntity.ok(ApiResponse.success(DriverResponse.fromEntity(updated), "Driver updated successfully"));
        } catch (DriverNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (DuplicateLicenseNumberException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Duplicate license number"));
        } catch (Exception e) {
            log.error("Error updating driver", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update driver: " + e.getMessage()));
        }
    }

    /**
     * Delete driver
     * DELETE /api/drivers/{driverId}
     */
    @DeleteMapping("/{driverId}")
    @Operation(summary = "Delete driver", description = "Delete driver record")
    public ResponseEntity<ApiResponse<Void>> deleteDriver(
            @PathVariable Long driverId
    ) {
        try {
            driverService.deleteDriver(driverId);
            return ResponseEntity.ok(ApiResponse.success(null, "Driver deleted successfully"));
        } catch (DriverNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting driver", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete driver: " + e.getMessage()));
        }
    }

    /**
     * Get all drivers for company
     * GET /api/drivers/company/{companyId}
     */
    @GetMapping("/company/{companyId}")
    @Operation(summary = "Get company drivers", description = "Get all drivers in a company")
    public ResponseEntity<ApiResponse<Page<DriverResponse>>> getCompanyDrivers(
            @PathVariable Long companyId,
            Pageable pageable
    ) {
        try {
            Page<Driver> drivers = driverService.getDriversByCompany(companyId, pageable);
            Page<DriverResponse> responses = drivers.map(DriverResponse::fromEntity);
            return ResponseEntity.ok(ApiResponse.<Page<DriverResponse>>success(responses, "Drivers retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving company drivers", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Page<DriverResponse>>error("Failed to retrieve drivers: " + e.getMessage()));
        }
    }

    // ==================== Driver Status Management ====================

    /**
     * Mark driver as available
     * PATCH /api/drivers/{driverId}/available
     */
    @PatchMapping("/{driverId}/available")
    @Operation(summary = "Mark available", description = "Mark driver as available")
    public ResponseEntity<ApiResponse<Void>> markAvailable(
            @PathVariable Long driverId
    ) {
        try {
            driverService.markAvailable(driverId);
            return ResponseEntity.ok(ApiResponse.success(null, "Driver marked as available"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update driver status"));
        }
    }

    /**
     * Mark driver on leave
     * PATCH /api/drivers/{driverId}/on-leave
     */
    @PatchMapping("/{driverId}/on-leave")
    @Operation(summary = "Mark on leave", description = "Mark driver as on leave")
    public ResponseEntity<ApiResponse<Void>> markOnLeave(
            @PathVariable Long driverId,
            @RequestParam Long fromDate,
            @RequestParam Long toDate,
            @RequestParam String reason
    ) {
        try {
            driverService.markOnLeave(driverId, fromDate, toDate, reason);
            return ResponseEntity.ok(ApiResponse.success(null, "Driver marked as on leave"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update driver status"));
        }
    }

    /**
     * Get driver status
     * GET /api/drivers/{driverId}/status
     */
    @GetMapping("/{driverId}/status")
    @Operation(summary = "Get status", description = "Get current driver status")
    public ResponseEntity<ApiResponse<String>> getDriverStatus(
            @PathVariable Long driverId
    ) {
        try {
            String status = driverService.getDriverStatus(driverId)
                    .orElse("UNKNOWN");
            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve status"));
        }
    }

    // ==================== License & Documentation ====================

    /**
     * Get license status
     * GET /api/drivers/{driverId}/license-status
     */
    @GetMapping("/{driverId}/license-status")
    @Operation(summary = "Get license status", description = "Check driver license validity")
    public ResponseEntity<ApiResponse<LicenseStatus>> getLicenseStatus(
            @PathVariable Long driverId
    ) {
        try {
            LicenseStatus status = driverService.getLicenseStatus(driverId);
            return ResponseEntity.ok(ApiResponse.<LicenseStatus>success(status, "License status retrieved"));
        } catch (DriverNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<LicenseStatus>error("Failed to retrieve license status"));
        }
    }

    /**
     * Get drivers with expiring licenses
     * GET /api/drivers/company/{companyId}/expiring-licenses
     */
    @GetMapping("/company/{companyId}/expiring-licenses")
    @Operation(summary = "Get expiring licenses", description = "Get drivers with licenses expiring soon")
    public ResponseEntity<ApiResponse<List<DriverBasicResponse>>> getExpiringLicenses(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "30") int daysFromNow
    ) {
        try {
            List<Driver> drivers = driverService.getDriversWithExpiringLicenses(companyId, daysFromNow);
            List<DriverBasicResponse> responses = drivers.stream()
                    .map(DriverBasicResponse::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.<List<DriverBasicResponse>>success(responses, "Expiring licenses retrieved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<List<DriverBasicResponse>>error("Failed to retrieve drivers with expiring licenses"));
        }
    }

    /**
     * Get document verification status
     * GET /api/drivers/{driverId}/documents
     */
    @GetMapping("/{driverId}/documents")
    @Operation(summary = "Get documents", description = "Get document verification status")
    public ResponseEntity<ApiResponse<DocumentVerificationStatus>> getDocumentStatus(
            @PathVariable Long driverId
    ) {
        try {
            DocumentVerificationStatus status = driverService.getDocumentStatus(driverId);
            return ResponseEntity.ok(ApiResponse.<DocumentVerificationStatus>success(status, "Document status retrieved"));
        } catch (DriverNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<DocumentVerificationStatus>error("Failed to retrieve document status"));
        }
    }

    // ==================== Work Hours & Performance ====================

    /**
     * Get work hours summary
     * GET /api/drivers/{driverId}/work-hours
     */
    @GetMapping("/{driverId}/work-hours")
    @Operation(summary = "Get work hours", description = "Get driver work hours summary")
    public ResponseEntity<ApiResponse<WorkHoursSummary>> getWorkHoursSummary(
            @PathVariable Long driverId,
            @RequestParam Long fromDate,
            @RequestParam Long toDate
    ) {
        try {
            WorkHoursSummary summary = driverService.getWorkHoursSummary(driverId, fromDate, toDate);
            return ResponseEntity.ok(ApiResponse.<WorkHoursSummary>success(summary, "Work hours summary retrieved"));
        } catch (DriverNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<WorkHoursSummary>error("Failed to retrieve work hours"));
        }
    }

    /**
     * Get performance score
     * GET /api/drivers/{driverId}/performance
     */
    @GetMapping("/{driverId}/performance")
    @Operation(summary = "Get performance", description = "Get driver performance metrics")
    public ResponseEntity<ApiResponse<DriverPerformance>> getPerformance(
            @PathVariable Long driverId,
            @RequestParam(required = false) Long fromDate,
            @RequestParam(required = false) Long toDate
    ) {
        try {
            DriverPerformance performance = driverService.getPerformanceScore(driverId, fromDate, toDate);
            return ResponseEntity.ok(ApiResponse.<DriverPerformance>success(performance, "Performance metrics retrieved"));
        } catch (DriverNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<DriverPerformance>error("Failed to retrieve performance data"));
        }
    }

    /**
     * Get top drivers
     * GET /api/drivers/company/{companyId}/top
     */
    @GetMapping("/company/{companyId}/top")
    @Operation(summary = "Get top drivers", description = "Get top rated drivers in company")
    public ResponseEntity<ApiResponse<List<DriverBasicResponse>>> getTopDrivers(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            List<Driver> drivers = driverService.getTopDrivers(companyId, limit);
            List<DriverBasicResponse> responses = drivers.stream()
                    .map(DriverBasicResponse::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.<List<DriverBasicResponse>>success(responses, "Top drivers retrieved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<List<DriverBasicResponse>>error("Failed to retrieve top drivers"));
        }
    }

    // ==================== Salary & Payroll ====================

    /**
     * Get salary components
     * GET /api/drivers/{driverId}/salary
     */
    @GetMapping("/{driverId}/salary")
    @Operation(summary = "Get salary", description = "Get driver salary components")
    public ResponseEntity<ApiResponse<DriverSalaryComponents>> getSalaryComponents(
            @PathVariable Long driverId,
            @RequestParam Long date
    ) {
        try {
            DriverSalaryComponents components = driverService.getSalaryComponents(driverId, date);
            return ResponseEntity.ok(ApiResponse.<DriverSalaryComponents>success(components, "Salary components retrieved"));
        } catch (DriverNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<DriverSalaryComponents>error("Failed to retrieve salary components"));
        }
    }

    // ==================== Search & Filter ====================

    /**
     * Search drivers
     * GET /api/drivers/company/{companyId}/search
     */
    @GetMapping("/company/{companyId}/search")
    @Operation(summary = "Search drivers", description = "Search drivers by name or license")
    public ResponseEntity<ApiResponse<Page<DriverResponse>>> searchDrivers(
            @PathVariable Long companyId,
            @RequestParam String searchTerm,
            Pageable pageable
    ) {
        try {
            Page<Driver> drivers = driverService.searchDrivers(companyId, searchTerm, pageable);
            Page<DriverResponse> responses = drivers.map(DriverResponse::fromEntity);
            return ResponseEntity.ok(ApiResponse.<Page<DriverResponse>>success(responses, "Search results retrieved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Page<DriverResponse>>error("Failed to search drivers"));
        }
    }

    /**
     * Filter drivers
     * POST /api/drivers/company/{companyId}/filter
     */
    @PostMapping("/company/{companyId}/filter")
    @Operation(summary = "Filter drivers", description = "Filter drivers by criteria")
    public ResponseEntity<ApiResponse<Page<DriverResponse>>> filterDrivers(
            @PathVariable Long companyId,
            @RequestBody DriverFilterCriteria criteria,
            Pageable pageable
    ) {
        try {
            Page<Driver> drivers = driverService.filterDrivers(companyId, criteria, pageable);
            Page<DriverResponse> responses = drivers.map(DriverResponse::fromEntity);
            return ResponseEntity.ok(ApiResponse.<Page<DriverResponse>>success(responses, "Filter results retrieved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Page<DriverResponse>>error("Failed to filter drivers"));
        }
    }
}
