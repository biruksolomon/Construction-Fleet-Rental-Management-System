package com.devcast.fleetmanagement.features.driver.repository;

import com.devcast.fleetmanagement.features.driver.model.DriverAssignmentHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DriverAssignmentHistoryRepository extends JpaRepository<DriverAssignmentHistory, Long> {

    /**
     * Find all assignments for a driver
     */
    List<DriverAssignmentHistory> findByDriverId(Long driverId);

    /**
     * Find active assignments for a driver
     */
    List<DriverAssignmentHistory> findByDriverIdAndStatus(Long driverId, DriverAssignmentHistory.AssignmentStatus status);

    /**
     * Find assignment for a specific rental
     */
    @Query("SELECT dah FROM DriverAssignmentHistory dah WHERE dah.rentalContract.id = :rentalId AND dah.status = 'ASSIGNED'")
    List<DriverAssignmentHistory> findActiveAssignmentForRental(@Param("rentalId") Long rentalId);

    /**
     * Find overlapping assignments for a driver
     */
    @Query("SELECT COUNT(dah) FROM DriverAssignmentHistory dah WHERE dah.driver.id = :driverId AND " +
            "dah.status = 'ASSIGNED' AND dah.unassignedAt IS NULL AND " +
            "EXISTS (SELECT 1 FROM RentalContract rc WHERE rc.id = dah.rentalContract.id AND " +
            "NOT (rc.endDate < :startDate OR rc.startDate > :endDate))")
    long countOverlappingAssignments(@Param("driverId") Long driverId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    /**
     * Find assignment history for a driver within date range
     */
    @Query("SELECT dah FROM DriverAssignmentHistory dah WHERE dah.driver.id = :driverId AND " +
            "dah.assignedAt BETWEEN :startDate AND :endDate ORDER BY dah.assignedAt DESC")
    List<DriverAssignmentHistory> findAssignmentsByDateRange(@Param("driverId") Long driverId,
                                                             @Param("startDate") LocalDateTime startDate,
                                                             @Param("endDate") LocalDateTime endDate);

    /**
     * Count assignments for a driver
     */
    long countByDriverId(Long driverId);

    /**
     * Find assignment history for company
     */
    Page<DriverAssignmentHistory> findByCompanyId(Long companyId, Pageable pageable);
}
