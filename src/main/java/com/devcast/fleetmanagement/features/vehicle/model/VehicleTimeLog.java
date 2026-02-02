package com.devcast.fleetmanagement.features.vehicle.model;

import com.devcast.fleetmanagement.features.rental.model.RentalContract;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_time_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleTimeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_contract_id")
    private RentalContract rentalContract;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogSource source;

    public enum LogSource {
        GPS,
        MOBILE,
        MANUAL
    }
}
