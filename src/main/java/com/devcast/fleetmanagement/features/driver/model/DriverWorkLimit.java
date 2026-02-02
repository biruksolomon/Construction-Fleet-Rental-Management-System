package com.devcast.fleetmanagement.features.driver.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "driver_work_limits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverWorkLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @Column(nullable = false)
    private Integer maxHoursPerDay;

    @Column(nullable = false)
    private Integer maxHoursPerWeek;
}
