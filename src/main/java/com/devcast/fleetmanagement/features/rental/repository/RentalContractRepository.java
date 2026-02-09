package com.devcast.fleetmanagement.features.rental.repository;

import com.devcast.fleetmanagement.features.rental.model.RentalContract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentalContractRepository extends JpaRepository<RentalContract, Long> {

    Optional<RentalContract> findByContractNumber(String contractNumber);

    List<RentalContract> findByCompanyId(Long companyId);

    Page<RentalContract> findByCompanyId(Long companyId, Pageable pageable);

    List<RentalContract> findByClientId(Long clientId);

    /**
     * Find rentals by status (updated to use new enum)
     */
    List<RentalContract> findByStatus(RentalContract.RentalStatus status);

    List<RentalContract> findByCompanyIdAndStatus(Long companyId, RentalContract.RentalStatus status);

    Page<RentalContract> findByCompanyIdAndStatus(Long companyId, RentalContract.RentalStatus status, Pageable pageable);

    List<RentalContract> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Check for overlapping rentals for a vehicle
     */
    @Query("SELECT COUNT(rc) FROM RentalContract rc WHERE rc.company.id = :companyId AND " +
            "EXISTS (SELECT 1 FROM RentalVehicle rv WHERE rv.rentalContract = rc AND rv.vehicle.id = :vehicleId) AND " +
            "rc.status IN ('PENDING', 'ACTIVE') AND " +
            "NOT (rc.endDate < :startDate OR rc.startDate > :endDate)")
    long countOverlappingRentals(@Param("companyId") Long companyId,
                                 @Param("vehicleId") Long vehicleId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    /**
     * Check for overlapping rentals for a driver
     */
    @Query("SELECT COUNT(rc) FROM RentalContract rc WHERE rc.company.id = :companyId AND " +
            "EXISTS (SELECT 1 FROM RentalVehicle rv WHERE rv.rentalContract = rc AND rv.driver.id = :driverId) AND " +
            "rc.status IN ('PENDING', 'ACTIVE') AND " +
            "NOT (rc.endDate < :startDate OR rc.startDate > :endDate)")
    long countOverlappingDriverRentals(@Param("companyId") Long companyId,
                                       @Param("driverId") Long driverId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    /**
     * Count active rental contracts for a company
     */
    long countByCompanyIdAndStatus(Long companyId, RentalContract.RentalStatus status);

    /**
     * Count contracts for a company
     */
    long countByCompanyId(Long companyId);

    /**
     * Delete soft-deleted rentals older than N days
     */
    @Modifying
    @Query("DELETE FROM RentalContract rc WHERE rc.deleted = true AND rc.deletedAt < :cutoffDate")
    void deleteDeletedRentalsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Delete soft-deleted rentals by days old
     */
    @Modifying
    default void deleteDeletedRentalsOlderThan(int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        deleteDeletedRentalsOlderThan(cutoffDate);
    }
}
