package com.DevCast.Fleet_Management.service.interfaces;

import com.DevCast.Fleet_Management.model.RentalContract;
import com.DevCast.Fleet_Management.model.RentalVehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Rental Contract Service Interface
 * Handles rental agreement management, pricing, and fulfillment
 */
public interface RentalContractService {

    // ==================== Contract CRUD Operations ====================

    /**
     * Create new rental contract
     */
    RentalContract createContract(Long companyId, RentalContract contract);

    /**
     * Get contract by ID
     */
    Optional<RentalContract> getContractById(Long contractId);

    /**
     * Update contract
     */
    RentalContract updateContract(Long contractId, RentalContract contract);

    /**
     * Cancel contract
     */
    void cancelContract(Long contractId, String reason);

    /**
     * Get all contracts in company
     */
    Page<RentalContract> getContractsByCompany(Long companyId, Pageable pageable);

    /**
     * Get active contracts
     */
    List<RentalContract> getActiveContracts(Long companyId);

    /**
     * Get contracts by status
     */
    Page<RentalContract> getContractsByStatus(Long companyId, String status, Pageable pageable);

    // ==================== Contract Status Management ====================

    /**
     * Confirm contract
     */
    void confirmContract(Long contractId);

    /**
     * Start rental (mark as in-progress)
     */
    void startRental(Long contractId);

    /**
     * Complete rental
     */
    void completeRental(Long contractId);

    /**
     * Get contract status
     */
    Optional<String> getContractStatus(Long contractId);

    /**
     * Check if contract is active
     */
    boolean isContractActive(Long contractId);

    /**
     * Get contracts expiring soon
     */
    List<RentalContract> getContractsExpiringSoon(Long companyId, int daysFromNow);

    // ==================== Rental Vehicle Management ====================

    /**
     * Add vehicle to rental contract
     */
    RentalVehicle addVehicleToRental(Long contractId, Long vehicleId);

    /**
     * Get vehicles in contract
     */
    List<RentalVehicle> getContractVehicles(Long contractId);

    /**
     * Remove vehicle from contract
     */
    void removeVehicleFromRental(Long rentalVehicleId);

    /**
     * Update vehicle rental terms
     */
    RentalVehicle updateVehicleTerms(Long rentalVehicleId, RentalVehicle vehicle);

    /**
     * Get rental vehicle details
     */
    Optional<RentalVehicle> getRentalVehicle(Long rentalVehicleId);

    // ==================== Pricing & Cost Calculation ====================

    /**
     * Calculate rental cost
     */
    RentalCostBreakdown calculateRentalCost(Long contractId);

    /**
     * Get base rental rate
     */
    BigDecimal getBaseRentalRate(Long vehicleId, Long rentalDays);

    /**
     * Apply discount to contract
     */
    void applyDiscount(Long contractId, BigDecimal discountAmount, String reason);

    /**
     * Add additional charges
     */
    void addAdditionalCharge(Long contractId, BigDecimal amount, String description);

    /**
     * Get total rental cost
     */
    BigDecimal getTotalRentalCost(Long contractId);

    /**
     * Get cost per day
     */
    BigDecimal getCostPerDay(Long contractId);

    /**
     * Calculate late fees
     */
    BigDecimal calculateLateFees(Long contractId);

    /**
     * Get damage charges estimate
     */
    BigDecimal estimateDamageCharges(Long contractId, String damageDescription);

    // ==================== Invoice & Payment ====================

    /**
     * Generate invoice for contract
     */
    Long generateInvoice(Long contractId);

    /**
     * Get contract invoice
     */
    Optional<Long> getContractInvoice(Long contractId);

    /**
     * Process payment
     */
    void processPayment(Long contractId, BigDecimal amount, String paymentMethod);

    /**
     * Get payment history
     */
    List<PaymentRecord> getPaymentHistory(Long contractId);

    /**
     * Calculate outstanding balance
     */
    BigDecimal getOutstandingBalance(Long contractId);

    /**
     * Send payment reminder
     */
    void sendPaymentReminder(Long contractId);

    // ==================== Client & Contact Information ====================

    /**
     * Get contract client
     */
    Optional<Long> getContractClient(Long contractId);

    /**
     * Update contract client
     */
    void updateContractClient(Long contractId, Long clientId);

