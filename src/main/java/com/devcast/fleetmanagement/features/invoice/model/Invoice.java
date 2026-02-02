package com.devcast.fleetmanagement.features.invoice.model;

import com.devcast.fleetmanagement.features.rental.model.RentalContract;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private com.devcast.fleetmanagement.features.company.model.Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_contract_id", nullable = false)
    private RentalContract rentalContract;

    @Column(nullable = false, length = 50)
    private String invoiceNumber;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal vehicleCost;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal driverCost;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal fuelCost;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    @Column(nullable = false)
    private LocalDate issuedDate;

    public enum InvoiceStatus {
        PENDING,
        PAID
    }
}
