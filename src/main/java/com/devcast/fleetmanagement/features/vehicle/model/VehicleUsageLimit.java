package com.devcast.fleetmanagement.features.vehicle.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicle_usage_limits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleUsageLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false)
    private Integer maxHoursPerDay;

    @Column(nullable = false)
    private Integer maxHoursPerMonth;

    @Column(nullable = false)
    private Integer maintenanceIntervalHours;
}
