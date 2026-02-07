package com.devcast.fleetmanagement.features.payroll.controller;

import com.devcast.fleetmanagement.features.auth.dto.ApiResponse;
import com.devcast.fleetmanagement.features.payroll.dto.PayrollPeriodCreateRequest;
import com.devcast.fleetmanagement.features.payroll.dto.PayrollPeriodResponse;
import com.devcast.fleetmanagement.features.payroll.dto.PayrollRecordResponse;
import com.devcast.fleetmanagement.features.payroll.model.PayrollPeriod;
import com.devcast.fleetmanagement.features.payroll.model.PayrollRecord;
import com.devcast.fleetmanagement.features.payroll.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payroll Management", description = "APIs for payroll processing and salary management")
public class PayrollController {

    private final PayrollService payrollService;

    // ==================== Payroll Period APIs ====================

    @PostMapping("/periods/company/{companyId}")
   /* @PreAuthorize("hasAuthority('CREATE_PAYROLL')")*/
    @Operation(summary = "Create payroll period", description = "Create new payroll period for a company")
    public ResponseEntity<ApiResponse<PayrollPeriodResponse>> createPayrollPeriod(
            @PathVariable Long companyId,
            @RequestBody PayrollPeriodCreateRequest request
    ) {
        try {
            PayrollPeriod period = PayrollPeriod.builder()
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .build();

            PayrollPeriod created = payrollService.createPayrollPeriod(companyId, period);
            return ResponseEntity.ok(ApiResponse.<PayrollPeriodResponse>success(
                    PayrollPeriodResponse.fromEntity(created),
                    "Payroll period created successfully"
            ));
        } catch (Exception e) {
            log.error("Error creating payroll period", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<PayrollPeriodResponse>error("Failed to create payroll period: " + e.getMessage()));
        }
    }

