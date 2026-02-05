package com.devcast.fleetmanagement.features.payroll.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payroll_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    @JsonIgnore
    private com.devcast.fleetmanagement.features.driver.model.Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_period_id", nullable = false)
    @JsonIgnore
    private PayrollPeriod payrollPeriod;

    @Column(nullable = false)
    private Long totalWorkHours;

    @Column(nullable = false)
    private Long overtimeHours;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basePay;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal workHourBonus;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal performanceBonus;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal incentives;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal grossSalary;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal taxDeduction;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal insuranceDeduction;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal loanDeduction;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal otherDeductions;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDeductions;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal netPay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollStatus status;

    @Column
    private String approvedBy;

    @Column
    private LocalDateTime approvedDate;

    @Column
    private String paymentMethod;

    @Column
    private String paymentReference;

    @Column
    private String remarks;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    public enum PayrollStatus {
        DRAFT,
        SUBMITTED,
        APPROVED,
        PAID,
        REJECTED,
        VOID
    }
}
