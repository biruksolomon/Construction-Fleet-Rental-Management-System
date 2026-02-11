package com.devcast.fleetmanagement.features.company.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Company Subscription Model
 * Manages subscription plans, features, and license limits per company
 */
@Entity
@Table(name = "company_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanySubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Schema(hidden = true)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyCost;

    @Column(nullable = false)
    private Long maxVehicles;

    @Column(nullable = false)
    private Long maxDrivers;

    @Column(nullable = false)
    private Long maxUsers;

    @Column(nullable = false)
    private Boolean autoRenewal;

    @Column(nullable = false)
    private Boolean gpsTrackingEnabled;

    @Column(nullable = false)
    private Boolean fuelTrackingEnabled;

    @Column(nullable = false)
    private Boolean maintenanceTrackingEnabled;

    @Column(nullable = false)
    private Boolean payrollIntegrationEnabled;

    @Column(nullable = false)
    private Boolean mobileAppEnabled;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum SubscriptionPlan {
        BASIC,
        STANDARD,
        PREMIUM,
        ENTERPRISE
    }

    public enum SubscriptionStatus {
        ACTIVE,
        SUSPENDED,
        EXPIRED,
        CANCELLED
    }

    /**
     * Check if subscription is valid (active and not expired)
     */
    public boolean isValid() {
        return status == SubscriptionStatus.ACTIVE &&
                expiryDate.isAfter(LocalDateTime.now());
    }

    /**
     * Check if subscription will expire soon (within 7 days)
     */
    public boolean isExpiringsoon() {
        LocalDateTime sevenDaysFromNow = LocalDateTime.now().plusDays(7);
        return expiryDate.isBefore(sevenDaysFromNow) &&
                expiryDate.isAfter(LocalDateTime.now());
    }

    /**
     * Check if subscription is expired
     */
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }
}
