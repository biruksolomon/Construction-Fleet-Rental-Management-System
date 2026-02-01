package com.DevCast.Fleet_Management.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fuel_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(nullable = false)
    private LocalDate refillDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal liters;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordedBy recordedBy;

    public enum RecordedBy {
        SYSTEM,
        MANUAL
    }
}
