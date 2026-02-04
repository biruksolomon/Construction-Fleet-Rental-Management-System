package com.devcast.fleetmanagement.features.vehicle.service;

import com.devcast.fleetmanagement.features.audit.service.AuditService;
import com.devcast.fleetmanagement.features.company.repository.CompanyRepository;
import com.devcast.fleetmanagement.features.fuel.model.FuelLog;
import com.devcast.fleetmanagement.features.fuel.repository.FuelLogRepository;
import com.devcast.fleetmanagement.features.maintenance.model.MaintenanceRecord;
import com.devcast.fleetmanagement.features.maintenance.repository.MaintenanceRecordRepository;
import com.devcast.fleetmanagement.features.vehicle.dto.*;
import com.devcast.fleetmanagement.features.vehicle.model.*;
import com.devcast.fleetmanagement.features.vehicle.repository.*;
import com.devcast.fleetmanagement.security.util.SecurityUtils;
import com.devcast.fleetmanagement.features.user.model.util.Permission;
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
 * Vehicle Service Implementation
 *
 * Provides comprehensive vehicle management functionality including CRUD operations,
 * status management, time tracking, GPS logging, fuel management, and maintenance.
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CompanyRepository companyRepository;
    private final VehicleUsageLimitRepository vehicleUsageLimitRepository;
    private final VehicleTimeLogRepository vehicleTimeLogRepository;
    private final GpsLogRepository gpsLogRepository;
    private final FuelLogRepository fuelLogRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public VehicleResponse createVehicle(Long companyId, VehicleCreateRequest request) {
        // Verify company access
        verifyCompanyAccess(companyId);

        // Validate plate number uniqueness
        if (vehicleRepository.findByPlateNumber(request.getPlateNumber()).isPresent()) {
            throw new IllegalArgumentException("Plate number already exists: " + request.getPlateNumber());
        }

        // Check permission
        if (!SecurityUtils.hasPermission(Permission.CREATE_VEHICLE)) {
            throw new SecurityException("User does not have permission to create vehicles");
        }

        // Get company
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        // Build vehicle entity
        Vehicle vehicle = Vehicle.builder()
                .company(company)
                .plateNumber(request.getPlateNumber())
                .assetCode(request.getAssetCode())
                .type(request.getType())
                .fuelType(request.getFuelType())
                .hourlyRate(request.getHourlyRate())
                .dailyRate(request.getDailyRate())
                .status(Vehicle.VehicleStatus.AVAILABLE)
                .hasGps(request.getHasGps() != null ? request.getHasGps() : false)
                .hasFuelSensor(request.getHasFuelSensor() != null ? request.getHasFuelSensor() : false)
                .description(request.getDescription())
                .licensePlateRegion(request.getLicensePlateRegion())
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);

        auditService.logAuditEvent(companyId, "VEHICLE_CREATED",
                "Vehicle", saved.getId(),
                "Vehicle created: " + request.getPlateNumber());

        log.info("Vehicle created successfully: {} (ID: {})", request.getPlateNumber(), saved.getId());
        return VehicleResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VehicleResponse> getVehicleById(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .map(vehicle -> {
                    verifyCompanyAccess(vehicle.getCompany().getId());
                    return VehicleResponse.fromEntity(vehicle);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VehicleResponse> getVehicleByPlateNumber(Long companyId, String plateNumber) {
        verifyCompanyAccess(companyId);
        return vehicleRepository.findByPlateNumber(plateNumber)
                .filter(v -> v.getCompany().getId().equals(companyId))
                .map(VehicleResponse::fromEntity);
    }

    @Override
    @Transactional
    public VehicleResponse updateVehicle(Long vehicleId, VehicleUpdateRequest request) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        verifyCompanyAccess(vehicle.getCompany().getId());

        if (!SecurityUtils.hasPermission(Permission.UPDATE_VEHICLE)) {
            throw new SecurityException("User does not have permission to update vehicles");
        }

        // Update plate number if provided
        if (request.getPlateNumber() != null && !request.getPlateNumber().isEmpty()) {
            if (!vehicle.getPlateNumber().equals(request.getPlateNumber())) {
                if (vehicleRepository.findByPlateNumber(request.getPlateNumber()).isPresent()) {
                    throw new IllegalArgumentException("Plate number already exists");
                }
            }
            vehicle.setPlateNumber(request.getPlateNumber());
        }

        // Update other optional fields
        if (request.getAssetCode() != null && !request.getAssetCode().isEmpty()) {
            vehicle.setAssetCode(request.getAssetCode());
        }
        if (request.getHourlyRate() != null) {
            vehicle.setHourlyRate(request.getHourlyRate());
        }
        if (request.getDailyRate() != null) {
            vehicle.setDailyRate(request.getDailyRate());
        }
        if (request.getHasGps() != null) {
            vehicle.setHasGps(request.getHasGps());
        }
        if (request.getHasFuelSensor() != null) {
            vehicle.setHasFuelSensor(request.getHasFuelSensor());
        }
        if (request.getDescription() != null) {
            vehicle.setDescription(request.getDescription());
        }
        if (request.getLicensePlateRegion() != null) {
            vehicle.setLicensePlateRegion(request.getLicensePlateRegion());
        }

        vehicle.setUpdatedAt(LocalDateTime.now());
        Vehicle updated = vehicleRepository.save(vehicle);

        auditService.logAuditEvent(vehicle.getCompany().getId(), "VEHICLE_UPDATED",
                "Vehicle", updated.getId(),
                "Vehicle updated: " + vehicle.getPlateNumber());

        log.info("Vehicle updated: {}", vehicleId);
        return VehicleResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteVehicle(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        verifyCompanyAccess(vehicle.getCompany().getId());

        if (!SecurityUtils.hasPermission(Permission.DELETE_VEHICLE)) {
            throw new SecurityException("User does not have permission to delete vehicles");
        }

        auditService.logAuditEvent(vehicle.getCompany().getId(), "VEHICLE_DELETED",
                "Vehicle", vehicle.getId(),
                "Vehicle deleted: " + vehicle.getPlateNumber());

        vehicleRepository.delete(vehicle);
        log.info("Vehicle deleted: {}", vehicleId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> getVehiclesByCompany(Long companyId, Pageable pageable) {
        verifyCompanyAccess(companyId);
        Page<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId, pageable);
        return vehicles.map(VehicleResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getActiveVehicles(Long companyId) {
        verifyCompanyAccess(companyId);
        return vehicleRepository.findByCompanyIdAndStatus(companyId, Vehicle.VehicleStatus.AVAILABLE)
                .stream()
                .map(VehicleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> getVehiclesByStatus(Long companyId, Vehicle.VehicleStatus status, Pageable pageable) {
        verifyCompanyAccess(companyId);
        Page<Vehicle> vehicles = vehicleRepository.findByCompanyIdAndStatus(companyId, status, pageable);
        return vehicles.map(VehicleResponse::fromEntity);
    }

    @Override
    @Transactional
    public void markAvailable(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        verifyCompanyAccess(vehicle.getCompany().getId());

        vehicle.setStatus(Vehicle.VehicleStatus.AVAILABLE);
        vehicle.setUpdatedAt(LocalDateTime.now());
        vehicleRepository.save(vehicle);

        auditService.logAuditEvent(vehicle.getCompany().getId(), "VEHICLE_MARKED_AVAILABLE",
                "Vehicle", vehicle.getId(),
                "Vehicle marked as available: " + vehicle.getPlateNumber());

        log.info("Vehicle marked available: {}", vehicleId);
    }

    @Override
    @Transactional
    public void markRented(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        verifyCompanyAccess(vehicle.getCompany().getId());

        vehicle.setStatus(Vehicle.VehicleStatus.RENTED);
        vehicle.setUpdatedAt(LocalDateTime.now());
        vehicleRepository.save(vehicle);

        auditService.logAuditEvent(vehicle.getCompany().getId(), "VEHICLE_MARKED_RENTED",
                "Vehicle", vehicle.getId(),
                "Vehicle marked as rented: " + vehicle.getPlateNumber());

        log.info("Vehicle marked rented: {}", vehicleId);
    }

    @Override
    @Transactional
    public void markForMaintenance(Long vehicleId, String reason) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        verifyCompanyAccess(vehicle.getCompany().getId());

        vehicle.setStatus(Vehicle.VehicleStatus.MAINTENANCE);
        vehicle.setUpdatedAt(LocalDateTime.now());
        vehicleRepository.save(vehicle);

        String details = "Vehicle marked for maintenance: " + vehicle.getPlateNumber() +
                (reason != null ? " Reason: " + reason : "");
        auditService.logAuditEvent(vehicle.getCompany().getId(), "VEHICLE_MARKED_MAINTENANCE",
                "Vehicle", vehicle.getId(), details);

        log.info("Vehicle marked for maintenance: {}", vehicleId);
    }

    @Override
    @Transactional
    public void markInactive(Long vehicleId, String reason) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        verifyCompanyAccess(vehicle.getCompany().getId());

        if (!SecurityUtils.hasPermission(Permission.UPDATE_VEHICLE)) {
            throw new SecurityException("User does not have permission to mark vehicles as inactive");
        }

        vehicle.setStatus(Vehicle.VehicleStatus.INACTIVE);
        vehicle.setUpdatedAt(LocalDateTime.now());
        vehicleRepository.save(vehicle);

        String details = "Vehicle marked as inactive: " + vehicle.getPlateNumber() +
                (reason != null ? " Reason: " + reason : "");
        auditService.logAuditEvent(vehicle.getCompany().getId(), "VEHICLE_MARKED_INACTIVE",
                "Vehicle", vehicle.getId(), details);

        log.info("Vehicle marked inactive: {}", vehicleId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isVehicleAvailable(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .map(v -> v.getStatus() == Vehicle.VehicleStatus.AVAILABLE)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Vehicle.VehicleStatus> getVehicleStatus(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .map(Vehicle::getStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VehicleUsageLimit> getUsageLimit(Long vehicleId) {
        return vehicleUsageLimitRepository.findByVehicleId(vehicleId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exceedsUsageLimit(Long vehicleId) {
        Optional<VehicleUsageLimit> limit = vehicleUsageLimitRepository.findByVehicleId(vehicleId);
        if (limit.isEmpty()) {
            return false;
        }

        // Check current usage against limit
        Long totalUsageHours = getTotalUsageTime(vehicleId, null, null);
        return totalUsageHours > limit.get().getMaxHoursPerMonth();
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleUsageStats getUsageStatistics(Long vehicleId) {
        // Implementation would aggregate usage data
        return VehicleUsageStats.builder()
                .vehicleId(vehicleId)
                .totalUsageHours(getTotalUsageTime(vehicleId, null, null))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> getVehiclesApproachingLimit(Long companyId) {
        verifyCompanyAccess(companyId);
        // Implementation would filter vehicles approaching limits
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public VehicleTimeLog logStartTime(Long vehicleId, Long driverId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        verifyCompanyAccess(vehicle.getCompany().getId());

        VehicleTimeLog log = VehicleTimeLog.builder()
                .vehicle(vehicle)
                .startTime(LocalDateTime.now())
                .build();

        return vehicleTimeLogRepository.save(log);
    }

    @Override
    @Transactional
    public VehicleTimeLog logEndTime(Long vehicleId) {
        // Find latest time log for this vehicle without end time
        VehicleTimeLog latestLog = vehicleTimeLogRepository.findLatestActiveLog(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("No active time log found"));

        latestLog.setEndTime(LocalDateTime.now());
        return vehicleTimeLogRepository.save(latestLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleTimeLog> getTimeLogs(Long vehicleId, Pageable pageable) {
        return vehicleTimeLogRepository.findByVehicleId(vehicleId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleTimeLog> getTimeLogsByPeriod(Long vehicleId, Long fromDate, Long toDate) {
        // Implementation would filter by date range
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalUsageTime(Long vehicleId, Long fromDate, Long toDate) {
        // Calculate total usage hours
        return 0L;
    }

    @Override
    @Transactional
    public GpsLog logLocation(Long vehicleId, Double latitude, Double longitude) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        if (!vehicle.getHasGps()) {
            throw new IllegalArgumentException("Vehicle does not have GPS capability");
        }

        GpsLog gpsLog = GpsLog.builder()
                .vehicle(vehicle)
                .latitude(BigDecimal.valueOf(latitude))
                .longitude(BigDecimal.valueOf(longitude))
                .recordedAt(LocalDateTime.now())
                .build();

        return gpsLogRepository.save(gpsLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GpsLog> getCurrentLocation(Long vehicleId) {
        return gpsLogRepository.findLatestByVehicleId(vehicleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GpsLog> getGpsTrail(Long vehicleId, Long fromDate, Long toDate) {
        // Implementation would return GPS trail for period
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleRoute getVehicleRoute(Long vehicleId, Long fromDate, Long toDate) {
        return VehicleRoute.builder()
                .vehicleId(vehicleId)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleTrackingInfo getRealtimeTracking(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        Optional<GpsLog> currentLocation = getCurrentLocation(vehicleId);
        Optional<VehicleTimeLog> activeLog = vehicleTimeLogRepository.findLatestActiveLog(vehicleId);

        return VehicleTrackingInfo.builder()
                .vehicleId(vehicleId)
                .plateNumber(vehicle.getPlateNumber())
                .status(vehicle.getStatus())
                .currentLocation(currentLocation.orElse(null))
                .isActive(activeLog.isPresent())
                .build();
    }

    @Override
    @Transactional
    public void checkGeofenceViolation(Long vehicleId, Double latitude, Double longitude) {
        // Implementation would check geofence boundaries
    }

    @Override
    @Transactional
    public FuelLog logFuel(Long vehicleId, FuelLog fuelLog) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        verifyCompanyAccess(vehicle.getCompany().getId());

        fuelLog.setVehicle(vehicle);
        if (fuelLog.getRefillDate() == null) {
            fuelLog.setRefillDate(java.time.LocalDate.now());
        }

        return fuelLogRepository.save(fuelLog);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FuelLog> getFuelLogs(Long vehicleId, Pageable pageable) {
        return fuelLogRepository.findByVehicleId(vehicleId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public FuelConsumptionAnalysis getFuelAnalysis(Long vehicleId, Long fromDate, Long toDate) {
        return FuelConsumptionAnalysis.builder()
                .vehicleId(vehicleId)
                .averageConsumption(getAverageFuelConsumption(vehicleId))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAverageFuelConsumption(Long vehicleId) {
        // Calculate average fuel consumption
        return BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuelAnomaly> detectFuelAnomalies(Long vehicleId) {
        // Implementation would detect unusual fuel consumption
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public MaintenanceRecord logMaintenance(Long vehicleId, MaintenanceRecord record) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        verifyCompanyAccess(vehicle.getCompany().getId());

        record.setVehicle(vehicle);
        if (record.getMaintenanceDate() == null) {
            record.setMaintenanceDate(java.time.LocalDate.now());
        }

        return maintenanceRecordRepository.save(record);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MaintenanceRecord> getMaintenanceRecords(Long vehicleId, Pageable pageable) {
        return maintenanceRecordRepository.findByVehicleId(vehicleId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceRecord> getPendingMaintenance(Long vehicleId) {
        return maintenanceRecordRepository.findPendingByVehicleId(vehicleId,LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaintenanceSchedule> getMaintenanceSchedule(Long vehicleId) {
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public MaintenanceRecord completeMaintenance(Long recordId, String notes) {
        MaintenanceRecord record = maintenanceRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance record not found"));

        record.setMaintenanceDate(LocalDate.from(LocalDateTime.now()));
        record.setNotes(notes);

        return maintenanceRecordRepository.save(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesNeedingMaintenance(Long companyId) {
        verifyCompanyAccess(companyId);
        List<Vehicle> vehicles = vehicleRepository.findByCompanyIdAndStatus(companyId, Vehicle.VehicleStatus.MAINTENANCE);
        return vehicles.stream()
                .map(VehicleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalMaintenanceCost(Long vehicleId, Long fromDate, Long toDate) {
        return BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public VehiclePerformanceReport getPerformanceReport(Long vehicleId, Long fromDate, Long toDate) {
        return VehiclePerformanceReport.builder()
                .vehicleId(vehicleId)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> searchVehicles(Long companyId, String searchTerm, Pageable pageable) {
        verifyCompanyAccess(companyId);
        Page<Vehicle> vehicles = vehicleRepository.searchByCompanyIdAndPlateOrAsset(companyId, searchTerm, pageable);
        return vehicles.map(VehicleResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VehicleResponse> filterVehicles(Long companyId, VehicleFilterCriteria criteria, Pageable pageable) {
        verifyCompanyAccess(companyId);
        // Implementation would apply filter criteria
        return getVehiclesByCompany(companyId, pageable);
    }

    @Override
    @Transactional
    public VehicleUsageLimit setUsageLimit(Long vehicleId, VehicleUsageLimit limit) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        verifyCompanyAccess(vehicle.getCompany().getId());

        limit.setVehicle(vehicle);
        return vehicleUsageLimitRepository.save(limit);
    }

    @Override
    @Transactional(readOnly = true)
    public FleetHealthStatus getFleetHealth(Long companyId) {
        verifyCompanyAccess(companyId);

        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        if (vehicles.isEmpty()) {
            return FleetHealthStatus.builder()
                    .companyId(companyId)
                    .totalVehicles(0)
                    .availableVehicles(0)
                    .rentedVehicles(0)
                    .maintenanceVehicles(0)
                    .healthPercentage(0.0)
                    .build();
        }

        long available = vehicles.stream().filter(v -> v.getStatus() == Vehicle.VehicleStatus.AVAILABLE).count();
        long rented = vehicles.stream().filter(v -> v.getStatus() == Vehicle.VehicleStatus.RENTED).count();
        long maintenance = vehicles.stream().filter(v -> v.getStatus() == Vehicle.VehicleStatus.MAINTENANCE).count();

        double healthPercentage = (available / (double) vehicles.size()) * 100;

        return FleetHealthStatus.builder()
                .companyId(companyId)
                .totalVehicles(vehicles.size())
                .availableVehicles((int) available)
                .rentedVehicles((int) rented)
                .maintenanceVehicles((int) maintenance)
                .healthPercentage(healthPercentage)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCostPerKm(Long vehicleId, Long fromDate, Long toDate) {
        return BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRevenuePerKm(Long vehicleId, Long fromDate, Long toDate) {
        return BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleProfitability getVehicleProfitability(Long vehicleId, Long fromDate, Long toDate) {
        return VehicleProfitability.builder()
                .vehicleId(vehicleId)
                .totalRevenue(BigDecimal.ZERO)
                .totalCost(BigDecimal.ZERO)
                .profit(BigDecimal.ZERO)
                .profitMargin(BigDecimal.ZERO)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleComparison> getFleetComparison(Long companyId) {
        verifyCompanyAccess(companyId);
        return new ArrayList<>();
    }

    private void verifyCompanyAccess(Long companyId) {
        if (!SecurityUtils.hasCompanyAccess(companyId)) {
            throw new SecurityException("Company access denied");
        }
    }
}
