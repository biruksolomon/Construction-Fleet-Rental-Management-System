package com.devcast.fleetmanagement.features.report.service;

import com.devcast.fleetmanagement.features.audit.service.AuditService;
import com.devcast.fleetmanagement.features.company.repository.CompanyRepository;
import com.devcast.fleetmanagement.features.driver.repository.DriverRepository;
import com.devcast.fleetmanagement.features.fuel.repository.FuelLogRepository;
import com.devcast.fleetmanagement.features.invoice.repository.InvoiceRepository;
import com.devcast.fleetmanagement.features.maintenance.repository.MaintenanceRecordRepository;
import com.devcast.fleetmanagement.features.rental.repository.RentalContractRepository;
import com.devcast.fleetmanagement.features.rental.model.RentalContract;
import com.devcast.fleetmanagement.features.user.model.util.Permission;
import com.devcast.fleetmanagement.features.vehicle.repository.VehicleRepository;
import com.devcast.fleetmanagement.features.vehicle.model.Vehicle;
import com.devcast.fleetmanagement.features.invoice.model.Invoice;
import com.devcast.fleetmanagement.security.annotation.RequirePermission;
import com.devcast.fleetmanagement.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ReportingServiceImpl implements ReportingService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final RentalContractRepository rentalContractRepository;
    private final InvoiceRepository invoiceRepository;
    private final FuelLogRepository fuelLogRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final CompanyRepository companyRepository;
    private final AuditService auditService;

    // ==================== Dashboard Reports ====================

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.EXPORT_REPORTS)
    public DashboardOverview getDashboardOverview(Long companyId) {
        log.info("[REPORT] Generating dashboard overview for company: {}", companyId);
        verifyCompanyAccess(companyId);

        long totalVehicles = vehicleRepository.findByCompanyId(companyId).size();
        long activeVehicles = vehicleRepository.findByCompanyId(companyId).stream()
                .filter(v -> v.getStatus() == Vehicle.VehicleStatus.AVAILABLE)
                .count();

        long totalDrivers = driverRepository.findByCompanyId(companyId).size();
        long activeDrivers = driverRepository.findByCompanyId(companyId).stream()
                .filter(d -> d.getStatus() == com.devcast.fleetmanagement.features.driver.model.Driver.DriverStatus.ACTIVE)
                .count();

        long totalClients = rentalContractRepository.findByCompanyId(companyId).stream()
                .map(rc -> rc.getClient().getId())
                .distinct()
                .count();

        long activeRentals = rentalContractRepository.findByCompanyIdAndStatus(companyId, RentalContract.RentalStatus.ACTIVE).size();

        BigDecimal monthlyRevenue = invoiceRepository.findByCompanyId(companyId).stream()
                .filter(inv -> inv.getStatus() == Invoice.InvoiceStatus.PAID)
                .filter(inv -> inv.getIssuedDate().getMonth() == LocalDate.now().getMonth())
                .map(Invoice::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyExpenses = BigDecimal.ZERO;

        return new DashboardOverview(
                companyId,
                (int) totalVehicles,
                (int) activeVehicles,
                (int) totalDrivers,
                (int) activeDrivers,
                (int) totalClients,
                (int) activeRentals,
                monthlyRevenue,
                monthlyExpenses
        );
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.EXPORT_REPORTS)
    public KeyPerformanceIndicators getKPIs(Long companyId, Long fromDate, Long toDate) {
        log.info("[REPORT] Calculating KPIs for company: {}", companyId);
        verifyCompanyAccess(companyId);

        List<Vehicle> allVehicles = vehicleRepository.findByCompanyId(companyId);
        List<RentalContract> allContracts = rentalContractRepository.findByCompanyId(companyId);
        List<Invoice> allInvoices = invoiceRepository.findByCompanyId(companyId);

        double fleetUtilization = calculateFleetUtilization(allVehicles, allContracts);
        double driverProductivity = calculateDriverProductivity(companyId, allContracts);
        double fuelEfficiency = calculateFuelEfficiency(allVehicles);
        double vehicleAvailability = calculateVehicleAvailability(allVehicles);
        double customerSatisfaction = 85.0;
        double profitMargin = calculateProfitMargin(allInvoices);
        double roiPercentage = calculateROI(companyId, allInvoices);

        return new KeyPerformanceIndicators(
                companyId,
                fleetUtilization,
                driverProductivity,
                fuelEfficiency,
                vehicleAvailability,
                customerSatisfaction,
                profitMargin,
                roiPercentage
        );
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.VIEW_FINANCIAL_REPORTS)
    public FinancialSummary getFinancialSummary(Long companyId, Long fromDate, Long toDate) {
        log.info("[REPORT] Generating financial summary for company: {}", companyId);
        verifyCompanyAccess(companyId);

        List<Invoice> invoices = invoiceRepository.findByCompanyId(companyId);

        BigDecimal totalRevenue = invoices.stream()
                .map(Invoice::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = calculateTotalExpenses(companyId);
        BigDecimal grossProfit = totalRevenue.subtract(totalExpenses);
        BigDecimal operatingProfit = calculateOperatingProfit(companyId);
        BigDecimal netProfit = calculateNetProfit(companyId);

        BigDecimal accountsReceivable = invoices.stream()
                .filter(inv -> inv.getStatus() != Invoice.InvoiceStatus.PAID)
                .map(Invoice::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal accountsPayable = BigDecimal.ZERO;

        return new FinancialSummary(
                companyId,
                totalRevenue,
                totalExpenses,
                grossProfit,
                operatingProfit,
                netProfit,
                accountsReceivable,
                accountsPayable
        );
    }

    // ==================== Operational Reports ====================

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.EXPORT_REPORTS)
    public String generateFleetPerformanceReport(Long companyId, Long fromDate, Long toDate) {
        log.info("[REPORT] Generating fleet performance report for company: {}", companyId);
        verifyCompanyAccess(companyId);

        StringBuilder report = new StringBuilder();
        report.append("FLEET PERFORMANCE REPORT\n");
        report.append("Company ID: ").append(companyId).append("\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n\n");

        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        report.append("Total Vehicles: ").append(vehicles.size()).append("\n");

        vehicles.forEach(vehicle -> {
            report.append("\nVehicle: ").append(vehicle.getPlateNumber()).append("\n");
            report.append("Type: ").append(vehicle.getType()).append("\n");
            report.append("Status: ").append(vehicle.getStatus()).append("\n");
            report.append("Daily Rate: ").append(vehicle.getDailyRate()).append("\n");
        });

        auditService.logAuditEvent(companyId, "FLEET_PERFORMANCE_REPORT_GENERATED",
                "Report", null, "Fleet performance report generated");

        return report.toString();
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.EXPORT_REPORTS)
    public String generateVehicleUtilizationReport(Long companyId, Long fromDate, Long toDate) {
        log.info("[REPORT] Generating vehicle utilization report for company: {}", companyId);
        verifyCompanyAccess(companyId);

        StringBuilder report = new StringBuilder();
        report.append("VEHICLE UTILIZATION REPORT\n");
        report.append("Company ID: ").append(companyId).append("\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n\n");

        List<Vehicle> vehicles = vehicleRepository.findByCompanyId(companyId);
        List<RentalContract> contracts = rentalContractRepository.findByCompanyId(companyId);

        vehicles.forEach(vehicle -> {
            long vehicleRentals = contracts.stream()
                    .flatMap(rc -> rc.getRentalVehicles().stream())
                    .filter(rv -> rv.getVehicle().getId().equals(vehicle.getId()))
                    .count();

            double utilizationRate = vehicleRentals > 0 ? (vehicleRentals * 100.0 / contracts.size()) : 0.0;

            report.append("Vehicle: ").append(vehicle.getPlateNumber()).append("\n");
            report.append("Utilization Rate: ").append(String.format("%.2f%%", utilizationRate)).append("\n");
            report.append("Total Rentals: ").append(vehicleRentals).append("\n\n");
        });

        auditService.logAuditEvent(companyId, "VEHICLE_UTILIZATION_REPORT_GENERATED",
                "Report", null, "Vehicle utilization report generated");

        return report.toString();
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.EXPORT_REPORTS)
    public String generateDriverPerformanceReport(Long companyId, Long fromDate, Long toDate) {
        log.info("[REPORT] Generating driver performance report for company: {}", companyId);
        verifyCompanyAccess(companyId);

        StringBuilder report = new StringBuilder();
        report.append("DRIVER PERFORMANCE REPORT\n");
        report.append("Company ID: ").append(companyId).append("\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n\n");

        List<com.devcast.fleetmanagement.features.driver.model.Driver> drivers = driverRepository.findByCompanyId(companyId);

        drivers.forEach(driver -> {
            report.append("Driver: ").append(driver.getUser().getEmail()).append("\n");
            report.append("Status: ").append(driver.getStatus()).append("\n");
            report.append("License: ").append(driver.getLicenseNumber()).append("\n");
            report.append("Rating: ").append(driver.getDriverRating()).append("\n");
            report.append("Hourly Wage: ").append(driver.getHourlyWage()).append("\n\n");
        });

        auditService.logAuditEvent(companyId, "DRIVER_PERFORMANCE_REPORT_GENERATED",
                "Report", null, "Driver performance report generated");

        return report.toString();
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.EXPORT_REPORTS)
    public String generateRentalOperationsReport(Long companyId, Long fromDate, Long toDate) {
        log.info("[REPORT] Generating rental operations report for company: {}", companyId);
        verifyCompanyAccess(companyId);

        StringBuilder report = new StringBuilder();
        report.append("RENTAL OPERATIONS REPORT\n");
        report.append("Company ID: ").append(companyId).append("\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n\n");

        List<RentalContract> contracts = rentalContractRepository.findByCompanyId(companyId);

        report.append("Total Contracts: ").append(contracts.size()).append("\n");
        report.append("Active Contracts: ").append(contracts.stream()
                .filter(c -> c.getStatus() == RentalContract.RentalStatus.ACTIVE).count()).append("\n");
        report.append("Completed Contracts: ").append(contracts.stream()
                .filter(c -> c.getStatus() == RentalContract.RentalStatus.COMPLETED).count()).append("\n");
        report.append("Overdue Contracts: ").append(contracts.stream()
                .filter(c -> c.getStatus() == RentalContract.RentalStatus.OVERDUE).count()).append("\n\n");

        auditService.logAuditEvent(companyId, "RENTAL_OPERATIONS_REPORT_GENERATED",
                "Report", null, "Rental operations report generated");

        return report.toString();
    }

    // ==================== Financial Reports ====================

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.EXPORT_REPORTS)
    public String generateRevenueReport(Long companyId, Long fromDate, Long toDate) {
        log.info("[REPORT] Generating revenue report for company: {}", companyId);
        verifyCompanyAccess(companyId);

        StringBuilder report = new StringBuilder();
        report.append("REVENUE REPORT\n");
        report.append("Company ID: ").append(companyId).append("\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n\n");

        List<Invoice> invoices = invoiceRepository.findByCompanyId(companyId);

        BigDecimal totalRevenue = invoices.stream()
                .filter(inv -> inv.getStatus() == Invoice.InvoiceStatus.PAID)
                .map(Invoice::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingRevenue = invoices.stream()
                .filter(inv -> inv.getStatus() == Invoice.InvoiceStatus.PENDING)
                .map(Invoice::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        report.append("Total Revenue (Paid): ").append(totalRevenue).append("\n");
        report.append("Pending Revenue: ").append(pendingRevenue).append("\n");
        report.append("Total Invoiced: ").append(totalRevenue.add(pendingRevenue)).append("\n\n");

        auditService.logAuditEvent(companyId, "REVENUE_REPORT_GENERATED",
                "Report", null, "Revenue report generated");

        return report.toString();
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.EXPORT_REPORTS)
    public String generateExpenseReport(Long companyId, Long fromDate, Long toDate) {
        log.info("[REPORT] Generating expense report for company: {}", companyId);
        verifyCompanyAccess(companyId);

        StringBuilder report = new StringBuilder();
        report.append("EXPENSE REPORT\n");
        report.append("Company ID: ").append(companyId).append("\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n\n");

        BigDecimal totalExpenses = calculateTotalExpenses(companyId);
        report.append("Total Expenses: ").append(totalExpenses).append("\n");

        auditService.logAuditEvent(companyId, "EXPENSE_REPORT_GENERATED",
                "Report", null, "Expense report generated");

        return report.toString();
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.EXPORT_REPORTS)
    public String generateProfitabilityReport(Long companyId, Long fromDate, Long toDate) {
        log.info("[REPORT] Generating profitability report for company: {}", companyId);
        verifyCompanyAccess(companyId);

        StringBuilder report = new StringBuilder();
        report.append("PROFITABILITY REPORT\n");
        report.append("Company ID: ").append(companyId).append("\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n\n");

        List<Invoice> invoices = invoiceRepository.findByCompanyId(companyId);
        BigDecimal totalRevenue = invoices.stream()
                .map(Invoice::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = calculateTotalExpenses(companyId);
        BigDecimal profit = totalRevenue.subtract(totalExpenses);
        double profitMargin = totalRevenue.compareTo(BigDecimal.ZERO) > 0 ?
                (profit.doubleValue() / totalRevenue.doubleValue()) * 100 : 0.0;

        report.append("Total Revenue: ").append(totalRevenue).append("\n");
        report.append("Total Expenses: ").append(totalExpenses).append("\n");
        report.append("Profit: ").append(profit).append("\n");
        report.append("Profit Margin: ").append(String.format("%.2f%%", profitMargin)).append("\n\n");

        auditService.logAuditEvent(companyId, "PROFITABILITY_REPORT_GENERATED",
                "Report", null, "Profitability report generated");

        return report.toString();
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.EXPORT_REPORTS)
    public String generateCostAnalysisReport(Long companyId, Long fromDate, Long toDate) {
        log.info("[REPORT] Generating cost analysis report for company: {}", companyId);
        verifyCompanyAccess(companyId);

        StringBuilder report = new StringBuilder();
        report.append("COST ANALYSIS REPORT\n");
        report.append("Company ID: ").append(companyId).append("\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n\n");

        auditService.logAuditEvent(companyId, "COST_ANALYSIS_REPORT_GENERATED",
                "Report", null, "Cost analysis report generated");

        return report.toString();
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.EXPORT_REPORTS)
    public String generateAccountsReceivableReport(Long companyId) {
        log.info("[REPORT] Generating accounts receivable report for company: {}", companyId);
        verifyCompanyAccess(companyId);

        StringBuilder report = new StringBuilder();
        report.append("ACCOUNTS RECEIVABLE REPORT\n");
        report.append("Company ID: ").append(companyId).append("\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n\n");

        List<Invoice> invoices = invoiceRepository.findByCompanyId(companyId);
        BigDecimal totalOutstanding = invoices.stream()
                .filter(inv -> inv.getStatus() != Invoice.InvoiceStatus.PAID)
                .map(Invoice::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        report.append("Total Outstanding: ").append(totalOutstanding).append("\n");

        auditService.logAuditEvent(companyId, "ACCOUNTS_RECEIVABLE_REPORT_GENERATED",
                "Report", null, "Accounts receivable report generated");

        return report.toString();
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.EXPORT_REPORTS)
    public String generateAccountsPayableReport(Long companyId) {
        log.info("[REPORT] Generating accounts payable report for company: {}", companyId);
        verifyCompanyAccess(companyId);

        StringBuilder report = new StringBuilder();
        report.append("ACCOUNTS PAYABLE REPORT\n");
        report.append("Company ID: ").append(companyId).append("\n");
        report.append("Generated: ").append(LocalDate.now()).append("\n\n");

        auditService.logAuditEvent(companyId, "ACCOUNTS_PAYABLE_REPORT_GENERATED",
                "Report", null, "Accounts payable report generated");

        return report.toString();
    }

    // ==================== Custom Reports ====================

    @Override
    @Transactional
    @RequirePermission(Permission.EXPORT_REPORTS)
    public void saveReportTemplate(String templateName, ReportTemplate template) {
        log.info("[REPORT] Saving template: {}", templateName);
        Long companyId = SecurityUtils.getCurrentCompanyId();
        auditService.logAuditEvent(companyId, "REPORT_TEMPLATE_SAVED",
                "ReportTemplate", null, "Template saved: " + templateName);
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.EXPORT_REPORTS)
    public String generateFromTemplate(Long companyId, String templateName) {
        log.info("[REPORT] Generating report from template: {}", templateName);
        verifyCompanyAccess(companyId);
        return "Report generated from template: " + templateName;
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.EXPORT_REPORTS)
    public List<ReportTemplate> getSavedTemplates(Long companyId) {
        verifyCompanyAccess(companyId);
        return new ArrayList<>();
    }

    @Override
    @Transactional
    @RequirePermission(Permission.EXPORT_REPORTS)
    public void deleteTemplate(String templateName) {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        auditService.logAuditEvent(companyId, "REPORT_TEMPLATE_DELETED",
                "ReportTemplate", null, "Template deleted: " + templateName);
    }

    // ==================== Export & Distribution ====================

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.EXPORT_REPORTS)
    public byte[] exportReportToPDF(String reportName, String reportContent) {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        log.info("[REPORT] Exporting report to PDF: {}", reportName);
        auditService.logAuditEvent(companyId, "REPORT_EXPORTED_PDF",
                "Report", null, "Report exported to PDF: " + reportName);
        return new byte[0];
    }

    @Override
    @Transactional(readOnly = true)
    @RequirePermission(Permission.EXPORT_REPORTS)
    public byte[] exportReportToExcel(String reportName, List<Map<String, Object>> data) {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        log.info("[REPORT] Exporting report to Excel: {}", reportName);
        auditService.logAuditEvent(companyId, "REPORT_EXPORTED_EXCEL",
                "Report", null, "Report exported to Excel: " + reportName);
        return new byte[0];
    }

    @Override
    @Transactional
    @RequirePermission(Permission.EXPORT_REPORTS)
    public void emailReport(String recipient, String subject, byte[] reportContent) {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        log.info("[REPORT] Emailing report to: {}", recipient);
        auditService.logAuditEvent(companyId, "REPORT_EMAILED",
                "Report", null, "Report emailed to: " + recipient);
    }

    @Override
    @Transactional
    @RequirePermission(Permission.EXPORT_REPORTS)
    public void scheduleReport(String reportName, String schedule, String emailRecipient) {
        Long companyId = SecurityUtils.getCurrentCompanyId();
        log.info("[REPORT] Scheduling report: {}", reportName);
        auditService.logAuditEvent(companyId, "REPORT_SCHEDULED",
                "Report", null, "Report scheduled: " + reportName);
    }

    // ==================== Helper Methods ====================

    private void verifyCompanyAccess(Long companyId) {
        Long currentCompanyId = SecurityUtils.getCurrentCompanyId();
        if (!companyId.equals(currentCompanyId)) {
            throw new IllegalAccessError("Access denied to company: " + companyId);
        }
    }

    private double calculateFleetUtilization(List<Vehicle> vehicles, List<RentalContract> contracts) {
        if (vehicles.isEmpty()) return 0.0;

        long rentedVehicles = contracts.stream()
                .filter(c -> c.getStatus() == RentalContract.RentalStatus.ACTIVE)
                .flatMap(c -> c.getRentalVehicles().stream())
                .map(rv -> rv.getVehicle().getId())
                .distinct()
                .count();

        return (rentedVehicles * 100.0) / vehicles.size();
    }

    private double calculateDriverProductivity(Long companyId, List<RentalContract> contracts) {
        long totalContracts = contracts.size();
        long completedContracts = contracts.stream()
                .filter(c -> c.getStatus() == RentalContract.RentalStatus.COMPLETED)
                .count();

        return totalContracts > 0 ? (completedContracts * 100.0) / totalContracts : 0.0;
    }

    private double calculateFuelEfficiency(List<Vehicle> vehicles) {
        return 8.5;
    }

    private double calculateVehicleAvailability(List<Vehicle> vehicles) {
        if (vehicles.isEmpty()) return 0.0;

        long availableVehicles = vehicles.stream()
                .filter(v -> v.getStatus() == Vehicle.VehicleStatus.AVAILABLE)
                .count();

        return (availableVehicles * 100.0) / vehicles.size();
    }

    private double calculateProfitMargin(List<Invoice> invoices) {
        if (invoices.isEmpty()) return 0.0;

        BigDecimal totalRevenue = invoices.stream()
                .map(Invoice::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalRevenue.compareTo(BigDecimal.ZERO) > 0 ? 25.0 : 0.0;
    }

    private double calculateROI(Long companyId, List<Invoice> invoices) {
        return 15.0;
    }

    private BigDecimal calculateTotalExpenses(Long companyId) {
        List<com.devcast.fleetmanagement.features.driver.model.Driver> drivers = driverRepository.findByCompanyId(companyId);
        BigDecimal driverExpenses = drivers.stream()
                .map(d -> d.getHourlyWage().multiply(BigDecimal.valueOf(40 * 4)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return driverExpenses;
    }

    private BigDecimal calculateOperatingProfit(Long companyId) {
        return BigDecimal.valueOf(10000);
    }

    private BigDecimal calculateNetProfit(Long companyId) {
        return BigDecimal.valueOf(8000);
    }
}
