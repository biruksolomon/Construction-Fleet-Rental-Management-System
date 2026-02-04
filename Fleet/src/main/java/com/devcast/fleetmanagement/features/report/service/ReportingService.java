package com.devcast.fleetmanagement.features.report.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Reporting Service Interface
 * Handles generation of comprehensive business reports and analytics
 */
public interface ReportingService {

    // ==================== Dashboard Reports ====================

    /**
     * Get dashboard overview
     */
    DashboardOverview getDashboardOverview(Long companyId);

    /**
     * Get key performance indicators
     */
    KeyPerformanceIndicators getKPIs(Long companyId, Long fromDate, Long toDate);

    /**
     * Get financial summary
     */
    FinancialSummary getFinancialSummary(Long companyId, Long fromDate, Long toDate);

    // ==================== Operational Reports ====================

    /**
     * Get fleet performance report
     */
    String generateFleetPerformanceReport(Long companyId, Long fromDate, Long toDate);

    /**
     * Get vehicle utilization report
     */
    String generateVehicleUtilizationReport(Long companyId, Long fromDate, Long toDate);

    /**
     * Get driver performance report
     */
    String generateDriverPerformanceReport(Long companyId, Long fromDate, Long toDate);

    /**
     * Get rental operations report
     */
    String generateRentalOperationsReport(Long companyId, Long fromDate, Long toDate);

    // ==================== Financial Reports ====================

    /**
     * Get revenue report
     */
    String generateRevenueReport(Long companyId, Long fromDate, Long toDate);

    /**
     * Get expense report
     */
    String generateExpenseReport(Long companyId, Long fromDate, Long toDate);

    /**
     * Get profitability report
     */
    String generateProfitabilityReport(Long companyId, Long fromDate, Long toDate);

    /**
     * Get cost analysis report
     */
    String generateCostAnalysisReport(Long companyId, Long fromDate, Long toDate);

    /**
     * Get accounts receivable report
     */
    String generateAccountsReceivableReport(Long companyId);

    /**
     * Get accounts payable report
     */
    String generateAccountsPayableReport(Long companyId);

    // ==================== Custom Reports ====================

    /**
     * Save custom report template
     */
    void saveReportTemplate(String templateName, ReportTemplate template);

    /**
     * Generate from template
     */
    String generateFromTemplate(Long companyId, String templateName);

    /**
     * Get saved templates
     */
    List<ReportTemplate> getSavedTemplates(Long companyId);

    /**
     * Delete template
     */
    void deleteTemplate(String templateName);

    // ==================== Export & Distribution ====================

    /**
     * Export report to PDF
     */
    byte[] exportReportToPDF(String reportName, String reportContent);

    /**
     * Export report to Excel
     */
    byte[] exportReportToExcel(String reportName, List<Map<String, Object>> data);

    /**
     * Email report
     */
    void emailReport(String recipient, String subject, byte[] reportContent);

    /**
     * Schedule report generation
     */
    void scheduleReport(String reportName, String schedule, String emailRecipient);

    // Data Transfer Objects

    record DashboardOverview(
            Long companyId,
            int totalVehicles,
            int activeVehicles,
            int totalDrivers,
            int activeDrivers,
            int totalClients,
            int activeRentals,
            BigDecimal monthlyRevenue,
            BigDecimal monthlyExpenses
    ) {}

    record KeyPerformanceIndicators(
            Long companyId,
            Double fleetUtilization,
            Double driverProductivity,
            Double fuelEfficiency,
            Double vehicleAvailability,
            Double customerSatisfaction,
            Double profitMargin,
            Double roiPercentage
    ) {}

    record FinancialSummary(
            Long companyId,
            BigDecimal totalRevenue,
            BigDecimal totalExpenses,
            BigDecimal grossProfit,
            BigDecimal operatingProfit,
            BigDecimal netProfit,
            BigDecimal accountsReceivable,
            BigDecimal accountsPayable
    ) {}

    record ReportTemplate(
            String templateId,
            String templateName,
            String reportType,
            String description,
            Map<String, String> parameters
    ) {}
}
