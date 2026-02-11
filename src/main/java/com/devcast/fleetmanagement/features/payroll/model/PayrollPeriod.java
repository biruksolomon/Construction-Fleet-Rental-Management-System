package com.devcast.fleetmanagement.features.payroll.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payroll_periods")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Schema(hidden = true)
    private com.devcast.fleetmanagement.features.company.model.Company company;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalGrossSalary;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDeductions;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalNetSalary;

    @Column
    private LocalDateTime approvedDate;

    @Column
    private String approvedBy;

    @Column
    private LocalDateTime closedDate;

    @Column
    private String closedBy;

    @Column
    private LocalDateTime finalizedDate;

    @Column
    private String finalizedBy;

    @OneToMany(mappedBy = "payrollPeriod", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("payrollPeriod")
    @Schema(hidden = true)
    @Builder.Default
    private List<PayrollRecord> payrollRecords = new ArrayList<>();

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    public enum PayrollStatus {
        OPEN,
        LOCKED,
        CLOSED,
        FINALIZED,
        CANCELLED
    }
}
