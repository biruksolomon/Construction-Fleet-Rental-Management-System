package com.devcast.fleetmanagement.features.vehicle.repository;

import com.devcast.fleetmanagement.features.vehicle.model.VehicleTimeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Vehicle Time Log Repository
 * Handles vehicle usage time tracking
 */
@Repository
public interface VehicleTimeLogRepository extends JpaRepository<VehicleTimeLog, Long> {

    /**
     * Find all time logs for a vehicle with pagination
     */
    Page<VehicleTimeLog> findByVehicleId(Long vehicleId, Pageable pageable);

    /**
     * Find latest active time log for a vehicle (end time is null)
     */
    @Query("select t from VehicleTimeLog t where t.vehicle.id = :vehicleId and t.endTime is null order by t.startTime desc")
    Optional<VehicleTimeLog> findLatestActiveLog(@Param("vehicleId") Long vehicleId);

    /**
     * Find time logs for a rental contract
     */
    @Query("select t from VehicleTimeLog t where t.rentalContract.id = :contractId order by t.startTime desc")
    List<VehicleTimeLog> findByRentalContractId(@Param("contractId") Long contractId);

    /**
     * Find time logs within time range
     */
    @Query("select t from VehicleTimeLog t where t.vehicle.id = :vehicleId and t.startTime between :startTime and :endTime order by t.startTime desc")
    List<VehicleTimeLog> findByVehicleIdAndTimeRange(
            @Param("vehicleId") Long vehicleId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * Calculate total hours for a vehicle
     */
    @Query("select sum(t.totalHours) from VehicleTimeLog t where t.vehicle.id = :vehicleId")
    Double calculateTotalHours(@Param("vehicleId") Long vehicleId);

    /**
     * Count time logs for a vehicle
     */
    long countByVehicleId(Long vehicleId);
}
