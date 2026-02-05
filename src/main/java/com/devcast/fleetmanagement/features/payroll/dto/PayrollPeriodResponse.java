package com.devcast.fleetmanagement.features.payroll.dto;

import com.devcast.fleetmanagement.features.payroll.model.PayrollPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollPeriodResponse {
    
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private BigDecimal totalGrossSalary;
    private BigDecimal totalDeductions;
    private BigDecimal totalNetSalary;
    private LocalDateTime approvedDate;
    private String approvedBy;
    private LocalDateTime closedDate;
    private String closedBy;
    
    public static PayrollPeriodResponse fromEntity(PayrollPeriod period) {
        return PayrollPeriodResponse.builder()
                .id(period.getId())
                .startDate(period.getStartDate())
                .endDate(period.getEndDate())
                .status(period.getStatus().toString())
                .totalGrossSalary(period.getTotalGrossSalary())
                .totalDeductions(period.getTotalDeductions())
                .totalNetSalary(period.getTotalNetSalary())
                .approvedDate(period.getApprovedDate())
                .approvedBy(period.getApprovedBy())
                .closedDate(period.getClosedDate())
                .closedBy(period.getClosedBy())
                .build();
    }
}
