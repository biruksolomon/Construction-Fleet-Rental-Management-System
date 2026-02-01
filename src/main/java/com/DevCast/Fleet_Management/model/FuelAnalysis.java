package com.DevCast.Fleet_Management.model;

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
