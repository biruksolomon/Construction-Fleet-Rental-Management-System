package com.devcast.fleetmanagement.features.rental.model;

import com.devcast.fleetmanagement.features.company.model.Company;
import com.devcast.fleetmanagement.features.driver.model.Driver;
import com.devcast.fleetmanagement.features.vehicle.model.Vehicle;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rental_vehicles", indexes = {
        @Index(name = "idx_rental_contract_id", columnList = "rental_contract_id"),
        @Index(name = "idx_vehicle_id", columnList = "vehicle_id"),
        @Index(name = "idx_driver_id", columnList = "driver_id"),
        @Index(name = "idx_company_id", columnList = "company_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_contract_id", nullable = false)
    private RentalContract rentalContract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal agreedRate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Validate driver if assigned
     */
    public void validateDriver() {
        if (driver != null && driver.getStatus() != Driver.DriverStatus.ACTIVE) {
            throw new IllegalArgumentException("Driver must be in ACTIVE status to be assigned to rental");
        }
    }
}
