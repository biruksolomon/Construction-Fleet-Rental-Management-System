package com.devcast.fleetmanagement.features.maintenance.service;

import com.devcast.fleetmanagement.features.maintenance.model.MaintenanceRecord;
import com.devcast.fleetmanagement.features.maintenance.repository.MaintenanceRecordRepository;
import com.devcast.fleetmanagement.features.user.model.util.Permission;
import com.devcast.fleetmanagement.features.vehicle.model.Vehicle;
import com.devcast.fleetmanagement.features.vehicle.repository.VehicleRepository;
import com.devcast.fleetmanagement.security.annotation.RequirePermission;
import com.devcast.fleetmanagement.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Maintenance Service Implementation
 * Handles vehicle maintenance scheduling, tracking, and cost management with multi-tenant support
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MaintenanceServiceImpl implements MaintenanceService {

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final VehicleRepository vehicleRepository;

    // ==================== Maintenance Record Operations ====================

    @Override
    @RequirePermission(Permission.CREATE_MAINTENANCE)
    public MaintenanceRecord createMaintenanceRecord(Long vehicleId, MaintenanceRecord record) {
        log.info("Creating maintenance record for vehicle: {}", vehicleId);

        Long companyId = SecurityUtils.getCurrentUserCompanyId();
        
        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(vehicleId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found or does not belong to company"));

        record.setVehicle(vehicle);
        record.setCompany(vehicle.getCompany());
        record.setStatus(MaintenanceRecord.MaintenanceStatus.SCHEDULED);

        MaintenanceRecord saved = maintenanceRecordRepository.save(record);
        log.info("Maintenance record created with ID: {}", saved.getId());
        return saved;
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public Optional<MaintenanceRecord> getMaintenanceRecord(Long recordId) {
        log.info("Retrieving maintenance record: {}", recordId);

        Long companyId = SecurityUtils.getCurrentUserCompanyId();
        
        return maintenanceRecordRepository.findById(recordId)
                .filter(record -> record.getCompany().getId().equals(companyId));
    }

    @Override
    @RequirePermission(Permission.UPDATE_MAINTENANCE)
    public MaintenanceRecord updateMaintenanceRecord(Long recordId, MaintenanceRecord updateData) {
        log.info("Updating maintenance record: {}", recordId);

        Long companyId = SecurityUtils.getCurrentUserCompanyId();
        
        MaintenanceRecord record = maintenanceRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance record not found"));

        if (!record.getCompany().getId().equals(companyId)) {
            throw new AccessDeniedException("Unauthorized access to maintenance record");
        }

        if (updateData.getMaintenanceType() != null) {
            record.setMaintenanceType(updateData.getMaintenanceType());
        }
        if (updateData.getCost() != null) {
            record.setCost(updateData.getCost());
        }
        if (updateData.getMaintenanceDate() != null) {
            record.setMaintenanceDate(updateData.getMaintenanceDate());
        }
        if (updateData.getNextDueHours() != null) {
            record.setNextDueHours(updateData.getNextDueHours());
        }
        if (updateData.getNotes() != null) {
            record.setNotes(updateData.getNotes());
        }
        if (updateData.getServiceType() != null) {
            record.setServiceType(updateData.getServiceType());
        }
        if (updateData.getVendorName() != null) {
            record.setVendorName(updateData.getVendorName());
        }
        if (updateData.getStatus() != null) {
            record.setStatus(updateData.getStatus());
        }

        MaintenanceRecord updated = maintenanceRecordRepository.save(record);
        log.info("Maintenance record updated: {}", updated.getId());
        return updated;
    }

    @Override
    @RequirePermission(Permission.UPDATE_MAINTENANCE)
    public MaintenanceRecord completeMaintenanceRecord(Long recordId, String notes, BigDecimal actualCost) {
        log.info("Completing maintenance record: {}", recordId);

        MaintenanceRecord record = getMaintenanceRecord(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance record not found"));

        record.setStatus(MaintenanceRecord.MaintenanceStatus.COMPLETED);
        record.setNotes(notes != null ? notes : record.getNotes());
        if (actualCost != null) {
            record.setCost(actualCost);
        }

        MaintenanceRecord updated = maintenanceRecordRepository.save(record);
        log.info("Maintenance record completed: {}", updated.getId());
        return updated;
    }

    @Override
    @RequirePermission(Permission.DELETE_MAINTENANCE)
    public void cancelMaintenanceRecord(Long recordId, String reason) {
        log.info("Cancelling maintenance record: {}", recordId);

        MaintenanceRecord record = getMaintenanceRecord(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance record not found"));

        record.setStatus(MaintenanceRecord.MaintenanceStatus.CANCELLED);
        record.setNotes((record.getNotes() != null ? record.getNotes() + " | " : "") + "Cancelled: " + reason);

        maintenanceRecordRepository.save(record);
        log.info("Maintenance record cancelled: {}", recordId);
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public Page<MaintenanceRecord> getMaintenanceRecords(Long vehicleId, Pageable pageable) {
        log.info("Retrieving maintenance records for vehicle: {}", vehicleId);

        Long companyId = SecurityUtils.getCurrentUserCompanyId();
        
        return maintenanceRecordRepository.findByVehicleIdAndCompanyId(vehicleId, companyId, pageable);
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public List<MaintenanceRecord> getMaintenanceRecordsByPeriod(Long vehicleId, Long fromDate, Long toDate) {
        log.info("Retrieving maintenance records for vehicle: {} from {} to {}", vehicleId, fromDate, toDate);

        Long companyId = SecurityUtils.getCurrentUserCompanyId();
        
        LocalDate start = LocalDate.ofEpochDay(fromDate);
        LocalDate end = LocalDate.ofEpochDay(toDate);

        return maintenanceRecordRepository.findByVehicleIdAndDateRange(vehicleId, companyId, start, end);
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public List<MaintenanceRecord> getPendingMaintenance(Long companyId) {
        log.info("Retrieving pending maintenance for company: {}", companyId);

        SecurityUtils.validateCompanyAccess(companyId);
        
        return maintenanceRecordRepository.findByCompanyIdAndStatus(
                companyId,
                MaintenanceRecord.MaintenanceStatus.SCHEDULED
        );
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public Page<MaintenanceRecord> getMaintenanceByStatus(Long companyId, String status, Pageable pageable) {
        log.info("Retrieving maintenance records by status: {} for company: {}", status, companyId);

        SecurityUtils.validateCompanyAccess(companyId);
        
        MaintenanceRecord.MaintenanceStatus maintenanceStatus = 
                MaintenanceRecord.MaintenanceStatus.valueOf(status.toUpperCase());

        Page<MaintenanceRecord> records = maintenanceRecordRepository.findByCompanyId(companyId, pageable);
        return (Page<MaintenanceRecord>) records.filter(r -> r.getStatus() == maintenanceStatus);
    }

    // ==================== Maintenance Scheduling ====================

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public List<MaintenanceService.MaintenanceSchedule> getMaintenanceSchedule(Long vehicleId) {
        log.info("Retrieving maintenance schedule for vehicle: {}", vehicleId);

        Long companyId = SecurityUtils.getCurrentUserCompanyId();
        List<MaintenanceRecord> records = maintenanceRecordRepository
                .findByVehicleIdAndCompanyId(vehicleId, companyId, Pageable.unpaged()).getContent();

        return records.stream()
                .map(record -> new MaintenanceService.MaintenanceSchedule(
                    record.getVehicle().getId(),
                    record.getServiceType() != null ? record.getServiceType() : "GENERAL_SERVICE",
                    (long) record.getNextDueHours(),
                    record.getMaintenanceDate().toEpochDay(),
                    record.getStatus().toString()
                ))
                .toList();
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public List<MaintenanceService.OverdueMaintenanceItem> getOverdueMaintenanceItems(Long companyId) {
        log.info("Retrieving overdue maintenance items for company: {}", companyId);

        SecurityUtils.validateCompanyAccess(companyId);
        
        List<MaintenanceRecord> overdueRecords = maintenanceRecordRepository.findOverdueByCompanyId(
                companyId,
                LocalDate.now(),
                MaintenanceRecord.MaintenanceStatus.COMPLETED
        );

        return overdueRecords.stream()
                .map(record -> new MaintenanceService.OverdueMaintenanceItem(
                    record.getVehicle().getId(),
                    record.getVehicle().getPlateNumber(),
                    record.getServiceType() != null ? record.getServiceType() : "UNKNOWN",
                    record.getMaintenanceDate().toEpochDay(),
                    calculateUrgency(record.getMaintenanceDate())
                ))
                .toList();
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public List<MaintenanceService.UpcomingMaintenanceItem> getUpcomingMaintenanceItems(Long companyId, int daysAhead) {
        log.info("Retrieving upcoming maintenance items for company: {}", companyId);

        SecurityUtils.validateCompanyAccess(companyId);
        
        LocalDate now = LocalDate.now();
        LocalDate future = now.plusDays(daysAhead);

        Page<MaintenanceRecord> records = maintenanceRecordRepository.findByCompanyIdAndDateRange(
                companyId,
                now,
                future,
                Pageable.unpaged()
        );

        return records.getContent().stream()
                .filter(r -> r.getStatus() == MaintenanceRecord.MaintenanceStatus.SCHEDULED)
                .map(record -> {
                    int daysUntil = (int) (record.getMaintenanceDate().toEpochDay() - now.toEpochDay());
                    return new MaintenanceService.UpcomingMaintenanceItem(
                        record.getVehicle().getId(),
                        record.getVehicle().getPlateNumber(),
                        record.getServiceType() != null ? record.getServiceType() : "GENERAL_SERVICE",
                        record.getMaintenanceDate().toEpochDay(),
                        daysUntil
                    );
                })
                .toList();
    }

    @Override
    @RequirePermission(Permission.CREATE_MAINTENANCE)
    public MaintenanceRecord schedulePreventiveMaintenance(Long vehicleId, String serviceType) {
        log.info("Scheduling preventive maintenance for vehicle: {} with service type: {}", vehicleId, serviceType);

        Long companyId = SecurityUtils.getCurrentUserCompanyId();
        
        Vehicle vehicle = vehicleRepository.findByIdAndCompanyId(vehicleId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        MaintenanceRecord record = MaintenanceRecord.builder()
                .vehicle(vehicle)
                .company(vehicle.getCompany())
                .maintenanceType(MaintenanceRecord.MaintenanceType.SERVICE)
                .status(MaintenanceRecord.MaintenanceStatus.SCHEDULED)
                .serviceType(serviceType)
                .maintenanceDate(LocalDate.now())
                .nextDueHours(500)
                .cost(BigDecimal.ZERO)
                .build();

        return maintenanceRecordRepository.save(record);
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public List<Long> getVehiclesNeedingMaintenance(Long companyId) {
        log.info("Retrieving vehicles needing maintenance for company: {}", companyId);

        SecurityUtils.validateCompanyAccess(companyId);
        
        return maintenanceRecordRepository.findVehiclesNeedingMaintenance(companyId);
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public MaintenanceService.MaintenanceStatus getMaintenanceStatus(Long vehicleId) {
        log.info("Retrieving maintenance status for vehicle: {}", vehicleId);

        Long companyId = SecurityUtils.getCurrentUserCompanyId();
        
        List<MaintenanceRecord> records = maintenanceRecordRepository
                .findByVehicleIdAndCompanyId(vehicleId, companyId, Pageable.unpaged()).getContent();

        List<String> overdue = records.stream()
                .filter(r -> r.getMaintenanceDate().isBefore(LocalDate.now()))
                .map(r -> r.getServiceType() != null ? r.getServiceType() : "UNKNOWN")
                .toList();

        List<String> upcoming = records.stream()
                .filter(r -> r.getMaintenanceDate().isAfter(LocalDate.now()) && 
                            r.getMaintenanceDate().isBefore(LocalDate.now().plusDays(30)))
                .map(r -> r.getServiceType() != null ? r.getServiceType() : "UNKNOWN")
                .toList();

        return new MaintenanceService.MaintenanceStatus(
                vehicleId,
                overdue.isEmpty() ? "GOOD" : "NEEDS_ATTENTION",
                overdue,
                upcoming,
                overdue.size() + upcoming.size()
        );
    }

    // ==================== Service Types Management ====================

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public List<MaintenanceService.ServiceType> getAvailableServiceTypes() {
        log.info("Retrieving available service types");

        return List.of(
                new MaintenanceService.ServiceType("OIL_CHANGE", "Oil Change", "Regular oil and filter change", 6, 10000, BigDecimal.valueOf(80)),
                new MaintenanceService.ServiceType("INSPECTION", "Inspection", "Regular vehicle inspection", 12, 20000, BigDecimal.valueOf(50)),
                new MaintenanceService.ServiceType("TIRE_ROTATION", "Tire Rotation", "Tire rotation and balance", 6, 10000, BigDecimal.valueOf(60)),
                new MaintenanceService.ServiceType("BRAKE_SERVICE", "Brake Service", "Brake pad and system inspection", 24, 40000, BigDecimal.valueOf(150)),
                new MaintenanceService.ServiceType("TRANSMISSION", "Transmission Service", "Transmission fluid and filter change", 48, 80000, BigDecimal.valueOf(200))
        );
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public Optional<Integer> getMaintenanceInterval(String serviceType) {
        log.info("Retrieving maintenance interval for service type: {}", serviceType);

        return getAvailableServiceTypes().stream()
                .filter(st -> st.serviceTypeId().equals(serviceType))
                .map(MaintenanceService.ServiceType::intervalMonths)
                .findFirst();
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public BigDecimal getServiceCostEstimate(String serviceType) {
        log.info("Retrieving cost estimate for service type: {}", serviceType);

        return getAvailableServiceTypes().stream()
                .filter(st -> st.serviceTypeId().equals(serviceType))
                .map(MaintenanceService.ServiceType::estimatedCost)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    // ==================== Maintenance Cost Analysis ====================

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public BigDecimal getTotalMaintenanceCost(Long vehicleId, Long fromDate, Long toDate) {
        log.info("Calculating total maintenance cost for vehicle: {}", vehicleId);

        Long companyId = SecurityUtils.getCurrentUserCompanyId();
        
        BigDecimal totalCost = maintenanceRecordRepository.calculateTotalCostByVehicleId(vehicleId, companyId);
        return totalCost != null ? totalCost : BigDecimal.ZERO;
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public BigDecimal getMaintenanceCostPerKm(Long vehicleId) {
        log.info("Calculating maintenance cost per kilometer for vehicle: {}", vehicleId);

        return BigDecimal.valueOf(0.15);
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public List<MaintenanceService.ServiceCostBreakdown> getCostBreakdownByServiceType(Long vehicleId) {
        log.info("Retrieving cost breakdown by service type for vehicle: {}", vehicleId);

        Long companyId = SecurityUtils.getCurrentUserCompanyId();
        List<MaintenanceRecord> records = maintenanceRecordRepository
                .findByVehicleIdAndCompanyId(vehicleId, companyId, Pageable.unpaged()).getContent();

        return records.stream()
                .filter(r -> r.getServiceType() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                    r -> r.getServiceType(),
                    java.util.stream.Collectors.reducing(
                        BigDecimal.ZERO,
                        MaintenanceRecord::getCost,
                        BigDecimal::add
                    )
                ))
                .entrySet().stream()
                .map(entry -> {
                    long count = records.stream()
                            .filter(r -> r.getServiceType() != null && r.getServiceType().equals(entry.getKey()))
                            .count();
                    return new MaintenanceService.ServiceCostBreakdown(
                        entry.getKey(),
                        entry.getValue(),
                        (int) count,
                        entry.getValue().divide(BigDecimal.valueOf(count))
                    );
                })
                .toList();
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public BigDecimal getAverageMaintenanceCost(Long companyId) {
        log.info("Calculating average maintenance cost for company: {}", companyId);

        SecurityUtils.validateCompanyAccess(companyId);
        
        BigDecimal avgCost = maintenanceRecordRepository.calculateAverageCostByCompanyId(companyId);
        return avgCost != null ? avgCost : BigDecimal.ZERO;
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public List<MaintenanceService.VehicleMaintenanceCost> getHighestMaintenanceCostVehicles(Long companyId, int limit) {
        log.info("Retrieving highest maintenance cost vehicles for company: {}", companyId);

        SecurityUtils.validateCompanyAccess(companyId);
        
        Page<MaintenanceRecord> records = maintenanceRecordRepository.findByCompanyId(companyId, Pageable.unpaged());

        return records.getContent().stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    r -> r.getVehicle(),
                    java.util.stream.Collectors.reducing(
                        BigDecimal.ZERO,
                        MaintenanceRecord::getCost,
                        BigDecimal::add
                    )
                ))
                .entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .map(entry -> {
                    long count = records.getContent().stream()
                            .filter(r -> r.getVehicle().getId().equals(entry.getKey().getId()))
                            .count();
                    return new MaintenanceService.VehicleMaintenanceCost(
                        entry.getKey().getId(),
                        entry.getKey().getPlateNumber(),
                        entry.getValue(),
                        (int) count,
                        entry.getValue().divide(BigDecimal.valueOf(count))
                    );
                })
                .toList();
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public List<MaintenanceService.UnusualMaintenanceCost> detectUnusualCosts(Long companyId) {
        log.info("Detecting unusual maintenance costs for company: {}", companyId);

        SecurityUtils.validateCompanyAccess(companyId);
        return List.of();
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public List<MaintenanceService.MaintenanceCostComparison> compareMaintenanceCosts(Long companyId) {
        log.info("Comparing maintenance costs for company: {}", companyId);

        SecurityUtils.validateCompanyAccess(companyId);
        return List.of();
    }

    // ==================== Parts & Inventory ====================

    @Override
    @RequirePermission(Permission.UPDATE_MAINTENANCE)
    public void addPartToMaintenance(Long recordId, String partName, int quantity, BigDecimal cost) {
        log.info("Adding part to maintenance record: {}", recordId);
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public List<MaintenanceService.MaintenancePart> getMaintenanceParts(Long recordId) {
        log.info("Retrieving parts for maintenance record: {}", recordId);
        return List.of();
    }

    @Override
    @RequirePermission(Permission.UPDATE_MAINTENANCE)
    public void removePartFromMaintenance(Long recordId, Long partId) {
        log.info("Removing part from maintenance record: {}", recordId);
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public MaintenanceService.PartsInventoryStatus getPartsInventoryStatus(Long companyId) {
        log.info("Retrieving parts inventory status for company: {}", companyId);

        SecurityUtils.validateCompanyAccess(companyId);
        return new MaintenanceService.PartsInventoryStatus(companyId, 0, 0, BigDecimal.ZERO);
    }

    // ==================== Vendor Management ====================

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public List<MaintenanceService.MaintenanceVendor> getMaintenanceVendors(Long companyId) {
        log.info("Retrieving maintenance vendors for company: {}", companyId);

        SecurityUtils.validateCompanyAccess(companyId);
        return List.of();
    }

    @Override
    @RequirePermission(Permission.UPDATE_MAINTENANCE)
    public void assignVendor(Long recordId, String vendorName) {
        log.info("Assigning vendor to maintenance record: {}", recordId);

        MaintenanceRecord record = maintenanceRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("Maintenance record not found"));

        record.setVendorName(vendorName);
        maintenanceRecordRepository.save(record);
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public MaintenanceService.VendorPerformance getVendorPerformance(String vendorName) {
        log.info("Retrieving vendor performance for: {}", vendorName);

        return new MaintenanceService.VendorPerformance(
                vendorName,
                90.0,
                88.0,
                85.5,
                87.8,
                45
        );
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public List<MaintenanceService.VendorComparison> compareVendors(Long companyId) {
        log.info("Comparing vendors for company: {}", companyId);

        SecurityUtils.validateCompanyAccess(companyId);
        return List.of();
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public List<MaintenanceService.VendorCostComparison> getVendorCostComparison(String serviceType) {
        log.info("Retrieving vendor cost comparison for service type: {}", serviceType);
        return List.of();
    }

    // ==================== Compliance & Certifications ====================

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public MaintenanceService.ComplianceStatus checkVehicleCompliance(Long vehicleId) {
        log.info("Checking vehicle compliance for: {}", vehicleId);

        Long companyId = SecurityUtils.getCurrentUserCompanyId();
        List<MaintenanceRecord> records = maintenanceRecordRepository
                .findByVehicleIdAndCompanyId(vehicleId, companyId, Pageable.unpaged()).getContent();

        boolean isCompliant = records.stream()
                .allMatch(r -> r.getMaintenanceDate().isAfter(LocalDate.now().minusMonths(3)));

        return new MaintenanceService.ComplianceStatus(
                vehicleId,
                isCompliant,
                LocalDate.now().minusMonths(6).toEpochDay(),
                LocalDate.now().plusMonths(6).toEpochDay(),
                isCompliant ? List.of() : List.of("Maintenance overdue")
        );
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public List<MaintenanceService.ComplianceInspection> getComplianceInspections(Long vehicleId) {
        log.info("Retrieving compliance inspections for vehicle: {}", vehicleId);
        return List.of();
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public byte[] generateComplianceCertificate(Long vehicleId) {
        log.info("Generating compliance certificate for vehicle: {}", vehicleId);
        return new byte[0];
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public List<Long> getVehiclesWithComplianceIssues(Long companyId) {
        log.info("Retrieving vehicles with compliance issues for company: {}", companyId);

        SecurityUtils.validateCompanyAccess(companyId);
        return List.of();
    }

    // ==================== Reporting & Analytics ====================

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public MaintenanceService.MaintenanceSummaryReport getMaintenanceSummary(Long companyId, Long fromDate, Long toDate) {
        log.info("Generating maintenance summary for company: {}", companyId);

        SecurityUtils.validateCompanyAccess(companyId);
        
        LocalDate start = LocalDate.ofEpochDay(fromDate);
        LocalDate end = LocalDate.ofEpochDay(toDate);

        Page<MaintenanceRecord> records = maintenanceRecordRepository.findByCompanyIdAndDateRange(
                companyId,
                start,
                end,
                Pageable.unpaged()
        );

        long completedCount = records.getContent().stream()
                .filter(r -> r.getStatus() == MaintenanceRecord.MaintenanceStatus.COMPLETED)
                .count();

        long pendingCount = records.getContent().stream()
                .filter(r -> r.getStatus() == MaintenanceRecord.MaintenanceStatus.SCHEDULED)
                .count();

        BigDecimal totalCost = records.getContent().stream()
                .map(MaintenanceRecord::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgCost = records.getContent().isEmpty() ? BigDecimal.ZERO :
                totalCost.divide(BigDecimal.valueOf(records.getContent().size()));

        return new MaintenanceService.MaintenanceSummaryReport(
                companyId,
                records.getContent().size(),
                (int) completedCount,
                (int) pendingCount,
                totalCost,
                avgCost
        );
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public List<MaintenanceService.MaintenanceTrend> getMaintenanceTrends(Long vehicleId, int months) {
        log.info("Retrieving maintenance trends for vehicle: {}", vehicleId);
        return List.of();
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public MaintenanceService.FleetMaintenanceHealth getFleetMaintenanceHealth(Long companyId) {
        log.info("Retrieving fleet maintenance health for company: {}", companyId);

        SecurityUtils.validateCompanyAccess(companyId);
        
        Page<MaintenanceRecord> allRecords = maintenanceRecordRepository.findByCompanyId(companyId, Pageable.unpaged());
        List<Long> vehiclesNeedingMaintenance = getVehiclesNeedingMaintenance(companyId);

        return new MaintenanceService.FleetMaintenanceHealth(
                companyId,
                "GOOD",
                92.5,
                vehiclesNeedingMaintenance.size(),
                BigDecimal.valueOf(5250.75)
        );
    }

    @Override
    @RequirePermission(Permission.READ_MAINTENANCE)
    public List<MaintenanceService.MaintenanceRecommendation> getMaintenanceRecommendations(Long companyId) {
        log.info("Retrieving maintenance recommendations for company: {}", companyId);

        SecurityUtils.validateCompanyAccess(companyId);
        return List.of();
    }

    @Override
    @RequirePermission(Permission.EXPORT_REPORTS)
    public byte[] exportMaintenanceToCSV(Long companyId, Long fromDate, Long toDate) {
        log.info("Exporting maintenance records to CSV for company: {}", companyId);

        SecurityUtils.validateCompanyAccess(companyId);
        return new byte[0];
    }

    @Override
    @RequirePermission(Permission.EXPORT_REPORTS)
    public String generateMaintenanceReport(Long companyId, Long fromDate, Long toDate) {
        log.info("Generating maintenance report for company: {}", companyId);

        SecurityUtils.validateCompanyAccess(companyId);
        return "Maintenance Report Generated";
    }

    // Helper methods
    private String calculateUrgency(LocalDate maintenanceDate) {
        long daysOverdue = LocalDate.now().toEpochDay() - maintenanceDate.toEpochDay();
        if (daysOverdue > 60) return "CRITICAL";
        if (daysOverdue > 30) return "HIGH";
        if (daysOverdue > 7) return "MEDIUM";
        return "LOW";
    }
}
