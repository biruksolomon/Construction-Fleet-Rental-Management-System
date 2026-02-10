package com.devcast.fleetmanagement.features.rental.repository;

import com.devcast.fleetmanagement.features.rental.model.RentalVehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Rental Vehicle Repository
 * Handles database operations for rental vehicles within contracts
 * Enhanced with overlap detection for double booking prevention
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

    /**
     * Find rental vehicle by vehicle ID and contract ID
     */
    Optional<RentalVehicle> findByRentalContractIdAndVehicleId(Long rentalContractId, Long vehicleId);

    /**
     * Double Booking Prevention
     * Check if vehicle has overlapping rentals (excludes current contract)
     */
    @Query("SELECT COUNT(rv) FROM RentalVehicle rv " +
            "WHERE rv.vehicle.id = :vehicleId " +
            "AND rv.rentalContract.status IN ('PENDING', 'ACTIVE') " +
            "AND rv.company.id = :companyId " +
            "AND rv.rentalContract.id != :excludeContractId " +
            "AND NOT (rv.rentalContract.endDate < :startDate OR rv.rentalContract.startDate > :endDate)")
    long countOverlappingVehicleRentals(@Param("companyId") Long companyId,
                                        @Param("vehicleId") Long vehicleId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("excludeContractId") Long excludeContractId);

    /**
     * Double Booking Prevention
     * Check if driver has overlapping assignments (excludes current contract)
     */
    @Query("SELECT COUNT(rv) FROM RentalVehicle rv " +
            "WHERE rv.driver.id = :driverId " +
            "AND rv.rentalContract.status IN ('PENDING', 'ACTIVE') " +
            "AND rv.company.id = :companyId " +
            "AND rv.rentalContract.id != :excludeContractId " +
            "AND NOT (rv.rentalContract.endDate < :startDate OR rv.rentalContract.startDate > :endDate)")
    long countOverlappingDriverAssignments(@Param("companyId") Long companyId,
                                           @Param("driverId") Long driverId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate,
                                           @Param("excludeContractId") Long excludeContractId);

    /**
     * Find active rental vehicles by vehicle
     */
    @Query("SELECT rv FROM RentalVehicle rv " +
            "WHERE rv.vehicle.id = :vehicleId " +
            "AND rv.rentalContract.status = 'ACTIVE'")
    List<RentalVehicle> findActiveRentalsByVehicle(@Param("vehicleId") Long vehicleId);

    /**
     * Find active rental vehicles by driver
     */
    @Query("SELECT rv FROM RentalVehicle rv " +
            "WHERE rv.driver.id = :driverId " +
            "AND rv.rentalContract.status = 'ACTIVE'")
    List<RentalVehicle> findActiveRentalsByDriver(@Param("driverId") Long driverId);
}
