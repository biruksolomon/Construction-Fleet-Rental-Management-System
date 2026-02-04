package com.devcast.fleetmanagement.features.rental.model;

import com.devcast.fleetmanagement.features.vehicle.model.Vehicle;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "rental_vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_contract_id", nullable = false)
    private RentalContract rentalContract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private com.devcast.fleetmanagement.features.driver.model.Driver driver;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal agreedRate;
}
