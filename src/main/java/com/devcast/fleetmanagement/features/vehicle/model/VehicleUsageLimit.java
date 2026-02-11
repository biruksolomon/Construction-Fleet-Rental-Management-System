package com.devcast.fleetmanagement.features.vehicle.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Schema(hidden = true)
    private Vehicle vehicle;

    @Column(nullable = false)
    private Integer maxHoursPerDay;

    @Column(nullable = false)
    private Integer maxHoursPerMonth;

    @Column(nullable = false)
    private Integer maintenanceIntervalHours;
}
