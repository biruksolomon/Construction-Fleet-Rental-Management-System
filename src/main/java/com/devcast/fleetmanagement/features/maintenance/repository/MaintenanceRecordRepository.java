package com.devcast.fleetmanagement.features.maintenance.repository;

import com.devcast.fleetmanagement.features.maintenance.model.MaintenanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Maintenance Record Repository
 * Handles vehicle maintenance and repair tracking
 */
@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {

    /**
     * Find all maintenance records for a vehicle with pagination
     */
    Page<MaintenanceRecord> findByVehicleId(Long vehicleId, Pageable pageable);

    /**
     * Find maintenance records within date range
     */
    @Query("select m from MaintenanceRecord m where m.vehicle.id = :vehicleId and m.maintenanceDate between :startDate and :endDate order by m.maintenanceDate desc")
    List<MaintenanceRecord> findByVehicleIdAndDateRange(
            @Param("vehicleId") Long vehicleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find pending maintenance records (future due dates)
     */
    @Query("select m from MaintenanceRecord m where m.vehicle.id = :vehicleId and m.nextDueHours > :currentHours order by m.nextDueHours asc")
    List<MaintenanceRecord> findPendingByVehicleId(
            @Param("vehicleId") Long vehicleId,
            @Param("currentHours") LocalDateTime currentHours
    );

    /**
     * Find maintenance records by type
     */
    @Query("select m from MaintenanceRecord m where m.vehicle.id = :vehicleId and m.maintenanceType = :type order by m.maintenanceDate desc")
    List<MaintenanceRecord> findByVehicleIdAndType(
            @Param("vehicleId") Long vehicleId,
            @Param("type") MaintenanceRecord.MaintenanceType type
    );

    /**
     * Find latest maintenance record
     */
    @Query("select m from MaintenanceRecord m where m.vehicle.id = :vehicleId order by m.maintenanceDate desc limit 1")
    Optional<MaintenanceRecord> findLatestByVehicleId(@Param("vehicleId") Long vehicleId);

    /**
     * Find overdue maintenance records
     */
    @Query("select m from MaintenanceRecord m where m.vehicle.id = :vehicleId and m.nextDueHours < :currentHours order by m.nextDueHours asc")
    List<MaintenanceRecord> findOverdueByVehicleId(
            @Param("vehicleId") Long vehicleId,
            @Param("currentHours") Integer currentHours
    );

    /**
     * Count maintenance records for a vehicle
     */
    long countByVehicleId(Long vehicleId);

    /**
     * Calculate total maintenance cost
     */
    @Query("select sum(m.cost) from MaintenanceRecord m where m.vehicle.id = :vehicleId")
    java.math.BigDecimal calculateTotalCost(@Param("vehicleId") Long vehicleId);
}