    /**
     * Add contact person
     */
    void addContactPerson(Long contractId, String name, String phone, String email);

    /**
     * Get contract contacts
     */
    List<ContactInfo> getContractContacts(Long contractId);

    // ==================== Usage & Duration ====================

    /**
     * Get rental duration
     */
    Long getRentalDuration(Long contractId);

    /**
     * Get remaining rental days
     */
    Long getRemainingDays(Long contractId);

    /**
     * Get actual usage vs planned
     */
    UsageComparison getUsageComparison(Long contractId);

    /**
     * Extend rental period
     */
    void extendRental(Long contractId, int additionalDays);

    /**
     * Reduce rental period
     */
    void reduceRental(Long contractId, int days);

    /**
     * Check early return
     */
    boolean isEarlyReturn(Long contractId);

    /**
     * Calculate early return refund
     */
    BigDecimal calculateEarlyReturnRefund(Long contractId);

    // ==================== Vehicle Condition ====================

    /**
     * Record vehicle condition at pickup
     */
    void recordPickupCondition(Long contractId, VehicleCondition condition);

    /**
     * Record vehicle condition at return
     */
    void recordReturnCondition(Long contractId, VehicleCondition condition);

    /**
     * Get vehicle condition history
     */
    List<VehicleCondition> getConditionHistory(Long contractId);

    /**
     * Generate damage report
     */
    String generateDamageReport(Long contractId);

    // ==================== Search & Filter ====================

    /**
     * Search contracts
     */
    Page<RentalContract> searchContracts(Long companyId, String searchTerm, Pageable pageable);

    /**
     * Filter contracts by criteria
     */
    Page<RentalContract> filterContracts(Long companyId, ContractFilterCriteria criteria, Pageable pageable);

    /**
     * Get overdue contracts
     */
    List<RentalContract> getOverdueContracts(Long companyId);

    /**
     * Get high-value contracts
     */
    List<RentalContract> getHighValueContracts(Long companyId, BigDecimal minValue);

    // ==================== Reporting ====================

    /**
     * Get contract summary report
     */
    ContractSummaryReport getSummaryReport(Long companyId, Long fromDate, Long toDate);

    /**
     * Get rental utilization report
     */
    RentalUtilizationReport getUtilizationReport(Long companyId, Long fromDate, Long toDate);

    /**
     * Export contracts to CSV
     */
    byte[] exportContractsToCSV(Long companyId);

    // Data Transfer Objects

    record RentalCostBreakdown(
            BigDecimal baseRate,
            BigDecimal discounts,
            BigDecimal additionalCharges,
            BigDecimal lateFees,
            BigDecimal damageCharges,
            BigDecimal totalCost,
            BigDecimal paidAmount,
            BigDecimal balanceDue
    ) {}

    record PaymentRecord(
            Long paymentId,
            Long contractId,
            BigDecimal amount,
            String paymentMethod,
            Long paymentDate,
            String status
    ) {}

    record ContactInfo(
            String name,
            String phone,
            String email,
            String role,
            String isPrimaryContact
    ) {}

    record UsageComparison(
            Long plannedDays,
            Long actualDays,
            Double variance,
            String status
    ) {}

    record VehicleCondition(
            Long contractId,
            String conditionType,
            String description,
            List<String> damages,
            List<String> photos,
            Long recordDate,
            String recordedBy
    ) {}

    record ContractFilterCriteria(
            String status,
            Long clientId,
            Long fromDate,
            Long toDate,
            BigDecimal minAmount,
            BigDecimal maxAmount
    ) {}

    record ContractSummaryReport(
            Long companyId,
            int totalContracts,
            int activeContracts,
            int completedContracts,
            BigDecimal totalRevenue,
            BigDecimal averageContractValue,
            Double occupancyRate
    ) {}

    record RentalUtilizationReport(
            Long companyId,
            int vehicleCount,
            Double avgUtilization,
            int totalRentals,
            BigDecimal totalIncome,
            List<VehicleUtilization> vehicleDetails
    ) {
        public record VehicleUtilization(
                Long vehicleId,
                String registrationNumber,
                Double utilizationPercent,
                int rentalCount,
                BigDecimal income
        ) {}
    }
}
