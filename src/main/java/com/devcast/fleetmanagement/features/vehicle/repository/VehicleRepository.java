package com.devcast.fleetmanagement.features.vehicle.repository;

import com.devcast.fleetmanagement.features.vehicle.model.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * Find vehicle by plate number
     */
    Optional<Vehicle> findByPlateNumber(String plateNumber);

    /**
     * Find vehicle by plate number and company
     */
    @Query("select v from Vehicle v where v.plateNumber = :plateNumber and v.company.id = :companyId")
    Optional<Vehicle> findByPlateNumberAndCompanyId(@Param("plateNumber") String plateNumber, @Param("companyId") Long companyId);

    /**
     * Find all vehicles for a company
     */
    List<Vehicle> findByCompanyId(Long companyId);

    /**
     * Find vehicles by company with pagination
     */
    Page<Vehicle> findByCompanyId(Long companyId, Pageable pageable);

    /**
     * Find vehicles by company and status
     */
    List<Vehicle> findByCompanyIdAndStatus(Long companyId, Vehicle.VehicleStatus status);

    /**
     * Find vehicles by company and status with pagination
     */
    Page<Vehicle> findByCompanyIdAndStatus(Long companyId, Vehicle.VehicleStatus status, Pageable pageable);

    /**
     * Search vehicles by plate number or asset code
     */
    @Query("select v from Vehicle v where v.company.id = :companyId and (v.plateNumber like %:searchTerm% or v.assetCode like %:searchTerm%)")
    Page<Vehicle> searchByCompanyIdAndPlateOrAsset(@Param("companyId") Long companyId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Count vehicles for a company
     */
    long countByCompanyId(Long companyId);

    /**
     * Check if plate number exists in company
     */
    @Query("select case when count(v) > 0 then true else false end from Vehicle v where v.plateNumber = :plateNumber and v.company.id = :companyId")
    boolean existsByPlateNumberAndCompanyId(@Param("plateNumber") String plateNumber, @Param("companyId") Long companyId);

    /**
     * Check if plate number exists excluding current vehicle
     */
    @Query("select case when count(v) > 0 then true else false end from Vehicle v where v.plateNumber = :plateNumber and v.company.id = :companyId and v.id != :vehicleId")
    boolean existsByPlateNumberAndCompanyIdAndIdNot(@Param("plateNumber") String plateNumber, @Param("companyId") Long companyId, @Param("vehicleId") Long vehicleId);

    /**
     * Find available vehicles for company
     */
    @Query("select v from Vehicle v where v.company.id = :companyId and v.status = 'AVAILABLE'")
    List<Vehicle> findAvailableByCompanyId(@Param("companyId") Long companyId);
}
