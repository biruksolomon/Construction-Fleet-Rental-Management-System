package com.devcast.fleetmanagement.features.fuel.repository;

import com.devcast.fleetmanagement.features.fuel.model.FuelAnalysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Fuel Analysis Repository
 * Handles fuel consumption analysis and variance tracking
 */
@Repository
public interface FuelAnalysisRepository extends JpaRepository<FuelAnalysis, Long> {

    /**
     * Find all fuel analysis records for a vehicle with pagination
     */
    Page<FuelAnalysis> findByVehicleId(Long vehicleId, Pageable pageable);

    /**
     * Find fuel analysis records with high variance (potential theft/anomaly)
     */
    @Query("select fa from FuelAnalysis fa where fa.vehicle.id = :vehicleId and fa.variance > :threshold order by fa.variance desc")
    List<FuelAnalysis> findHighVarianceByVehicleId(
            @Param("vehicleId") Long vehicleId,
            @Param("threshold") BigDecimal threshold
    );

    /**
     * Find all unresolved alerts (alert_generated = true)
     */
    @Query("select fa from FuelAnalysis fa where fa.vehicle.id = :vehicleId and fa.alertGenerated = true order by fa.id desc")
    List<FuelAnalysis> findUnresolvedAlerts(@Param("vehicleId") Long vehicleId);

    /**
     * Calculate average variance for a vehicle
     */
    @Query("select avg(fa.variance) from FuelAnalysis fa where fa.vehicle.id = :vehicleId")
    Optional<BigDecimal> calculateAverageVariance(@Param("vehicleId") Long vehicleId);

    /**
     * Find maximum variance for a vehicle
     */
    @Query("select max(fa.variance) from FuelAnalysis fa where fa.vehicle.id = :vehicleId")
    Optional<BigDecimal> findMaxVariance(@Param("vehicleId") Long vehicleId);

    /**
     * Count fuel analysis records with alerts
     */
    @Query("select count(fa) from FuelAnalysis fa where fa.vehicle.id = :vehicleId and fa.alertGenerated = true")
    long countAlertsForVehicle(@Param("vehicleId") Long vehicleId);

    /**
     * Find fuel analysis records with expected consumption data
     */
    @Query("select fa from FuelAnalysis fa where fa.vehicle.id = :vehicleId and fa.expectedConsumption is not null order by fa.id desc")
    Page<FuelAnalysis> findWithExpectedConsumption(@Param("vehicleId") Long vehicleId, Pageable pageable);

    /**
     * Find fuel analysis records where actual consumption exceeds expected
     */
    @Query("select fa from FuelAnalysis fa where fa.vehicle.id = :vehicleId and fa.actualConsumption > fa.expectedConsumption order by fa.variance desc")
    List<FuelAnalysis> findExcessiveConsumption(@Param("vehicleId") Long vehicleId);

    /**
     * Mark alerts as resolved
     */
    @Query("update FuelAnalysis fa set fa.alertGenerated = false where fa.vehicle.id = :vehicleId and fa.alertGenerated = true")
    void resolveAlerts(@Param("vehicleId") Long vehicleId);

    /**
     * Delete fuel analysis records for a vehicle
     */
    @Query("delete from FuelAnalysis fa where fa.vehicle.id = :vehicleId")
    void deleteByVehicleId(@Param("vehicleId") Long vehicleId);
}
