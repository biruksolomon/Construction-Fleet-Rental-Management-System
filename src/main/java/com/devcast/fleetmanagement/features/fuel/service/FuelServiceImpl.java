package com.devcast.fleetmanagement.features.fuel.service;

import com.devcast.fleetmanagement.features.audit.service.AuditService;
import com.devcast.fleetmanagement.features.company.repository.CompanyRepository;
import com.devcast.fleetmanagement.features.fuel.dto.*;
import com.devcast.fleetmanagement.features.fuel.model.FuelAnalysis;
import com.devcast.fleetmanagement.features.fuel.model.FuelLog;
import com.devcast.fleetmanagement.features.fuel.repository.FuelLogRepository;
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

/**
 * Fuel Service Implementation
 * Comprehensive fuel tracking, consumption analysis, and cost management
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class FuelServiceImpl implements FuelService {

    private final FuelLogRepository fuelLogRepository;
    private final VehicleRepository vehicleRepository;
    private final CompanyRepository companyRepository;
    private final AuditService auditService;

    // Constants
    private static final BigDecimal FUEL_ANOMALY_THRESHOLD = BigDecimal.valueOf(1.2); // 20% variance
    private static final BigDecimal FUEL_SPIKE_THRESHOLD = BigDecimal.valueOf(1.5); // 50% increase
    private static final BigDecimal THEFT_THRESHOLD = BigDecimal.valueOf(2.0); // 100% increase

    // ==================== Fuel Log Operations ====================

    @Override
    @Transactional
    @RequirePermission(Permission.CREATE_FUEL_LOG)
    public FuelLog logFuelEntry(Long vehicleId, FuelLog fuelLog) {
        log.info("Logging fuel entry for vehicle: {}", vehicleId);

        // Verify vehicle exists and get company context
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        verifyCompanyAccess(vehicle.getCompany().getId());

        // Build fuel log with vehicle association
        FuelLog log = FuelLog.builder()
                .vehicle(vehicle)
                .refillDate(fuelLog.getRefillDate())
                .liters(fuelLog.getLiters())
                .cost(fuelLog.getCost())
                .recordedBy(fuelLog.getRecordedBy() != null ? fuelLog.getRecordedBy() : FuelLog.RecordedBy.MANUAL)
                .build();

        FuelLog saved = fuelLogRepository.save(log);

        auditService.logAuditEvent(vehicle.getCompany().getId(), "FUEL_LOG_CREATED",
                "FuelLog", saved.getId(),
                "Fuel refill logged: " + fuelLog.getLiters() + " liters for vehicle " + vehicleId);

        log.info("Fuel entry logged successfully for vehicle: {} (ID: {})", vehicleId, saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.READ_FUEL_LOG)
    public Page<FuelLog> getFuelLogs(Long vehicleId, Pageable pageable) {
        log.debug("Retrieving fuel logs for vehicle: {}", vehicleId);

        // Verify vehicle and company access
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        verifyCompanyAccess(vehicle.getCompany().getId());

        return fuelLogRepository.findByVehicleId(vehicleId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.READ_FUEL_LOG)
    public List<FuelLog> getFuelLogsByPeriod(Long vehicleId, Long fromDate, Long toDate) {
        log.debug("Retrieving fuel logs for vehicle: {} from {} to {}", vehicleId, fromDate, toDate);

        // Verify vehicle and company access
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        verifyCompanyAccess(vehicle.getCompany().getId());

        LocalDate startDate = Instant.ofEpochMilli(fromDate).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(toDate).atZone(ZoneId.systemDefault()).toLocalDate();

        return fuelLogRepository.findByVehicleIdAndDateRange(vehicleId, startDate, endDate);
    }

    @Override
    @Transactional
    @RequirePermission(Permission.UPDATE_FUEL_LOG)
    public FuelLog updateFuelLog(Long logId, FuelLog fuelLog) {
        log.info("Updating fuel log: {}", logId);

        FuelLog existing = fuelLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Fuel log not found: " + logId));

        verifyCompanyAccess(existing.getVehicle().getCompany().getId());

        existing.setLiters(fuelLog.getLiters());
        existing.setCost(fuelLog.getCost());
        existing.setRefillDate(fuelLog.getRefillDate());
        existing.setRecordedBy(fuelLog.getRecordedBy());

        FuelLog updated = fuelLogRepository.save(existing);

        auditService.logAuditEvent(existing.getVehicle().getCompany().getId(), "FUEL_LOG_UPDATED",
                "FuelLog", updated.getId(), "Fuel log updated");

        log.info("Fuel log updated successfully: {}", logId);
        return updated;
    }

    @Override
    @Transactional
    @RequirePermission(Permission.DELETE_FUEL_LOG)
    public void deleteFuelLog(Long logId) {
        log.info("Deleting fuel log: {}", logId);

        FuelLog fuelLog = fuelLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Fuel log not found: " + logId));

        verifyCompanyAccess(fuelLog.getVehicle().getCompany().getId());

        auditService.logAuditEvent(fuelLog.getVehicle().getCompany().getId(), "FUEL_LOG_DELETED",
                "FuelLog", logId, "Fuel log deleted");

        fuelLogRepository.deleteById(logId);

        log.info("Fuel log deleted successfully: {}", logId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FuelLog> getLatestFuelLog(Long vehicleId) {
        log.debug("Retrieving latest fuel log for vehicle: {}", vehicleId);

        // Verify vehicle and company access
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        verifyCompanyAccess(vehicle.getCompany().getId());

        return fuelLogRepository.findLatestByVehicleId(vehicleId);
    }

    // ==================== Fuel Consumption Analysis ====================

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateConsumption(Long vehicleId, Long fromDate, Long toDate) {
        log.debug("Calculating fuel consumption for vehicle: {} from {} to {}", vehicleId, fromDate, toDate);

        // Verify vehicle and company access
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        verifyCompanyAccess(vehicle.getCompany().getId());

        LocalDate startDate = Instant.ofEpochMilli(fromDate).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(toDate).atZone(ZoneId.systemDefault()).toLocalDate();

        Optional<BigDecimal> totalConsumption = fuelLogRepository.calculateTotalFuelConsumed(vehicleId);

        return totalConsumption.orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAverageConsumption(Long vehicleId) {
        log.debug("Retrieving average fuel consumption for vehicle: {}", vehicleId);

        // Verify vehicle and company access
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        verifyCompanyAccess(vehicle.getCompany().getId());

        List<FuelLog> logs = fuelLogRepository.findByVehicleId(vehicleId, Pageable.unpaged()).getContent();

        if (logs.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalLiters = logs.stream()
                .map(FuelLog::getLiters)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalLiters.divide(BigDecimal.valueOf(logs.size()), 2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsumptionTrend> getFuelTrend(Long vehicleId, Long fromDate, Long toDate) {
        log.debug("Retrieving fuel consumption trend for vehicle: {}", vehicleId);

        // Verify vehicle and company access
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        verifyCompanyAccess(vehicle.getCompany().getId());

        LocalDate startDate = Instant.ofEpochMilli(fromDate).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(toDate).atZone(ZoneId.systemDefault()).toLocalDate();

        List<FuelLog> logs = fuelLogRepository.findByVehicleIdAndDateRange(vehicleId, startDate, endDate);

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
        log.debug("Comparing fuel consumption across fleet for company: {}", companyId);

        verifyCompanyAccess(companyId);

        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);

        LocalDate startDate = Instant.ofEpochMilli(fromDate).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(toDate).atZone(ZoneId.systemDefault()).toLocalDate();

        return vehicles.stream()
                .map(vehicle -> {
                    List<FuelLog> logs = fuelLogRepository.findByVehicleIdAndDateRange(vehicle.getId(), startDate, endDate);
                    BigDecimal totalLiters = logs.stream()
                            .map(FuelLog::getLiters)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal avgConsumption = logs.isEmpty() ? BigDecimal.ZERO :
                            totalLiters.divide(BigDecimal.valueOf(logs.size()), 2, RoundingMode.HALF_UP);

                    return VehicleFuelComparison.builder()
                            .vehicleId(vehicle.getId())
                            .plateNumber(vehicle.getPlateNumber())
                            .totalConsumption(totalLiters)
                            .averageConsumption(avgConsumption)
                            .logCount(logs.size())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FuelEfficiencyReport getFuelEfficiencyReport(Long vehicleId, Long fromDate, Long toDate) {
        log.debug("Generating fuel efficiency report for vehicle: {}", vehicleId);

        // Verify vehicle and company access
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        verifyCompanyAccess(vehicle.getCompany().getId());

        LocalDate startDate = Instant.ofEpochMilli(fromDate).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(toDate).atZone(ZoneId.systemDefault()).toLocalDate();

        List<FuelLog> logs = fuelLogRepository.findByVehicleIdAndDateRange(vehicleId, startDate, endDate);

        if (logs.isEmpty()) {
            return FuelEfficiencyReport.builder()
                    .vehicleId(vehicleId)
                    .avgConsumption(BigDecimal.ZERO)
                    .bestConsumption(BigDecimal.ZERO)
                    .worstConsumption(BigDecimal.ZERO)
                    .trend("N/A")
                    .recommendation("Insufficient data")
                    .build();
        }

        BigDecimal avgConsumption = logs.stream()
                .map(FuelLog::getLiters)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(logs.size()), 2, RoundingMode.HALF_UP);

        BigDecimal best = logs.stream()
                .map(FuelLog::getLiters)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal worst = logs.stream()
                .map(FuelLog::getLiters)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        String trend = avgConsumption.compareTo(best) > 0 ? "INCREASING" : "DECREASING";
        String recommendation = trend.equals("INCREASING") ? "Review driving patterns and maintenance" : "Good efficiency";

        return FuelEfficiencyReport.builder()
                .vehicleId(vehicleId)
                .avgConsumption(avgConsumption)
                .bestConsumption(best)
                .worstConsumption(worst)
                .trend(trend)
                .recommendation(recommendation)
                .build();
    }

    // ==================== Fuel Cost Analysis ====================

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalFuelCost(Long vehicleId, Long fromDate, Long toDate) {
        log.debug("Calculating total fuel cost for vehicle: {}", vehicleId);

        // Verify vehicle and company access
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        verifyCompanyAccess(vehicle.getCompany().getId());

        Optional<BigDecimal> totalCost = fuelLogRepository.calculateTotalFuelCost(vehicleId);
        return totalCost.orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getFuelCostPerKm(Long vehicleId, Long fromDate, Long toDate) {
        log.debug("Calculating fuel cost per km for vehicle: {}", vehicleId);

        // Verify vehicle and company access
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        verifyCompanyAccess(vehicle.getCompany().getId());

        // Note: Requires mileage/distance data - placeholder calculation
        BigDecimal totalCost = calculateTotalFuelCost(vehicleId, fromDate, toDate);
        
        // Assuming average distance - adjust based on actual GPS/mileage data
        BigDecimal estimatedDistance = BigDecimal.valueOf(1000); // Placeholder
        
        return totalCost.divide(estimatedDistance, 2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAverageFuelPrice(Long vehicleId, Long fromDate, Long toDate) {
        log.debug("Retrieving average fuel price for vehicle: {}", vehicleId);

        // Verify vehicle and company access
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        verifyCompanyAccess(vehicle.getCompany().getId());

        Optional<BigDecimal> avgPrice = fuelLogRepository.calculateAverageCostPerLiter(vehicleId);
        return avgPrice.orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceFluctuation> detectPriceFluctuations(Long vehicleId, Long fromDate, Long toDate) {
        log.debug("Detecting price fluctuations for vehicle: {}", vehicleId);

        // Verify vehicle and company access
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        verifyCompanyAccess(vehicle.getCompany().getId());

        LocalDate startDate = Instant.ofEpochMilli(fromDate).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(toDate).atZone(ZoneId.systemDefault()).toLocalDate();

        List<FuelLog> logs = fuelLogRepository.findByVehicleIdAndDateRange(vehicleId, startDate, endDate);

        List<PriceFluctuation> fluctuations = new ArrayList<>();
        BigDecimal avgPrice = getAverageFuelPrice(vehicleId, fromDate, toDate);

        for (FuelLog log : logs) {
            BigDecimal pricePerLiter = log.getCost().divide(log.getLiters(), 2, RoundingMode.HALF_UP);
            BigDecimal variance = pricePerLiter.subtract(avgPrice).abs();
            BigDecimal percentChange = variance.divide(avgPrice, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            if (percentChange.compareTo(BigDecimal.valueOf(10)) > 0) {
                fluctuations.add(PriceFluctuation.builder()
                        .logId(log.getId())
                        .refillDate(log.getRefillDate())
                        .pricePerLiter(pricePerLiter)
                        .averagePrice(avgPrice)
                        .percentChange(percentChange)
                        .build());
            }
        }

        return fluctuations;
    }

    @Override
    @Transactional(readOnly = true)
    public String getCheapestFuelVendor(Long companyId) {
        log.debug("Finding cheapest fuel vendor for company: {}", companyId);

        verifyCompanyAccess(companyId);

        // Placeholder - requires vendor tracking in FuelLog
        return "DEFAULT_VENDOR";
    }

    // ==================== Fuel Anomaly Detection ====================

    @Override
    @Transactional(readOnly = true)
    public List<FuelAnomaly> detectAnomalies(Long vehicleId) {
        log.debug("Detecting fuel anomalies for vehicle: {}", vehicleId);

        // Verify vehicle and company access
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        verifyCompanyAccess(vehicle.getCompany().getId());

        List<FuelLog> logs = fuelLogRepository.findByVehicleId(vehicleId, Pageable.unpaged()).getContent();
        List<FuelAnomaly> anomalies = new ArrayList<>();

        BigDecimal avgConsumption = getAverageConsumption(vehicleId);

        for (FuelLog log : logs) {
            BigDecimal variance = log.getLiters().subtract(avgConsumption).abs()
                    .divide(avgConsumption, 2, RoundingMode.HALF_UP);

            if (variance.compareTo(FUEL_ANOMALY_THRESHOLD) > 0) {
                String severity = variance.compareTo(THEFT_THRESHOLD) > 0 ? "CRITICAL" :
                        variance.compareTo(FUEL_SPIKE_THRESHOLD) > 0 ? "HIGH" : "MEDIUM";

                anomalies.add(FuelAnomaly.builder()
                        .logId(log.getId())
                        .anomalyType("CONSUMPTION_VARIANCE")
                        .expectedConsumption(avgConsumption)
                        .actualConsumption(log.getLiters())
                        .severity(severity)
                        .build());
            }
        }

        return anomalies;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuelSpike> detectConsumptionSpikes(Long vehicleId) {
        log.debug("Detecting consumption spikes for vehicle: {}", vehicleId);

        // Verify vehicle and company access
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        verifyCompanyAccess(vehicle.getCompany().getId());

        List<FuelLog> logs = fuelLogRepository.findByVehicleId(vehicleId, Pageable.unpaged()).getContent()
                .stream()
                .sorted(Comparator.comparing(FuelLog::getRefillDate))
                .collect(Collectors.toList());

        List<FuelSpike> spikes = new ArrayList<>();

        for (int i = 1; i < logs.size(); i++) {
            FuelLog current = logs.get(i);
            FuelLog previous = logs.get(i - 1);

            BigDecimal increase = current.getLiters().subtract(previous.getLiters())
                    .divide(previous.getLiters(), 2, RoundingMode.HALF_UP);

            if (increase.compareTo(FUEL_SPIKE_THRESHOLD) > 0) {
                spikes.add(FuelSpike.builder()
                        .logId(current.getId())
                        .refillDate(current.getRefillDate())
                        .previousConsumption(previous.getLiters())
                        .currentConsumption(current.getLiters())
                        .increasePercentage(increase.multiply(BigDecimal.valueOf(100)))
                        .build());
            }
        }

        return spikes;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SuspiciousFuelLog> getSuspiciousFuelLogs(Long companyId) {
        log.debug("Retrieving suspicious fuel logs for company: {}", companyId);

        verifyCompanyAccess(companyId);

        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        List<SuspiciousFuelLog> suspiciousLogs = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            List<FuelAnomaly> anomalies = detectAnomalies(vehicle.getId());
            for (FuelAnomaly anomaly : anomalies) {
                if (anomaly.getSeverity().equals("HIGH") || anomaly.getSeverity().equals("CRITICAL")) {
                    suspiciousLogs.add(SuspiciousFuelLog.builder()
                            .vehicleId(vehicle.getId())
                            .plateNumber(vehicle.getPlateNumber())
                            .logId(anomaly.getLogId())
                            .anomalyType(anomaly.getAnomalyType())
                            .severity(anomaly.getSeverity())
                            .build());
                }
            }
        }

        return suspiciousLogs;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuelTheftAlert> getFuelTheftAlerts(Long companyId) {
        log.debug("Retrieving fuel theft alerts for company: {}", companyId);

        verifyCompanyAccess(companyId);

        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        List<FuelTheftAlert> alerts = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            List<FuelAnomaly> anomalies = detectAnomalies(vehicle.getId());
            for (FuelAnomaly anomaly : anomalies) {
                if (anomaly.getSeverity().equals("CRITICAL")) {
                    alerts.add(FuelTheftAlert.builder()
                            .vehicleId(vehicle.getId())
                            .plateNumber(vehicle.getPlateNumber())
                            .logId(anomaly.getLogId())
                            .suspectedTheftLiters(anomaly.getActualConsumption()
                                    .subtract(anomaly.getExpectedConsumption()))
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
        log.debug("Retrieving fleet fuel statistics for company: {}", companyId);

        verifyCompanyAccess(companyId);

        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        BigDecimal totalConsumption = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        int vehicleCount = 0;

        for (Vehicle vehicle : vehicles) {
            Optional<BigDecimal> consumption = fuelLogRepository.calculateTotalFuelConsumed(vehicle.getId());
            Optional<BigDecimal> cost = fuelLogRepository.calculateTotalFuelCost(vehicle.getId());

            if (consumption.isPresent()) {
                totalConsumption = totalConsumption.add(consumption.get());
            }
            if (cost.isPresent()) {
                totalCost = totalCost.add(cost.get());
            }
            vehicleCount++;
        }

        BigDecimal avgCostPerLiter = totalConsumption.compareTo(BigDecimal.ZERO) > 0 ?
                totalCost.divide(totalConsumption, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return FleetFuelStatistics.builder()
                .totalConsumption(totalConsumption)
                .totalCost(totalCost)
                .averageCostPerLiter(avgCostPerLiter)
                .vehicleCount(vehicleCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryFuelStats> getFuelStatsByCategory(Long companyId, Long fromDate, Long toDate) {
        log.debug("Retrieving fuel statistics by vehicle category for company: {}", companyId);

        verifyCompanyAccess(companyId);

        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        Map<String, CategoryFuelStats.Builder> statsMap = new HashMap<>();

        for (Vehicle vehicle : vehicles) {
            String category = vehicle.getType().name();
            CategoryFuelStats.Builder builder = statsMap.getOrDefault(category,
                    CategoryFuelStats.builder().category(category).vehicleCount(0).totalConsumption(BigDecimal.ZERO)
                            .totalCost(BigDecimal.ZERO));

            Optional<BigDecimal> consumption = fuelLogRepository.calculateTotalFuelConsumed(vehicle.getId());
            Optional<BigDecimal> cost = fuelLogRepository.calculateTotalFuelCost(vehicle.getId());

            if (consumption.isPresent()) {
                builder.totalConsumption(builder.build().getTotalConsumption().add(consumption.get()));
            }
            if (cost.isPresent()) {
                builder.totalCost(builder.build().getTotalCost().add(cost.get()));
            }
            builder.vehicleCount(builder.build().getVehicleCount() + 1);

            statsMap.put(category, builder);
        }

        return statsMap.values().stream()
                .map(CategoryFuelStats.Builder::build)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleWithHighConsumption> getHighConsumptionVehicles(Long companyId, double threshold) {
        log.debug("Retrieving high consumption vehicles for company: {} with threshold: {}", companyId, threshold);

        verifyCompanyAccess(companyId);

        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);

        return vehicles.stream()
                .map(vehicle -> {
                    BigDecimal avgConsumption = getAverageConsumption(vehicle.getId());
                    return VehicleWithHighConsumption.builder()
                            .vehicleId(vehicle.getId())
                            .plateNumber(vehicle.getPlateNumber())
                            .type(vehicle.getType().name())
                            .averageConsumption(avgConsumption)
                            .build();
                })
                .filter(v -> v.getAverageConsumption().compareTo(BigDecimal.valueOf(threshold)) > 0)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FuelOptimizationTip> getFuelOptimizationTips(Long companyId) {
        log.debug("Generating fuel optimization tips for company: {}", companyId);

        verifyCompanyAccess(companyId);

        List<FuelOptimizationTip> tips = new ArrayList<>();

        // Analyze high consumption vehicles
        List<VehicleWithHighConsumption> highConsumption = getHighConsumptionVehicles(companyId, 15);
        if (!highConsumption.isEmpty()) {
            tips.add(FuelOptimizationTip.builder()
                    .category("MAINTENANCE")
                    .tip("Schedule regular maintenance for high-consumption vehicles")
                    .potentialSavings(BigDecimal.valueOf(5))
                    .priority("HIGH")
                    .build());
        }

        // Check for price fluctuations
        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        for (Vehicle vehicle : vehicles) {
            long now = System.currentTimeMillis();
            long thirtyDaysAgo = now - (30L * 24 * 60 * 60 * 1000);

            List<PriceFluctuation> fluctuations = detectPriceFluctuations(vehicle.getId(), thirtyDaysAgo, now);
            if (!fluctuations.isEmpty()) {
                tips.add(FuelOptimizationTip.builder()
                        .category("FUEL_PRICING")
                        .tip("Fuel prices are fluctuating significantly. Consider bulk purchases at lower prices")
                        .potentialSavings(BigDecimal.valueOf(3))
                        .priority("MEDIUM")
                        .build());
                break;
            }
        }

        tips.add(FuelOptimizationTip.builder()
                .category("DRIVER_TRAINING")
                .tip("Implement driver training for eco-driving techniques")
                .potentialSavings(BigDecimal.valueOf(8))
                .priority("HIGH")
                .build());

        return tips;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculatePotentialSavings(Long companyId) {
        log.debug("Calculating potential fuel savings for company: {}", companyId);

        verifyCompanyAccess(companyId);

        List<FuelOptimizationTip> tips = getFuelOptimizationTips(companyId);

        return tips.stream()
                .map(FuelOptimizationTip::getPotentialSavings)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ==================== Fuel Budget Management ====================

    @Override
    @Transactional
    public void setFuelBudget(Long vehicleId, BigDecimal monthlyBudget) {
        log.info("Setting fuel budget for vehicle: {} to {}", vehicleId, monthlyBudget);

        // Verify vehicle and company access
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        verifyCompanyAccess(vehicle.getCompany().getId());

        // Placeholder - implement budget storage based on requirements
        auditService.logAuditEvent(vehicle.getCompany().getId(), "FUEL_BUDGET_SET",
                "Vehicle", vehicleId, "Fuel budget set to: " + monthlyBudget);
    }

    @Override
    @Transactional(readOnly = true)
    public FuelBudgetStatus getFuelBudgetStatus(Long vehicleId) {
        log.debug("Retrieving fuel budget status for vehicle: {}", vehicleId);

        // Verify vehicle and company access
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        verifyCompanyAccess(vehicle.getCompany().getId());

        // Placeholder implementation
        return FuelBudgetStatus.builder()
                .vehicleId(vehicleId)
                .budget(BigDecimal.valueOf(500))
                .spent(BigDecimal.valueOf(400))
                .remaining(BigDecimal.valueOf(100))
                .percentageUsed(BigDecimal.valueOf(80))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isOverBudget(Long vehicleId) {
        log.debug("Checking if vehicle is over budget: {}", vehicleId);

        FuelBudgetStatus status = getFuelBudgetStatus(vehicleId);
        return status.getSpent().compareTo(status.getBudget()) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getVehiclesApproachingBudgetLimit(Long companyId) {
        log.debug("Retrieving vehicles approaching budget limit for company: {}", companyId);

        verifyCompanyAccess(companyId);

        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);

        return vehicles.stream()
                .filter(vehicle -> {
                    FuelBudgetStatus status = getFuelBudgetStatus(vehicle.getId());
                    return status.getPercentageUsed().compareTo(BigDecimal.valueOf(80)) >= 0;
                })
                .map(Vehicle::getId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetComparison getFleetBudgetComparison(Long companyId) {
        log.debug("Retrieving fleet budget comparison for company: {}", companyId);

        verifyCompanyAccess(companyId);

        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        BigDecimal totalBudget = BigDecimal.ZERO;
        BigDecimal totalSpent = BigDecimal.ZERO;

        for (Vehicle vehicle : vehicles) {
            FuelBudgetStatus status = getFuelBudgetStatus(vehicle.getId());
            totalBudget = totalBudget.add(status.getBudget());
            totalSpent = totalSpent.add(status.getSpent());
        }

        BigDecimal percentageUsed = totalBudget.compareTo(BigDecimal.ZERO) > 0 ?
                totalSpent.divide(totalBudget, 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

        return BudgetComparison.builder()
                .budget(totalBudget)
                .spent(totalSpent)
                .remaining(totalBudget.subtract(totalSpent))
                .percentageUsed(percentageUsed)
                .build();
    }

    // ==================== Fuel Vendor Management ====================

    @Override
    @Transactional(readOnly = true)
    public List<FuelLog> getFuelLogsByVendor(Long companyId, String vendor, Long fromDate, Long toDate) {
        log.debug("Retrieving fuel logs for vendor: {} for company: {}", vendor, companyId);

        verifyCompanyAccess(companyId);

        // Placeholder - requires vendor field in FuelLog
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public VendorFuelStatistics getVendorStatistics(Long companyId, String vendor) {
        log.debug("Retrieving statistics for vendor: {} for company: {}", vendor, companyId);

        verifyCompanyAccess(companyId);

        // Placeholder implementation
        return VendorFuelStatistics.builder()
                .vendor(vendor)
                .totalLiters(BigDecimal.valueOf(1000))
                .totalCost(BigDecimal.valueOf(1200))
                .averagePricePerLiter(BigDecimal.valueOf(1.20))
                .transactionCount(10)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorComparison> compareVendors(Long companyId, Long fromDate, Long toDate) {
        log.debug("Comparing fuel vendors for company: {}", companyId);

        verifyCompanyAccess(companyId);

        // Placeholder - requires vendor tracking
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public Double getVendorRating(String vendor) {
        log.debug("Retrieving rating for vendor: {}", vendor);

        // Placeholder - requires vendor rating system
        return 4.5;
    }

    // ==================== Reporting & Export ====================

    @Override
    @Transactional(readOnly = true)
    public String generateFuelReport(Long companyId, Long fromDate, Long toDate) {
        log.debug("Generating fuel consumption report for company: {}", companyId);

        verifyCompanyAccess(companyId);

        FleetFuelStatistics stats = getFleetFuelStatistics(companyId, fromDate, toDate);

        StringBuilder report = new StringBuilder();
        report.append("FUEL CONSUMPTION REPORT\n");
        report.append("=====================\n");
        report.append("Total Consumption: ").append(stats.getTotalConsumption()).append(" liters\n");
        report.append("Total Cost: ").append(stats.getTotalCost()).append("\n");
        report.append("Average Cost per Liter: ").append(stats.getAverageCostPerLiter()).append("\n");
        report.append("Vehicle Count: ").append(stats.getVehicleCount()).append("\n");

        return report.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportFuelLogsToCSV(Long vehicleId, Long fromDate, Long toDate) {
        log.debug("Exporting fuel logs to CSV for vehicle: {}", vehicleId);

        // Verify vehicle and company access
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + vehicleId));

        verifyCompanyAccess(vehicle.getCompany().getId());

        LocalDate startDate = Instant.ofEpochMilli(fromDate).atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = Instant.ofEpochMilli(toDate).atZone(ZoneId.systemDefault()).toLocalDate();

        List<FuelLog> logs = fuelLogRepository.findByVehicleIdAndDateRange(vehicleId, startDate, endDate);

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Refill Date,Liters,Cost,Recorded By\n");

        for (FuelLog log : logs) {
            csv.append(log.getId()).append(",")
                    .append(log.getRefillDate()).append(",")
                    .append(log.getLiters()).append(",")
                    .append(log.getCost()).append(",")
                    .append(log.getRecordedBy()).append("\n");
        }

        return csv.toString().getBytes();
    }

    @Override
    @Transactional(readOnly = true)
    public String generateCostAnalysisReport(Long companyId, Long fromDate, Long toDate) {
        log.debug("Generating fuel cost analysis report for company: {}", companyId);

        verifyCompanyAccess(companyId);

        FleetFuelStatistics stats = getFleetFuelStatistics(companyId, fromDate, toDate);
        BigDecimal potentialSavings = calculatePotentialSavings(companyId);

        StringBuilder report = new StringBuilder();
        report.append("FUEL COST ANALYSIS REPORT\n");
        report.append("=========================\n");
        report.append("Total Cost: ").append(stats.getTotalCost()).append("\n");
        report.append("Average Cost per Liter: ").append(stats.getAverageCostPerLiter()).append("\n");
        report.append("Potential Savings: ").append(potentialSavings).append("%\n");

        return report.toString();
    }

    // ==================== Helper Methods ====================

    private void verifyCompanyAccess(Long companyId) {
        SecurityUtils.validateCompanyAccess(companyId);
    }
}
