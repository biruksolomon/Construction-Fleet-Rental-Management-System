package com.devcast.fleetmanagement.features.maintenance.repository;

import com.devcast.fleetmanagement.features.maintenance.model.MaintenanceRecord;
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
 * Maintenance Record Repository
 * Handles vehicle maintenance and repair tracking with multi-tenant support
 */
@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, Long> {

    /**
     * Find all maintenance records for a vehicle
     */
    List<MaintenanceRecord> findByVehicleId(Long vehicleId);

    /**
     * Find all maintenance records for a vehicle with pagination
     */
    Page<MaintenanceRecord> findByVehicleId(Long vehicleId, Pageable pageable);

    /**
     * Find pending maintenance records for a vehicle
     * Pending means records with SCHEDULED or IN_PROGRESS status
     */
    @Query("select m from MaintenanceRecord m where m.vehicle.id = :vehicleId and m.status in ('SCHEDULED', 'IN_PROGRESS') order by m.maintenanceDate asc")
    List<MaintenanceRecord> findPendingByVehicleId(@Param("vehicleId") Long vehicleId);

    /**
     * Find all maintenance records for a vehicle and company
     */
    List<MaintenanceRecord> findByVehicleIdAndCompanyId(Long vehicleId, Long companyId);

    /**
     * Find all maintenance records for a vehicle and company with pagination
     */
    Page<MaintenanceRecord> findByVehicleIdAndCompanyId(Long vehicleId, Long companyId, Pageable pageable);

    /**
     * Find pending maintenance records for vehicle within company
     * Pending means records with SCHEDULED or IN_PROGRESS status with company isolation
     */
    @Query("select m from MaintenanceRecord m where m.vehicle.id = :vehicleId and m.company.id = :companyId and m.status in ('SCHEDULED', 'IN_PROGRESS') order by m.maintenanceDate asc")
    List<MaintenanceRecord> findPendingByVehicleIdAndCompanyId(
            @Param("vehicleId") Long vehicleId,
            @Param("companyId") Long companyId
    );

    /**
     * Find all maintenance records by company
     */
    List<MaintenanceRecord> findByCompanyId(Long companyId);

    /**
     * Find maintenance records by company with pagination
     */
    Page<MaintenanceRecord> findByCompanyId(Long companyId, Pageable pageable);

    /**
     * Find maintenance records by company and status
     */
    List<MaintenanceRecord> findByCompanyIdAndStatus(Long companyId, MaintenanceRecord.MaintenanceStatus status);

    /**
     * Find maintenance records by company and status with pagination
     */
    Page<MaintenanceRecord> findByCompanyIdAndStatus(Long companyId, MaintenanceRecord.MaintenanceStatus status, Pageable pageable);

    /**
     * Find maintenance records by company and service type
     */
    List<MaintenanceRecord> findByCompanyIdAndServiceType(Long companyId, String serviceType);

    /**
     * Find maintenance records by company and vendor name
     */
    List<MaintenanceRecord> findByCompanyIdAndVendorName(Long companyId, String vendorName);

    /**
     * Find maintenance records by company and maintenance type
     */
    List<MaintenanceRecord> findByCompanyIdAndMaintenanceType(Long companyId, MaintenanceRecord.MaintenanceType maintenanceType);

    /**
     * Find maintenance record by ID and company ID
     */
    @Query("select m from MaintenanceRecord m where m.id = :id and m.company.id = :companyId")
    Optional<MaintenanceRecord> findByIdAndCompanyId(@Param("id") Long id, @Param("companyId") Long companyId);

    /**
     * Count maintenance records for a vehicle
     */
    long countByVehicleId(Long vehicleId);

    /**
     * Count maintenance records for a vehicle and company
     */
    long countByVehicleIdAndCompanyId(Long vehicleId, Long companyId);

    /**
     * Count maintenance records for a company
     */
    long countByCompanyId(Long companyId);

    /**
     * Find latest maintenance record for vehicle
     */
    @Query("select m from MaintenanceRecord m where m.vehicle.id = :vehicleId and m.company.id = :companyId order by m.maintenanceDate desc limit 1")
    Optional<MaintenanceRecord> findLatestByVehicleId(
            @Param("vehicleId") Long vehicleId,
            @Param("companyId") Long companyId
    );

    /**
     * Find maintenance records within date range
     */
    @Query("select m from MaintenanceRecord m where m.vehicle.id = :vehicleId and m.company.id = :companyId and m.maintenanceDate between :startDate and :endDate order by m.maintenanceDate desc")
    List<MaintenanceRecord> findByVehicleIdAndDateRange(
            @Param("vehicleId") Long vehicleId,
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find overdue maintenance records
     */
    @Query("select m from MaintenanceRecord m where m.company.id = :companyId and m.status != :completedStatus and m.maintenanceDate < :currentDate order by m.maintenanceDate asc")
    List<MaintenanceRecord> findOverdueByCompanyId(
            @Param("companyId") Long companyId,
            @Param("currentDate") LocalDate currentDate,
            @Param("completedStatus") MaintenanceRecord.MaintenanceStatus completedStatus
    );

    /**
     * Find all records for a company within date range with pagination
     */
    @Query("select m from MaintenanceRecord m where m.company.id = :companyId and m.maintenanceDate between :startDate and :endDate order by m.maintenanceDate desc")
    Page<MaintenanceRecord> findByCompanyIdAndDateRange(
            @Param("companyId") Long companyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    /**
     * Calculate total maintenance cost for vehicle
     */
    @Query("select sum(m.cost) from MaintenanceRecord m where m.vehicle.id = :vehicleId and m.company.id = :companyId")
    BigDecimal calculateTotalCostByVehicleId(
            @Param("vehicleId") Long vehicleId,
            @Param("companyId") Long companyId
    );

    /**
     * Calculate total maintenance cost for company
     */
    @Query("select sum(m.cost) from MaintenanceRecord m where m.company.id = :companyId")
    BigDecimal calculateTotalCostByCompanyId(@Param("companyId") Long companyId);

    /**
     * Calculate average maintenance cost
     */
    @Query("select avg(m.cost) from MaintenanceRecord m where m.company.id = :companyId")
    BigDecimal calculateAverageCostByCompanyId(@Param("companyId") Long companyId);

    /**
     * Find vehicles needing maintenance
     */
    @Query("select distinct m.vehicle.id from MaintenanceRecord m where m.company.id = :companyId and m.status = 'SCHEDULED' order by m.maintenanceDate asc")
    List<Long> findVehiclesNeedingMaintenance(@Param("companyId") Long companyId);

    /**
     * Check if vehicle exists for company
     */
    @Query("select case when count(m) > 0 then true else false end from MaintenanceRecord m where m.vehicle.id = :vehicleId and m.company.id = :companyId")
    boolean existsByVehicleIdAndCompanyId(
            @Param("vehicleId") Long vehicleId,
            @Param("companyId") Long companyId
    );
}