    @GetMapping("/periods/{periodId}")
    @Operation(summary = "Get payroll period", description = "Retrieve payroll period details")
    public ResponseEntity<ApiResponse<PayrollPeriodResponse>> getPayrollPeriod(
            @PathVariable Long periodId
    ) {
        try {
            return payrollService.getPayrollPeriodById(periodId)
                    .map(period -> ResponseEntity.ok(ApiResponse.<PayrollPeriodResponse>success(
                            PayrollPeriodResponse.fromEntity(period),
                            "Payroll period retrieved successfully"
                    )))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<PayrollPeriodResponse>error("Failed to retrieve payroll period"));
        }
    }

    @GetMapping("/periods/company/{companyId}")
    @Operation(summary = "Get company payroll periods", description = "Get all payroll periods for a company")
    public ResponseEntity<ApiResponse<Page<PayrollPeriodResponse>>> getCompanyPayrollPeriods(
            @PathVariable Long companyId,
            Pageable pageable
    ) {
        try {
            Page<PayrollPeriod> periods = payrollService.getPayrollPeriods(companyId, pageable);
            Page<PayrollPeriodResponse> responses = periods.map(PayrollPeriodResponse::fromEntity);
            return ResponseEntity.ok(ApiResponse.<Page<PayrollPeriodResponse>>success(
                    responses,
                    "Payroll periods retrieved successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Page<PayrollPeriodResponse>>error("Failed to retrieve payroll periods"));
        }
    }

    @GetMapping("/periods/company/{companyId}/active")
    @Operation(summary = "Get active payroll period", description = "Get currently active payroll period")
    public ResponseEntity<ApiResponse<PayrollPeriodResponse>> getActivePayrollPeriod(
            @PathVariable Long companyId
    ) {
        try {
            return payrollService.getActivePayrollPeriod(companyId)
                    .map(period -> ResponseEntity.ok(ApiResponse.<PayrollPeriodResponse>success(
                            PayrollPeriodResponse.fromEntity(period),
                            "Active payroll period retrieved"
                    )))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<PayrollPeriodResponse>error("Failed to retrieve active payroll period"));
        }
    }

    @PostMapping("/periods/{periodId}/close")
    /*@PreAuthorize("hasAuthority('UPDATE_PAYROLL')")*/
    @Operation(summary = "Close payroll period", description = "Close payroll period for final processing")
    public ResponseEntity<ApiResponse<String>> closePayrollPeriod(
            @PathVariable Long periodId
    ) {
        try {
            payrollService.closePayrollPeriod(periodId);
            return ResponseEntity.ok(ApiResponse.<String>success("Payroll period closed successfully", ""));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>error("Failed to close payroll period: " + e.getMessage()));
        }
    }

    @PostMapping("/periods/{periodId}/finalize")
    /*@PreAuthorize("hasAuthority('UPDATE_PAYROLL')")*/
    @Operation(summary = "Finalize payroll period", description = "Finalize payroll period for payment")
    public ResponseEntity<ApiResponse<String>> finalizePayrollPeriod(
            @PathVariable Long periodId
    ) {
        try {
            payrollService.finalizePayrollPeriod(periodId);
            return ResponseEntity.ok(ApiResponse.<String>success("Payroll period finalized", ""));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>error("Failed to finalize payroll period"));
        }
    }

    // ==================== Payroll Record APIs ====================

    @PostMapping("/periods/{periodId}/generate")
   /* @PreAuthorize("hasAuthority('CREATE_PAYROLL')")*/
    @Operation(summary = "Generate payroll records", description = "Generate payroll records for all employees in period")
    public ResponseEntity<ApiResponse<List<PayrollRecordResponse>>> generatePayrollRecords(
            @PathVariable Long periodId
    ) {
        try {
            List<PayrollRecord> records = payrollService.generatePayrollRecords(periodId);
            List<PayrollRecordResponse> responses = records.stream()
                    .map(PayrollRecordResponse::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.<List<PayrollRecordResponse>>success(
                    responses,
                    "Payroll records generated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<List<PayrollRecordResponse>>error("Failed to generate payroll records"));
        }
    }

    @GetMapping("/records/{recordId}")
    @Operation(summary = "Get payroll record", description = "Retrieve payroll record details")
    public ResponseEntity<ApiResponse<PayrollRecordResponse>> getPayrollRecord(
            @PathVariable Long recordId
    ) {
        try {
            return payrollService.getPayrollRecord(recordId)
                    .map(record -> ResponseEntity.ok(ApiResponse.<PayrollRecordResponse>success(
                            PayrollRecordResponse.fromEntity(record),
                            "Payroll record retrieved"
                    )))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<PayrollRecordResponse>error("Failed to retrieve payroll record"));
        }
    }

    @GetMapping("/periods/{periodId}/records")
    @Operation(summary = "Get period records", description = "Get all payroll records for a period")
    public ResponseEntity<ApiResponse<Page<PayrollRecordResponse>>> getPayrollRecordsForPeriod(
            @PathVariable Long periodId,
            Pageable pageable
    ) {
        try {
            Page<PayrollRecord> records = payrollService.getPayrollRecordsForPeriod(periodId, pageable);
            Page<PayrollRecordResponse> responses = records.map(PayrollRecordResponse::fromEntity);
            return ResponseEntity.ok(ApiResponse.<Page<PayrollRecordResponse>>success(
                    responses,
                    "Payroll records retrieved"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Page<PayrollRecordResponse>>error("Failed to retrieve payroll records"));
        }
    }

    @PostMapping("/records/{recordId}/approve")
   /* @PreAuthorize("hasAuthority('UPDATE_PAYROLL')")*/
    @Operation(summary = "Approve payroll record", description = "Approve payroll record for payment")
    public ResponseEntity<ApiResponse<String>> approvePayrollRecord(
            @PathVariable Long recordId
    ) {
        try {
            payrollService.approvePayrollRecord(recordId);
            return ResponseEntity.ok(ApiResponse.<String>success("Payroll record approved", ""));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>error("Failed to approve payroll record"));
        }
    }

    @PostMapping("/records/{recordId}/reject")
    /*@PreAuthorize("hasAuthority('UPDATE_PAYROLL')")*/
    @Operation(summary = "Reject payroll record", description = "Reject payroll record")
    public ResponseEntity<ApiResponse<String>> rejectPayrollRecord(
            @PathVariable Long recordId,
            @RequestParam String reason
    ) {
        try {
            payrollService.rejectPayrollRecord(recordId, reason);
            return ResponseEntity.ok(ApiResponse.<String>success("Payroll record rejected", ""));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>error("Failed to reject payroll record"));
        }
    }

    // ==================== Salary Calculation APIs ====================

    @GetMapping("/salary/calculate/{driverId}/{periodId}")
    @Operation(summary = "Calculate salary", description = "Calculate employee salary for period")
    public ResponseEntity<ApiResponse<PayrollService.SalaryCalculation>> calculateSalary(
            @PathVariable Long driverId,
            @PathVariable Long periodId
    ) {
        try {
            PayrollService.SalaryCalculation calculation = payrollService.calculateEmployeeSalary(driverId, periodId);
            return ResponseEntity.ok(ApiResponse.<PayrollService.SalaryCalculation>success(
                    calculation,
                    "Salary calculated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<PayrollService.SalaryCalculation>error("Failed to calculate salary"));
        }
    }

    @GetMapping("/salary/base/{driverId}/{periodId}")
    @Operation(summary = "Get base salary", description = "Get base salary amount")
    public ResponseEntity<ApiResponse<BigDecimal>> getBaseSalary(
            @PathVariable Long driverId,
            @PathVariable Long periodId
    ) {
        try {
            BigDecimal baseSalary = payrollService.calculateBaseSalary(driverId, periodId);
            return ResponseEntity.ok(ApiResponse.<BigDecimal>success(baseSalary, "Base salary retrieved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<BigDecimal>error("Failed to retrieve base salary"));
        }
    }

    @GetMapping("/salary/gross/{driverId}/{periodId}")
    @Operation(summary = "Get gross salary", description = "Get gross salary amount")
    public ResponseEntity<ApiResponse<BigDecimal>> getGrossSalary(
            @PathVariable Long driverId,
            @PathVariable Long periodId
    ) {
        try {
            BigDecimal grossSalary = payrollService.calculateGrossSalary(driverId, periodId);
            return ResponseEntity.ok(ApiResponse.<BigDecimal>success(grossSalary, "Gross salary retrieved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<BigDecimal>error("Failed to retrieve gross salary"));
        }
    }

    @GetMapping("/salary/net/{driverId}/{periodId}")
    @Operation(summary = "Get net salary", description = "Get net salary amount")
    public ResponseEntity<ApiResponse<BigDecimal>> getNetSalary(
            @PathVariable Long driverId,
            @PathVariable Long periodId
    ) {
        try {
            BigDecimal netSalary = payrollService.calculateNetSalary(driverId, periodId);
            return ResponseEntity.ok(ApiResponse.<BigDecimal>success(netSalary, "Net salary retrieved"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<BigDecimal>error("Failed to retrieve net salary"));
        }
    }

    // ==================== Payment Processing APIs ====================

    @PostMapping("/records/{recordId}/process-payment")
    /*@PreAuthorize("hasAuthority('UPDATE_PAYROLL')")*/
    @Operation(summary = "Process payment", description = "Process payment for payroll record")
    public ResponseEntity<ApiResponse<String>> processPayment(
            @PathVariable Long recordId,
            @RequestParam String paymentMethod,
            @RequestParam String reference
    ) {
        try {
            payrollService.processPayment(recordId, paymentMethod, reference);
            return ResponseEntity.ok(ApiResponse.<String>success("Payment processed successfully", ""));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>error("Failed to process payment"));
        }
    }

    @PostMapping("/periods/{periodId}/batch-payment")
    /*@PreAuthorize("hasAuthority('UPDATE_PAYROLL')")*/
    @Operation(summary = "Batch process payments", description = "Process payments for all approved records in period")
    public ResponseEntity<ApiResponse<String>> batchProcessPayments(
            @PathVariable Long periodId,
            @RequestParam String paymentMethod
    ) {
        try {
            payrollService.batchProcessPayments(periodId, paymentMethod);
            return ResponseEntity.ok(ApiResponse.<String>success("Batch payment processing completed", ""));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<String>error("Failed to process batch payments"));
        }
    }

    @GetMapping("/periods/{periodId}/payment-status")
    @Operation(summary = "Get payment status", description = "Get payment status for payroll period")
    public ResponseEntity<ApiResponse<PayrollService.PaymentStatus>> getPaymentStatusForPeriod(
            @PathVariable Long periodId
    ) {
        try {
            PayrollService.PaymentStatus status = payrollService.getPaymentStatusForPeriod(periodId);
            return ResponseEntity.ok(ApiResponse.<PayrollService.PaymentStatus>success(
                    status,
                    "Payment status retrieved"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<PayrollService.PaymentStatus>error("Failed to retrieve payment status"));
        }
    }

    // ==================== Reporting APIs ====================

    @GetMapping("/summary/{companyId}/{periodId}")
    @Operation(summary = "Get payroll summary", description = "Get payroll summary report")
    public ResponseEntity<ApiResponse<PayrollService.PayrollSummaryReport>> getPayrollSummary(
            @PathVariable Long companyId,
            @PathVariable Long periodId
    ) {
        try {
            PayrollService.PayrollSummaryReport report = payrollService.getPayrollSummary(companyId, periodId);
            return ResponseEntity.ok(ApiResponse.<PayrollService.PayrollSummaryReport>success(
                    report,
                    "Payroll summary retrieved"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<PayrollService.PayrollSummaryReport>error("Failed to retrieve payroll summary"));
        }
    }

    @GetMapping("/employee/{driverId}/history")
    @Operation(summary = "Get employee payroll history", description = "Get payroll history for employee")
    public ResponseEntity<ApiResponse<List<PayrollRecordResponse>>> getEmployeePayrollHistory(
            @PathVariable Long driverId,
            @RequestParam(defaultValue = "12") int months
    ) {
        try {
            List<PayrollRecord> records = payrollService.getEmployeePayrollHistory(driverId, months);
            List<PayrollRecordResponse> responses = records.stream()
                    .map(PayrollRecordResponse::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponse.<List<PayrollRecordResponse>>success(
                    responses,
                    "Payroll history retrieved"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<List<PayrollRecordResponse>>error("Failed to retrieve payroll history"));
        }
    }
}
