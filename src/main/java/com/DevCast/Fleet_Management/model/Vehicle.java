package com.DevCast.Fleet_Management.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 50)
    private String plateNumber;

    @Column(nullable = false, length = 100)
    private String assetCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuelType fuelType;

    @Column(precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(precision = 10, scale = 2)
    private BigDecimal dailyRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleStatus status;

    @Column(nullable = false)
    private Boolean hasGps;

    @Column(nullable = false)
    private Boolean hasFuelSensor;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (hasGps == null) hasGps = false;
        if (hasFuelSensor == null) hasFuelSensor = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum VehicleType {
        CAR,
        TRUCK,
        EXCAVATOR,
        BULLDOZER,
        CRANE
    }

    public enum FuelType {
        DIESEL,
        PETROL,
        ELECTRIC
    }

    public enum VehicleStatus {
        AVAILABLE,
        RENTED,
        MAINTENANCE,
        INACTIVE
    }
}
