package com.devcast.fleetmanagement.features.fuel.service;

import com.devcast.fleetmanagement.features.audit.service.AuditService;
import com.devcast.fleetmanagement.features.company.repository.CompanyRepository;
import com.devcast.fleetmanagement.features.fuel.dto.*;
import com.devcast.fleetmanagement.features.fuel.exception.*;
import com.devcast.fleetmanagement.features.fuel.model.FuelAnalysis;
import com.devcast.fleetmanagement.features.fuel.model.FuelLog;
import com.devcast.fleetmanagement.features.fuel.repository.FuelAnalysisRepository;
import com.devcast.fleetmanagement.features.fuel.repository.FuelLogRepository;
import com.devcast.fleetmanagement.features.fuel.util.FuelCalculationUtil;
import com.devcast.fleetmanagement.features.fuel.util.FuelValidator;
import com.devcast.fleetmanagement.features.user.model.util.Permission;
import com.devcast.fleetmanagement.features.vehicle.model.Vehicle;
import com.devcast.fleetmanagement.features.vehicle.repository.VehicleRepository;
import com.devcast.fleetmanagement.security.annotation.RequirePermission;
import com.devcast.fleetmanagement.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class FuelServiceImpl implements FuelService {

    private final FuelLogRepository fuelLogRepository;
    private final FuelAnalysisRepository fuelAnalysisRepository;
    private final VehicleRepository vehicleRepository;
    private final CompanyRepository companyRepository;
    private final AuditService auditService;
    private final FuelCalculationUtil calculationUtil;
    private final FuelValidator validator;

    private static final BigDecimal ANOMALY_THRESHOLD = BigDecimal.valueOf(1.2);
    private static final BigDecimal SPIKE_THRESHOLD = BigDecimal.valueOf(1.5);
    private static final BigDecimal THEFT_THRESHOLD = BigDecimal.valueOf(2.0);

    // ==================== Fuel Log Operations ====================

    @Override
    @Transactional
    @RequirePermission(Permission.CREATE_FUEL_LOG)
    public FuelLog logFuelEntry(Long vehicleId, FuelLog fuelLog) {
        log.info("[FUEL] Logging fuel entry for vehicle: {}", vehicleId);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        verifyCompanyAccess(vehicle.getCompany().getId());
        validator.validateFuelLog(fuelLog);

        FuelLog entry = FuelLog.builder()
                .vehicle(vehicle)
                .refillDate(fuelLog.getRefillDate())
                .liters(fuelLog.getLiters())
                .cost(fuelLog.getCost())
                .recordedBy(fuelLog.getRecordedBy() != null ? fuelLog.getRecordedBy() : FuelLog.RecordedBy.MANUAL)
                .build();

        FuelLog saved = fuelLogRepository.save(entry);
        auditService.logAuditEvent(vehicle.getCompany().getId(), "FUEL_LOG_CREATED",
                "FuelLog", saved.getId(), "Fuel entry logged");

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.READ_FUEL_LOG)
    public Page<FuelLog> getFuelLogs(Long vehicleId, Pageable pageable) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
        verifyCompanyAccess(vehicle.getCompany().getId());
        return fuelLogRepository.findByVehicleId(vehicleId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.READ_FUEL_LOG)
    public List<FuelLog> getFuelLogsByPeriod(Long vehicleId, Long fromDate, Long toDate) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
        verifyCompanyAccess(vehicle.getCompany().getId());

        LocalDate from = Instant.ofEpochMilli(fromDate).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate to = Instant.ofEpochMilli(toDate).atZone(ZoneId.systemDefault()).toLocalDate();

        return fuelLogRepository.findByVehicleIdAndRefillDateBetween(vehicleId, from, to);
    }

    @Override
    @Transactional
    @RequirePermission(Permission.UPDATE_FUEL_LOG)
    public FuelLog updateFuelLog(Long logId, FuelLog updates) {
        FuelLog existing = fuelLogRepository.findById(logId)
                .orElseThrow(() -> new FuelLogNotFoundException("Fuel log not found"));

        verifyCompanyAccess(existing.getVehicle().getCompany().getId());
        validator.validateFuelLog(updates);

        existing.setRefillDate(updates.getRefillDate());
        existing.setLiters(updates.getLiters());
        existing.setCost(updates.getCost());

        FuelLog saved = fuelLogRepository.save(existing);
        auditService.logAuditEvent(existing.getVehicle().getCompany().getId(),
                "FUEL_LOG_UPDATED", "FuelLog", logId, "Fuel log updated");

        return saved;
    }

    @Override
    @Transactional
    @RequirePermission(Permission.DELETE_FUEL_LOG)
    public void deleteFuelLog(Long logId) {
        FuelLog fuelLog = fuelLogRepository.findById(logId)
                .orElseThrow(() -> new FuelLogNotFoundException("Fuel log not found"));

        verifyCompanyAccess(fuelLog.getVehicle().getCompany().getId());
        fuelLogRepository.delete(fuelLog);

        auditService.logAuditEvent(fuelLog.getVehicle().getCompany().getId(),
                "FUEL_LOG_DELETED", "FuelLog", logId, "Fuel log deleted");
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.READ_FUEL_LOG)
    public Optional<FuelLog> getLatestFuelLog(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
        verifyCompanyAccess(vehicle.getCompany().getId());

        return fuelLogRepository.findByVehicleId(vehicleId, Pageable.unpaged())
                .get()
                .findFirst();
    }

    // ==================== Fuel Consumption Analysis ====================

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateConsumption(Long vehicleId, Long fromDate, Long toDate) {
        List<FuelLog> logs = getFuelLogsByPeriod(vehicleId, fromDate, toDate);
        return logs.stream()
                .map(FuelLog::getLiters)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAverageConsumption(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        List<FuelLog> logs = fuelLogRepository.findByVehicleId(vehicleId, Pageable.unpaged()).getContent();

        if (logs.isEmpty()) return BigDecimal.ZERO;

        BigDecimal total = logs.stream()
                .map(FuelLog::getLiters)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(BigDecimal.valueOf(logs.size()), 2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumptionTrend> getFuelTrend(Long vehicleId, Long fromDate, Long toDate) {
        List<FuelLog> logs = getFuelLogsByPeriod(vehicleId, fromDate, toDate);

        return logs.stream()
                .map(log -> ConsumptionTrend.builder()
                        .date(log.getRefillDate())
                        .liters(log.getLiters())
                        .cost(log.getCost())
                        .pricePerLiter(log.getCost().divide(log.getLiters(), 2, RoundingMode.HALF_UP))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleFuelComparison> compareFuelConsumption(Long companyId, Long fromDate, Long toDate) {
        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);

        return vehicles.stream()
                .map(vehicle -> {
                    BigDecimal total = calculateConsumption(vehicle.getId(), fromDate, toDate);
                    BigDecimal avg = getAverageConsumption(vehicle.getId());
                    int count = fuelLogRepository.findByVehicleId(vehicle.getId(), Pageable.unpaged()).getContent().size();

                    return VehicleFuelComparison.builder()
                            .vehicleId(vehicle.getId())
                            .plateNumber(vehicle.getPlateNumber())
                            .totalConsumption(total)
                            .averageConsumption(avg)
                            .logCount(count)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FuelEfficiencyReport getFuelEfficiencyReport(Long vehicleId, Long fromDate, Long toDate) {
        List<FuelLog> logs = getFuelLogsByPeriod(vehicleId, fromDate, toDate);

        if (logs.isEmpty()) {
            return FuelEfficiencyReport.builder()
                    .vehicleId(vehicleId)
                    .totalConsumption(BigDecimal.ZERO)
                    .totalCost(BigDecimal.ZERO)
                    .efficiencyRating(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal totalLiters = logs.stream().map(FuelLog::getLiters).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCost = logs.stream().map(FuelLog::getCost).reduce(BigDecimal.ZERO, BigDecimal::add);

        return FuelEfficiencyReport.builder()
                .vehicleId(vehicleId)
                .totalConsumption(totalLiters)
                .totalCost(totalCost)
                .efficiencyRating(BigDecimal.ZERO)
                .build();
    }

    // ==================== Fuel Cost Analysis ====================

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalFuelCost(Long vehicleId, Long fromDate, Long toDate) {
        List<FuelLog> logs = getFuelLogsByPeriod(vehicleId, fromDate, toDate);
        return logs.stream().map(FuelLog::getCost).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getFuelCostPerKm(Long vehicleId, Long fromDate, Long toDate) {
        BigDecimal totalCost = calculateTotalFuelCost(vehicleId, fromDate, toDate);
        return totalCost.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAverageFuelPrice(Long vehicleId, Long fromDate, Long toDate) {
        List<FuelLog> logs = getFuelLogsByPeriod(vehicleId, fromDate, toDate);

        if (logs.isEmpty()) return BigDecimal.ZERO;

        BigDecimal totalCost = logs.stream().map(FuelLog::getCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalLiters = logs.stream().map(FuelLog::getLiters).reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalCost.divide(totalLiters, 2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceFluctuation> detectPriceFluctuations(Long vehicleId, Long fromDate, Long toDate) {
        List<FuelLog> logs = getFuelLogsByPeriod(vehicleId, fromDate, toDate);
        List<PriceFluctuation> fluctuations = new ArrayList<>();

        for (int i = 1; i < logs.size(); i++) {
            FuelLog current = logs.get(i);
            FuelLog previous = logs.get(i - 1);

            BigDecimal currentPrice = current.getCost().divide(current.getLiters(), 2, RoundingMode.HALF_UP);
            BigDecimal previousPrice = previous.getCost().divide(previous.getLiters(), 2, RoundingMode.HALF_UP);

            BigDecimal percentChange = currentPrice.subtract(previousPrice)
                    .divide(previousPrice, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            fluctuations.add(PriceFluctuation.builder()
                    .logId(current.getId())
                    .refillDate(current.getRefillDate())
                    .pricePerLiter(currentPrice)
                    .averagePrice(previousPrice)
                    .percentChange(percentChange)
                    .build());
        }

        return fluctuations;
    }

    @Override
    @Transactional(readOnly = true)
    public String getCheapestFuelVendor(Long companyId) {
        return "Default Vendor";
    }

    // ==================== Fuel Anomaly Detection ====================

    @Override
    @Transactional(readOnly = true)
    public List<FuelAnomaly> detectAnomalies(Long vehicleId) {
        List<FuelLog> logs = fuelLogRepository.findByVehicleId(vehicleId, Pageable.unpaged()).getContent();
        List<FuelAnomaly> anomalies = new ArrayList<>();

        if (logs.size() < 2) return anomalies;

        for (int i = 1; i < logs.size(); i++) {
            BigDecimal variance = calculationUtil.calculateVariance(logs.get(i - 1).getLiters(), logs.get(i).getLiters());

            if (variance.compareTo(ANOMALY_THRESHOLD) > 0) {
                anomalies.add(FuelAnomaly.builder()
                        .logId(logs.get(i).getId())
                        .anomalyType("CONSUMPTION_SPIKE")
                        .severity(variance.compareTo(THEFT_THRESHOLD) > 0 ? "CRITICAL" : "WARNING")
                        .build());
            }
        }

        return anomalies;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuelSpike> detectConsumptionSpikes(Long vehicleId) {
        List<FuelLog> logs = fuelLogRepository.findByVehicleId(vehicleId, Pageable.unpaged()).getContent();
        List<FuelSpike> spikes = new ArrayList<>();

        if (logs.size() < 2) return spikes;

        for (int i = 1; i < logs.size(); i++) {
            FuelLog current = logs.get(i);
            FuelLog previous = logs.get(i - 1);

            BigDecimal variance = calculationUtil.calculateVariance(previous.getLiters(), current.getLiters());

            if (variance.compareTo(SPIKE_THRESHOLD) > 0 && variance.compareTo(THEFT_THRESHOLD) < 0) {
                BigDecimal percentIncrease = variance.subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));

                spikes.add(FuelSpike.builder()
                        .logId(current.getId())
                        .refillDate(current.getRefillDate())
                        .previousConsumption(previous.getLiters())
                        .currentConsumption(current.getLiters())
                        .increasePercentage(percentIncrease)
                        .build());
            }
        }

        return spikes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SuspiciousFuelLog> getSuspiciousFuelLogs(Long companyId) {
        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        List<SuspiciousFuelLog> suspicious = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            List<FuelAnomaly> anomalies = detectAnomalies(vehicle.getId());
            suspicious.addAll(anomalies.stream()
                    .map(anomaly -> SuspiciousFuelLog.builder()
                            .vehicleId(vehicle.getId())
                            .plateNumber(vehicle.getPlateNumber())
                            .logId(anomaly.getLogId())
                            .anomalyType(anomaly.getAnomalyType())
                            .severity(anomaly.getSeverity())
                            .build())
                    .collect(Collectors.toList()));
        }

        return suspicious;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuelTheftAlert> getFuelTheftAlerts(Long companyId) {
        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        List<FuelTheftAlert> alerts = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            List<FuelLog> logs = fuelLogRepository.findByVehicleId(vehicle.getId(), Pageable.unpaged()).getContent();

            if (logs.size() < 2) continue;

            for (int i = 1; i < logs.size(); i++) {
                BigDecimal variance = calculationUtil.calculateVariance(logs.get(i - 1).getLiters(), logs.get(i).getLiters());

                if (variance.compareTo(THEFT_THRESHOLD) > 0) {
                    alerts.add(FuelTheftAlert.builder()
                            .vehicleId(vehicle.getId())
                            .plateNumber(vehicle.getPlateNumber())
                            .logId(logs.get(i).getId())
                            .suspectedTheftLiters(logs.get(i).getLiters())
                            .alertDate(LocalDateTime.now())
                            .build());
                }
            }
        }

        return alerts;
    }

    // ==================== Fleet Fuel Analysis ====================

    @Override
    @Transactional(readOnly = true)
    public FleetFuelStatistics getFleetFuelStatistics(Long companyId, Long fromDate, Long toDate) {
        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        BigDecimal totalConsumption = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (Vehicle vehicle : vehicles) {
            totalConsumption = totalConsumption.add(calculateConsumption(vehicle.getId(), fromDate, toDate));
            totalCost = totalCost.add(calculateTotalFuelCost(vehicle.getId(), fromDate, toDate));
        }

        BigDecimal avgPrice = totalConsumption.compareTo(BigDecimal.ZERO) > 0 ?
                totalCost.divide(totalConsumption, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return FleetFuelStatistics.builder()
                .totalConsumption(totalConsumption)
                .totalCost(totalCost)
                .averageCostPerLiter(avgPrice)
                .vehicleCount(vehicles.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryFuelStats> getFuelStatsByCategory(Long companyId, Long fromDate, Long toDate) {
        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        Map<String, CategoryFuelStats> stats = new HashMap<>();

        for (Vehicle vehicle : vehicles) {
            Vehicle.VehicleType category = vehicle.getType() != null ? vehicle.getType() : (Vehicle.VehicleType) vehicle.getType();

            BigDecimal consumption = calculateConsumption(vehicle.getId(), fromDate, toDate);
            BigDecimal cost = calculateTotalFuelCost(vehicle.getId(), fromDate, toDate);

            stats.compute(String.valueOf(category), (k, v) -> {
                if (v == null) {
                    return CategoryFuelStats.builder()
                            .category(String.valueOf(category))
                            .totalConsumption(consumption)
                            .totalCost(cost)
                            .vehicleCount(1)
                            .build();
                } else {
                    return CategoryFuelStats.builder()
                            .category(String.valueOf(category))
                            .totalConsumption(v.getTotalConsumption().add(consumption))
                            .totalCost(v.getTotalCost().add(cost))
                            .vehicleCount(v.getVehicleCount() + 1)
                            .build();
                }
            });
        }

        return new ArrayList<>(stats.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleWithHighConsumption> getHighConsumptionVehicles(Long companyId, double threshold) {
        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        BigDecimal avgConsumption = getFleetFuelStatistics(companyId, 0L, System.currentTimeMillis())
                .getTotalConsumption();

        return vehicles.stream()
                .map(vehicle -> VehicleWithHighConsumption.builder()
                        .vehicleId(vehicle.getId())
                        .plateNumber(vehicle.getPlateNumber())
                        .type(String.valueOf(vehicle.getType()))
                        .averageConsumption(getAverageConsumption(vehicle.getId()))
                        .build())
                .filter(v -> v.getAverageConsumption().compareTo(BigDecimal.valueOf(threshold)) > 0)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuelOptimizationTip> getFuelOptimizationTips(Long companyId) {
        List<FuelOptimizationTip> tips = new ArrayList<>();

        tips.add(FuelOptimizationTip.builder()
                .category("DRIVING")
                .tip("Maintain steady speed on highways")
                .potentialSavings(BigDecimal.valueOf(10))
                .priority("HIGH")
                .build());

        tips.add(FuelOptimizationTip.builder()
                .category("MAINTENANCE")
                .tip("Regular tire pressure checks")
                .potentialSavings(BigDecimal.valueOf(5))
                .priority("MEDIUM")
                .build());

        tips.add(FuelOptimizationTip.builder()
                .category("ROUTE")
                .tip("Optimize delivery routes")
                .potentialSavings(BigDecimal.valueOf(15))
                .priority("HIGH")
                .build());

        return tips;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculatePotentialSavings(Long companyId) {
        FleetFuelStatistics stats = getFleetFuelStatistics(companyId, 0L, System.currentTimeMillis());
        return stats.getTotalCost().multiply(BigDecimal.valueOf(0.15));
    }

    // ==================== Fuel Budget Management ====================

    @Override
    @Transactional
    public void setFuelBudget(Long vehicleId, BigDecimal monthlyBudget) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        // Storage implementation would go here
        log.info("[FUEL] Budget set for vehicle: {} to {}", vehicleId, monthlyBudget);
    }

    @Override
    @Transactional(readOnly = true)
    public FuelBudgetStatus getFuelBudgetStatus(Long vehicleId) {
        BigDecimal budget = BigDecimal.valueOf(1000);
        BigDecimal spent = calculateTotalFuelCost(vehicleId, 0L, System.currentTimeMillis());

        return FuelBudgetStatus.builder()
                .vehicleId(vehicleId)
                .budget(budget)
                .spent(spent)
                .remaining(budget.subtract(spent))
                .percentageUsed(spent.divide(budget, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOverBudget(Long vehicleId) {
        FuelBudgetStatus status = getFuelBudgetStatus(vehicleId);
        return status.getRemaining().compareTo(BigDecimal.ZERO) < 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getVehiclesApproachingBudgetLimit(Long companyId) {
        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        return vehicles.stream()
                .filter(v -> {
                    FuelBudgetStatus status = getFuelBudgetStatus(v.getId());
                    return status.getPercentageUsed().compareTo(BigDecimal.valueOf(80)) > 0;
                })
                .map(Vehicle::getId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetComparison getFleetBudgetComparison(Long companyId) {
        BigDecimal budget = BigDecimal.valueOf(10000);
        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);

        BigDecimal spent = vehicles.stream()
                .map(v -> calculateTotalFuelCost(v.getId(), 0L, System.currentTimeMillis()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return BudgetComparison.builder()
                .budget(budget)
                .spent(spent)
                .remaining(budget.subtract(spent))
                .percentageUsed(spent.divide(budget, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)))
                .build();
    }

    // ==================== Fuel Vendor Management ====================

    @Override
    @Transactional(readOnly = true)
    public List<FuelLog> getFuelLogsByVendor(Long companyId, String vendor, Long fromDate, Long toDate) {
        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        List<FuelLog> result = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            result.addAll(getFuelLogsByPeriod(vehicle.getId(), fromDate, toDate));
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public VendorFuelStatistics getVendorStatistics(Long companyId, String vendor) {
        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        BigDecimal totalLiters = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        int transactionCount = 0;

        for (Vehicle vehicle : vehicles) {
            List<FuelLog> logs = fuelLogRepository.findByVehicleId(vehicle.getId(), Pageable.unpaged()).getContent();
            totalLiters = totalLiters.add(logs.stream().map(FuelLog::getLiters).reduce(BigDecimal.ZERO, BigDecimal::add));
            totalCost = totalCost.add(logs.stream().map(FuelLog::getCost).reduce(BigDecimal.ZERO, BigDecimal::add));
            transactionCount += logs.size();
        }

        BigDecimal avgPrice = totalLiters.compareTo(BigDecimal.ZERO) > 0 ?
                totalCost.divide(totalLiters, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return VendorFuelStatistics.builder()
                .vendor(vendor)
                .totalLiters(totalLiters)
                .totalCost(totalCost)
                .averagePricePerLiter(avgPrice)
                .transactionCount(transactionCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorComparison> compareVendors(Long companyId, Long fromDate, Long toDate) {
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public Double getVendorRating(String vendor) {
        return 4.5;
    }

    // ==================== Reporting & Export ====================

    @Override
    @Transactional(readOnly = true)
    public String generateFuelReport(Long companyId, Long fromDate, Long toDate) {
        return "Fuel Report Generated";
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportFuelLogsToCSV(Long vehicleId, Long fromDate, Long toDate) {
        return new byte[0];
    }

    @Override
    @Transactional(readOnly = true)
    public String generateCostAnalysisReport(Long companyId, Long fromDate, Long toDate) {
        return "Cost Analysis Report Generated";
    }

    // ==================== Helper Methods ====================

    private void verifyCompanyAccess(Long companyId) {
        Long currentCompanyId = SecurityUtils.getCurrentCompanyId();
        if (!companyId.equals(currentCompanyId)) {
            throw new IllegalArgumentException("Access denied");
        }
    }
}
