package com.devcast.fleetmanagement.features.fuel.model;

import com.devcast.fleetmanagement.features.vehicle.model.Vehicle;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "fuel_analysis")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Schema(hidden = true)
    private Vehicle vehicle;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal expectedConsumption;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal actualConsumption;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal variance;

    @Column(nullable = false)
    private Boolean alertGenerated;
}
