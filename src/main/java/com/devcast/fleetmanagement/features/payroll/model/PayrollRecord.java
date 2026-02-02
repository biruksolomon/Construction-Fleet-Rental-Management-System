package com.devcast.fleetmanagement.features.payroll.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

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
    private com.devcast.fleetmanagement.features.driver.model.Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_period_id", nullable = false)
    private PayrollPeriod payrollPeriod;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalHours;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basePay;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal overtimePay;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal deductions;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal netPay;
}
