package com.devcast.fleetmanagement.features.rental.controller;

import com.devcast.fleetmanagement.features.rental.dto.*;
import com.devcast.fleetmanagement.features.rental.model.RentalContract;
import com.devcast.fleetmanagement.features.rental.service.RentalContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Rental Contract Controller
 *
 * Provides REST endpoints for rental contract management, including CRUD operations,
 * pricing calculations, invoicing, and analytics.
 *
 * Base path: /api/v1/rentals
 * All endpoints require authentication and proper RBAC permissions.
 */
@RestController
@RequestMapping("/api/v1/rentals")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Rentals", description = "Rental contract management APIs")
public class RentalContractController {

    private final RentalContractService rentalContractService;

    // ==================== CRUD Operations ====================

    @PostMapping
    @Operation(summary = "Create new rental contract", description = "Create a new rental contract with vehicles")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Contract created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "409", description = "Conflict in contract data")
    })
    public ResponseEntity<RentalContractResponse> createContract(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @Valid @RequestBody RentalContractCreateRequest request) {
        log.info("Creating rental contract for company: {}", companyId);

        RentalContract contract = RentalContract.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .includeDriver(request.getIncludeDriver())
                .pricingModel(request.getPricingModel())
                .build();

        RentalContract created = rentalContractService.createContract(companyId, contract);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RentalContractResponse.fromEntity(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get rental contract by ID", description = "Retrieve contract details by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contract found"),
            @ApiResponse(responseCode = "404", description = "Contract not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<RentalContractResponse> getContract(
            @Parameter(description = "Contract ID") @PathVariable Long id) {
        log.info("Retrieving rental contract: {}", id);

        return rentalContractService.getContractById(id)
                .map(contract -> ResponseEntity.ok(RentalContractResponse.fromEntity(contract)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update rental contract", description = "Update contract details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contract updated successfully"),
            @ApiResponse(responseCode = "404", description = "Contract not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<RentalContractResponse> updateContract(
            @Parameter(description = "Contract ID") @PathVariable Long id,
            @Valid @RequestBody RentalContractUpdateRequest request) {
        log.info("Updating rental contract: {}", id);

        RentalContract contract = RentalContract.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .includeDriver(request.getIncludeDriver())
                .pricingModel(request.getPricingModel())
                .build();

        RentalContract updated = rentalContractService.updateContract(id, contract);

        return ResponseEntity.ok(RentalContractResponse.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel rental contract", description = "Cancel a rental contract")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contract cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Contract not found"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Void> cancelContract(
            @Parameter(description = "Contract ID") @PathVariable Long id,
            @Parameter(description = "Cancellation reason") @RequestParam String reason) {
        log.info("Cancelling rental contract: {}", id);

        rentalContractService.cancelContract(id, reason);

        return ResponseEntity.noContent().build();
    }

    // ==================== Contract Listing & Filtering ====================

    @GetMapping
    @Operation(summary = "Get rental contracts for company", description = "Retrieve paginated list of rental contracts")
    @ApiResponse(responseCode = "200", description = "Contracts retrieved successfully")
    public ResponseEntity<Page<RentalContractBasicResponse>> getContracts(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            Pageable pageable) {
        log.info("Retrieving rental contracts for company: {}", companyId);

        Page<RentalContract> contracts = rentalContractService.getContractsByCompany(companyId, pageable);

        return ResponseEntity.ok(contracts.map(RentalContractBasicResponse::fromEntity));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active rental contracts", description = "Retrieve all active contracts for a company")
    @ApiResponse(responseCode = "200", description = "Active contracts retrieved")
    public ResponseEntity<List<RentalContractBasicResponse>> getActiveContracts(
            @Parameter(description = "Company ID") @RequestParam Long companyId) {
        log.info("Retrieving active rental contracts for company: {}", companyId);

        List<RentalContract> contracts = rentalContractService.getActiveContracts(companyId);

        return ResponseEntity.ok(contracts.stream()
                .map(RentalContractBasicResponse::fromEntity)
                .toList());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get contracts by status", description = "Retrieve contracts filtered by status")
    @ApiResponse(responseCode = "200", description = "Contracts retrieved")
    public ResponseEntity<Page<RentalContractBasicResponse>> getContractsByStatus(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @Parameter(description = "Contract status") @PathVariable String status,
            Pageable pageable) {
        log.info("Retrieving rental contracts with status: {}", status);

        Page<RentalContract> contracts = rentalContractService.getContractsByStatus(companyId, status, pageable);

        return ResponseEntity.ok(contracts.map(RentalContractBasicResponse::fromEntity));
    }

    @GetMapping("/expiring")
    @Operation(summary = "Get contracts expiring soon", description = "Retrieve contracts expiring within specified days")
    @ApiResponse(responseCode = "200", description = "Expiring contracts retrieved")
    public ResponseEntity<List<RentalContractBasicResponse>> getExpiringContracts(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @Parameter(description = "Days threshold") @RequestParam(defaultValue = "7") int daysFromNow) {
        log.info("Retrieving contracts expiring in {} days", daysFromNow);

        List<RentalContract> contracts = rentalContractService.getContractsExpiringSoon(companyId, daysFromNow);

        return ResponseEntity.ok(contracts.stream()
                .map(RentalContractBasicResponse::fromEntity)
                .toList());
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue contracts", description = "Retrieve contracts that have exceeded their end date")
    @ApiResponse(responseCode = "200", description = "Overdue contracts retrieved")
    public ResponseEntity<List<RentalContractBasicResponse>> getOverdueContracts(
            @Parameter(description = "Company ID") @RequestParam Long companyId) {
        log.info("Retrieving overdue contracts");

        List<RentalContract> contracts = rentalContractService.getOverdueContracts(companyId);

        return ResponseEntity.ok(contracts.stream()
                .map(RentalContractBasicResponse::fromEntity)
                .toList());
    }

    // ==================== Pricing & Cost Calculation ====================

    @GetMapping("/{id}/cost")
    @Operation(summary = "Calculate rental cost", description = "Get detailed cost breakdown for a rental contract")
    @ApiResponse(responseCode = "200", description = "Cost calculated successfully")
    public ResponseEntity<RentalContractService.RentalCostBreakdown> calculateCost(
            @Parameter(description = "Contract ID") @PathVariable Long id) {
        log.info("Calculating cost for contract: {}", id);

        return ResponseEntity.ok(rentalContractService.calculateRentalCost(id));
    }

    @PostMapping("/{id}/apply-discount")
    @Operation(summary = "Apply discount to contract", description = "Apply a discount to rental contract")
    @ApiResponse(responseCode = "200", description = "Discount applied successfully")
    public ResponseEntity<Void> applyDiscount(
            @Parameter(description = "Contract ID") @PathVariable Long id,
            @Parameter(description = "Discount amount") @RequestParam BigDecimal amount,
            @Parameter(description = "Reason") @RequestParam String reason) {
        log.info("Applying discount {} to contract: {}", amount, id);

        rentalContractService.applyDiscount(id, amount, reason);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/add-charge")
    @Operation(summary = "Add additional charge", description = "Add an additional charge to rental contract")
    @ApiResponse(responseCode = "200", description = "Charge added successfully")
    public ResponseEntity<Void> addCharge(
            @Parameter(description = "Contract ID") @PathVariable Long id,
            @Parameter(description = "Charge amount") @RequestParam BigDecimal amount,
            @Parameter(description = "Description") @RequestParam String description) {
        log.info("Adding charge {} to contract: {}", amount, id);

        rentalContractService.addAdditionalCharge(id, amount, description);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/cost-per-day")
    @Operation(summary = "Get daily rental cost", description = "Calculate cost per day for a contract")
    @ApiResponse(responseCode = "200", description = "Cost calculated")
    public ResponseEntity<BigDecimal> getCostPerDay(
            @Parameter(description = "Contract ID") @PathVariable Long id) {
        log.info("Getting cost per day for contract: {}", id);

        return ResponseEntity.ok(rentalContractService.getCostPerDay(id));
    }

    @GetMapping("/{id}/late-fees")
    @Operation(summary = "Calculate late fees", description = "Calculate late fees if contract is overdue")
    @ApiResponse(responseCode = "200", description = "Late fees calculated")
    public ResponseEntity<BigDecimal> calculateLateFees(
            @Parameter(description = "Contract ID") @PathVariable Long id) {
        log.info("Calculating late fees for contract: {}", id);

        return ResponseEntity.ok(rentalContractService.calculateLateFees(id));
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Get outstanding balance", description = "Get amount still owed for a contract")
    @ApiResponse(responseCode = "200", description = "Balance retrieved")
    public ResponseEntity<BigDecimal> getOutstandingBalance(
            @Parameter(description = "Contract ID") @PathVariable Long id) {
        log.info("Getting outstanding balance for contract: {}", id);

        return ResponseEntity.ok(rentalContractService.getOutstandingBalance(id));
    }

    // ==================== Invoice & Payment ====================

    @PostMapping("/{id}/generate-invoice")
    @Operation(summary = "Generate invoice", description = "Generate invoice for a rental contract")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Invoice generated successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot generate invoice"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<Long> generateInvoice(
            @Parameter(description = "Contract ID") @PathVariable Long id) {
        log.info("Generating invoice for contract: {}", id);

        Long invoiceId = rentalContractService.generateInvoice(id);

        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceId);
    }

    @PostMapping("/{id}/process-payment")
    @Operation(summary = "Process payment", description = "Process a payment for a rental contract")
    @ApiResponse(responseCode = "200", description = "Payment processed successfully")
    public ResponseEntity<Void> processPayment(
            @Parameter(description = "Contract ID") @PathVariable Long id,
            @Parameter(description = "Payment amount") @RequestParam BigDecimal amount,
            @Parameter(description = "Payment method") @RequestParam String paymentMethod) {
        log.info("Processing payment of {} for contract: {}", amount, id);

        rentalContractService.processPayment(id, amount, paymentMethod);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/send-payment-reminder")
    @Operation(summary = "Send payment reminder", description = "Send payment reminder to client")
    @ApiResponse(responseCode = "200", description = "Reminder sent successfully")
    public ResponseEntity<Void> sendPaymentReminder(
            @Parameter(description = "Contract ID") @PathVariable Long id) {
        log.info("Sending payment reminder for contract: {}", id);

        rentalContractService.sendPaymentReminder(id);

        return ResponseEntity.ok().build();
    }

    // ==================== Rental Management ====================

    @PostMapping("/{id}/start")
    @Operation(summary = "Start rental", description = "Mark rental as started")
    @ApiResponse(responseCode = "200", description = "Rental started")
    public ResponseEntity<Void> startRental(
            @Parameter(description = "Contract ID") @PathVariable Long id) {
        log.info("Starting rental for contract: {}", id);

        rentalContractService.startRental(id);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete rental", description = "Mark rental as completed")
    @ApiResponse(responseCode = "200", description = "Rental completed")
    public ResponseEntity<Void> completeRental(
            @Parameter(description = "Contract ID") @PathVariable Long id) {
        log.info("Completing rental for contract: {}", id);

        rentalContractService.completeRental(id);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/extend")
    @Operation(summary = "Extend rental period", description = "Extend rental by additional days")
    @ApiResponse(responseCode = "200", description = "Rental extended")
    public ResponseEntity<Void> extendRental(
            @Parameter(description = "Contract ID") @PathVariable Long id,
            @Parameter(description = "Additional days") @RequestParam int additionalDays) {
        log.info("Extending rental {} by {} days", id, additionalDays);

        rentalContractService.extendRental(id, additionalDays);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/remaining-days")
    @Operation(summary = "Get remaining days", description = "Get number of days remaining in rental")
    @ApiResponse(responseCode = "200", description = "Days calculated")
    public ResponseEntity<Long> getRemainingDays(
            @Parameter(description = "Contract ID") @PathVariable Long id) {
        log.info("Getting remaining days for contract: {}", id);

        return ResponseEntity.ok(rentalContractService.getRemainingDays(id));
    }

    @GetMapping("/{id}/early-return-refund")
    @Operation(summary = "Calculate early return refund", description = "Calculate refund amount if returned early")
    @ApiResponse(responseCode = "200", description = "Refund calculated")
    public ResponseEntity<BigDecimal> calculateEarlyReturnRefund(
            @Parameter(description = "Contract ID") @PathVariable Long id) {
        log.info("Calculating early return refund for contract: {}", id);

        return ResponseEntity.ok(rentalContractService.calculateEarlyReturnRefund(id));
    }

    // ==================== Reporting ====================

    @GetMapping("/report/summary")
    @Operation(summary = "Get summary report", description = "Get rental summary report for company")
    @ApiResponse(responseCode = "200", description = "Report generated")
    public ResponseEntity<RentalContractService.ContractSummaryReport> getSummaryReport(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @Parameter(description = "From date (timestamp)") @RequestParam Long fromDate,
            @Parameter(description = "To date (timestamp)") @RequestParam Long toDate) {
        log.info("Generating summary report for company: {}", companyId);

        return ResponseEntity.ok(rentalContractService.getSummaryReport(companyId, fromDate, toDate));
    }

    @GetMapping("/report/utilization")
    @Operation(summary = "Get utilization report", description = "Get vehicle utilization report")
    @ApiResponse(responseCode = "200", description = "Report generated")
    public ResponseEntity<RentalContractService.RentalUtilizationReport> getUtilizationReport(
            @Parameter(description = "Company ID") @RequestParam Long companyId,
            @Parameter(description = "From date (timestamp)") @RequestParam Long fromDate,
            @Parameter(description = "To date (timestamp)") @RequestParam Long toDate) {
        log.info("Generating utilization report for company: {}", companyId);

        return ResponseEntity.ok(rentalContractService.getUtilizationReport(companyId, fromDate, toDate));
    }
}
