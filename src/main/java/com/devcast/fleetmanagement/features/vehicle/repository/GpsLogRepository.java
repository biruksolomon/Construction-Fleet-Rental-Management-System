package com.devcast.fleetmanagement.features.vehicle.repository;

import com.devcast.fleetmanagement.features.vehicle.model.GpsLog;
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
 * GPS Log Repository
 * Handles GPS location tracking data for vehicles
 */
@Repository
public interface GpsLogRepository extends JpaRepository<GpsLog, Long> {

    /**
     * Find all GPS logs for a vehicle with pagination
     */
    Page<GpsLog> findByVehicleId(Long vehicleId, Pageable pageable);

    /**
     * Find latest GPS log for a vehicle
     */
    @Query("select g from GpsLog g where g.vehicle.id = :vehicleId order by g.recordedAt desc limit 1")
    Optional<GpsLog> findLatestByVehicleId(@Param("vehicleId") Long vehicleId);

    /**
     * Find GPS logs within time range for a vehicle
     */
    @Query("select g from GpsLog g where g.vehicle.id = :vehicleId and g.recordedAt between :startTime and :endTime order by g.recordedAt desc")
    List<GpsLog> findByVehicleIdAndTimeRange(
            @Param("vehicleId") Long vehicleId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * Find GPS logs for a vehicle within specific location bounds
     */
    @Query("select g from GpsLog g where g.vehicle.id = :vehicleId " +
            "and g.latitude between :minLat and :maxLat " +
            "and g.longitude between :minLon and :maxLon " +
            "order by g.recordedAt desc")
    List<GpsLog> findByLocationBounds(
            @Param("vehicleId") Long vehicleId,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLon") double minLon,
            @Param("maxLon") double maxLon
    );

    /**
     * Count GPS logs for a vehicle
     */
    long countByVehicleId(Long vehicleId);

    /**
     * Delete GPS logs older than specified date
     */
    void deleteByRecordedAtBefore(LocalDateTime dateTime);
}
