package com.devcast.fleetmanagement.features.payroll.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
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
    private com.devcast.fleetmanagement.features.company.model.Company company;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayrollStatus status;

    @OneToMany(mappedBy = "payrollPeriod", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PayrollRecord> payrollRecords = new ArrayList<>();

    public enum PayrollStatus {
        OPEN,
        CLOSED
    }
}
