package com.devcast.fleetmanagement.features.driver.repository;

import com.devcast.fleetmanagement.features.driver.model.Driver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByLicenseNumber(String licenseNumber);

    Optional<Driver> findByUserId(Long userId);

    List<Driver> findByCompanyId(Long companyId);

    Page<Driver> findByCompanyId(Long companyId, Pageable pageable);

    List<Driver> findByCompanyIdAndStatus(Long companyId, Driver.DriverStatus status);

    Page<Driver> findByCompanyIdAndStatus(Long companyId, Driver.DriverStatus status, Pageable pageable);

    /**
     * Count drivers for a company
     */
    long countByCompanyId(Long companyId);

    /**
     * Find drivers with expiring licenses
     */
    @Query("""
    SELECT d FROM Driver d
    WHERE d.company.id = :companyId
    AND d.licenseExpiry BETWEEN CURRENT_DATE AND :expiryDate
""")
    List<Driver> findDriversWithExpiringLicenses(
            @Param("companyId") Long companyId,
            @Param("expiryDate") LocalDate expiryDate
    );


    /**
     * Find drivers with expiring insurance
     */
    @Query("""
    SELECT d FROM Driver d
    WHERE d.company.id = :companyId
    AND d.insuranceExpiry BETWEEN CURRENT_DATE AND :expiryDate
""")
    List<Driver> findDriversWithExpiringInsurance(
            @Param("companyId") Long companyId,
            @Param("expiryDate") LocalDate expiryDate
    );

    /**
     * Find active drivers for a company
     */
    @Query("SELECT d FROM Driver d WHERE d.company.id = :companyId AND d.status = 'ACTIVE'")
    List<Driver> findActiveDrivers(@Param("companyId") Long companyId);

    /**
     * Find drivers by company with pagination
     */
    Page<Driver> findByCompanyIdAndLicenseTypeLike(Long companyId, String licenseType, Pageable pageable);

    /**
     * Search drivers by name or license
     */
    @Query("SELECT d FROM Driver d WHERE d.company.id = :companyId AND (d.user.fullName LIKE %:searchTerm% OR d.licenseNumber LIKE %:searchTerm%)")
    Page<Driver> searchDrivers(@Param("companyId") Long companyId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find drivers with invalid licenses
     */
    @Query("SELECT d FROM Driver d WHERE d.company.id = :companyId AND d.licenseExpiry < CURRENT_DATE")
    List<Driver> findDriversWithInvalidLicenses(@Param("companyId") Long companyId);



    /**
     * Check if driver is available (not suspended, not on leave)
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Driver d " +
            "WHERE d.id = :driverId  AND d.status = 'ACTIVE'")
    boolean isDriverAvailable(@Param("driverId") Long driverId);

    /**
     * Check if driver is suspended
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Driver d " +
            "WHERE d.id = :driverId AND d.status = 'SUSPENDED'")
    boolean isDriverSuspended(@Param("driverId") Long driverId);
}
