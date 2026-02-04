package com.devcast.fleetmanagement.features.company.repository;

import com.devcast.fleetmanagement.features.company.model.CompanySubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing company subscriptions
 */
@Repository
public interface CompanySubscriptionRepository extends JpaRepository<CompanySubscription, Long> {

    /**
     * Find subscription by company ID
     */
    Optional<CompanySubscription> findByCompanyId(Long companyId);

    /**
     * Find subscriptions by status
     */
    List<CompanySubscription> findByStatus(CompanySubscription.SubscriptionStatus status);

    /**
     * Find subscriptions by plan
     */
    List<CompanySubscription> findByPlan(CompanySubscription.SubscriptionPlan plan);

    /**
     * Find subscriptions expiring within date range
     */
    @Query("SELECT cs FROM CompanySubscription cs WHERE cs.expiryDate BETWEEN :startDate AND :endDate")
    List<CompanySubscription> findExpiringSubscriptions(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find expired subscriptions
     */
    @Query("SELECT cs FROM CompanySubscription cs WHERE cs.expiryDate < CURRENT_TIMESTAMP AND cs.status != 'CANCELLED'")
    List<CompanySubscription> findExpiredSubscriptions();

    /**
     * Find active subscriptions
     */
    List<CompanySubscription> findByStatusAndExpiryDateAfter(
            CompanySubscription.SubscriptionStatus status,
            LocalDateTime date
    );

    /**
     * Delete subscription by company ID
     */
    void deleteByCompanyId(Long companyId);
}
