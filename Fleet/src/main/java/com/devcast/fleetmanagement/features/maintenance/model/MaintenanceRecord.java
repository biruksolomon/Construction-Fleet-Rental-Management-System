package com.devcast.fleetmanagement.features.maintenance.model;

import com.devcast.fleetmanagement.features.vehicle.model.Vehicle;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "maintenance_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceType maintenanceType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(nullable = false)
    private LocalDate maintenanceDate;

    @Column(nullable = false)
    private Integer nextDueHours;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public enum MaintenanceType {
        SERVICE,
        REPAIR
    }
}
