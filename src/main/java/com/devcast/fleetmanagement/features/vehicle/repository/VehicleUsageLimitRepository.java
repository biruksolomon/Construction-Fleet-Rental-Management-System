package com.devcast.fleetmanagement.features.vehicle.repository;

import com.devcast.fleetmanagement.features.vehicle.model.VehicleUsageLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Vehicle Usage Limit Repository
 * Handles vehicle usage limit and maintenance interval configurations
 */
@Repository
public interface VehicleUsageLimitRepository extends JpaRepository<VehicleUsageLimit, Long> {

    /**
     * Find usage limit by vehicle ID
     */
    Optional<VehicleUsageLimit> findByVehicleId(Long vehicleId);

    /**
     * Check if vehicle has usage limits configured
     */
    @Query("select case when count(v) > 0 then true else false end from VehicleUsageLimit v where v.vehicle.id = :vehicleId")
    boolean existsByVehicleId(@Param("vehicleId") Long vehicleId);

    /**
     * Update daily usage limit
     */
    @Query("update VehicleUsageLimit v set v.maxHoursPerDay = :hours where v.vehicle.id = :vehicleId")
    void updateMaxHoursPerDay(@Param("vehicleId") Long vehicleId, @Param("hours") Integer hours);

    /**
     * Update monthly usage limit
     */
    @Query("update VehicleUsageLimit v set v.maxHoursPerMonth = :hours where v.vehicle.id = :vehicleId")
    void updateMaxHoursPerMonth(@Param("vehicleId") Long vehicleId, @Param("hours") Integer hours);

    /**
     * Update maintenance interval
     */
    @Query("update VehicleUsageLimit v set v.maintenanceIntervalHours = :hours where v.vehicle.id = :vehicleId")
    void updateMaintenanceInterval(@Param("vehicleId") Long vehicleId, @Param("hours") Integer hours);
}
