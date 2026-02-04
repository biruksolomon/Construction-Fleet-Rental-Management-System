package com.devcast.fleetmanagement.features.rental.repository;

import com.devcast.fleetmanagement.features.rental.model.RentalVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Rental Vehicle Repository
 * Handles database operations for rental vehicles within contracts
 */
@Repository
public interface RentalVehicleRepository extends JpaRepository<RentalVehicle, Long> {

    /**
     * Find all rental vehicles in a specific contract
     */
    List<RentalVehicle> findByRentalContractId(Long rentalContractId);

    /**
     * Find rental vehicles for a specific vehicle
     */
    List<RentalVehicle> findByVehicleId(Long vehicleId);

    /**
     * Find rental vehicles with a specific driver
     */
    List<RentalVehicle> findByDriverId(Long driverId);

    /**
     * Count rental vehicles in a contract
     */
    long countByRentalContractId(Long rentalContractId);
}
