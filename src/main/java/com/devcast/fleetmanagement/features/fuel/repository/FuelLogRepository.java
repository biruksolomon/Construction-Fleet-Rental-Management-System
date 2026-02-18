package com.devcast.fleetmanagement.features.fuel.repository;

import com.devcast.fleetmanagement.features.fuel.model.FuelLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Fuel Log Repository
 * Handles fuel consumption tracking and analysis
 */
@Repository
public interface FuelLogRepository extends JpaRepository<FuelLog, Long> {

    /**
     * Find all fuel logs for a vehicle with pagination
     */
    Page<FuelLog> findByVehicleId(Long vehicleId, Pageable pageable);

    /**
     * Find fuel logs within date range
     */
    @Query("select f from FuelLog f where f.vehicle.id = :vehicleId and f.refillDate between :startDate and :endDate order by f.refillDate desc")
    List<FuelLog> findByVehicleIdAndDateRange(
            @Param("vehicleId") Long vehicleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Calculate total fuel consumed
     */
    @Query("select sum(f.liters) from FuelLog f where f.vehicle.id = :vehicleId")
    Optional<BigDecimal> calculateTotalFuelConsumed(@Param("vehicleId") Long vehicleId);

    /**
     * Calculate total fuel cost
     */
    @Query("select sum(f.cost) from FuelLog f where f.vehicle.id = :vehicleId")
    Optional<BigDecimal> calculateTotalFuelCost(@Param("vehicleId") Long vehicleId);

    /**
     * Calculate average fuel cost per liter
     */
    @Query("select sum(f.cost) / sum(f.liters) from FuelLog f where f.vehicle.id = :vehicleId")
    Optional<BigDecimal> calculateAverageCostPerLiter(@Param("vehicleId") Long vehicleId);

    /**
     * Find latest fuel log for a vehicle
     */
    @Query("select f from FuelLog f where f.vehicle.id = :vehicleId order by f.refillDate desc limit 1")
    Optional<FuelLog> findLatestByVehicleId(@Param("vehicleId") Long vehicleId);

    /**
     * Count fuel logs for a vehicle
     */
    long countByVehicleId(Long vehicleId);

    /**
     * Find fuel logs within date range for a specific vehicle
     */
    List<FuelLog> findByVehicleIdAndRefillDateBetween(Long vehicleId, LocalDate startDate, LocalDate endDate);
}
