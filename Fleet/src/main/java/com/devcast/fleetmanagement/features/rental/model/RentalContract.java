package com.devcast.fleetmanagement.features.rental.model;

import com.devcast.fleetmanagement.features.company.model.Client;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rental_contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private com.devcast.fleetmanagement.features.company.model.Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false, length = 50)
    private String contractNumber;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Boolean includeDriver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PricingModel pricingModel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContractStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "rentalContract", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RentalVehicle> rentalVehicles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum PricingModel {
        HOURLY,
        DAILY,
        PROJECT
    }

    public enum ContractStatus {
        ACTIVE,
        COMPLETED,
        CANCELLED
    }
}
