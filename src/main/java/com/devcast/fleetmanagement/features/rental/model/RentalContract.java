package com.devcast.fleetmanagement.features.rental.model;

import com.devcast.fleetmanagement.features.company.model.Client;
import com.devcast.fleetmanagement.features.company.model.Company;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rental_contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Schema(hidden = true)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @Schema(hidden = true)
    private Client client;

    @Column(nullable = false, length = 50)
    private String contractNumber;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private Boolean includeDriver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PricingModel pricingModel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalStatus status;

    @Column
    private LocalDateTime actualEndDate;

    @Column
    private String cancellationReason;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column
    private LocalDateTime deletedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @OneToMany(mappedBy = "rentalContract", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("rentalContract")
    @Schema(hidden = true)
    @Builder.Default
    private List<RentalVehicle> rentalVehicles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        deleted = false;
        if (status == null) {
            status = RentalStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Validate date range
     */
    public void validateDates() {
        if (endDate.isBefore(startDate) || endDate.equals(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
    }

    /**
     * Check if rental is overdue
     */
    public boolean isOverdue() {
        return status == RentalStatus.ACTIVE && LocalDate.now().isAfter(endDate);
    }

    /**
     * Mark as overdue
     */
    public void markOverdue() {
        if (status == RentalStatus.ACTIVE) {
            this.status = RentalStatus.OVERDUE;
        }
    }

    /**
     * Soft delete the rental
     */
    public void softDelete(String reason) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    public enum PricingModel {
        HOURLY,
        DAILY,
        PROJECT
    }

    /**
     * Rental Status Lifecycle
     * PENDING -> ACTIVE -> (COMPLETED | OVERDUE | CANCELLED)
     */
    public enum RentalStatus {
        PENDING,      // Initial state, waiting to start
        ACTIVE,       // Currently ongoing
        COMPLETED,    // Finished on time
        OVERDUE,      // Exceeded end date
        CANCELLED     // Cancelled before completion
    }

    /**
     * Validate status transition
     * Enforce valid state transitions only
     */
    public boolean canTransitionTo(RentalStatus newStatus) {
        if (this.status == newStatus) {
            return true; // Same status is allowed
        }

        switch (this.status) {
            case PENDING:
                return newStatus == RentalStatus.ACTIVE || newStatus == RentalStatus.CANCELLED;
            case ACTIVE:
                return newStatus == RentalStatus.COMPLETED ||
                        newStatus == RentalStatus.OVERDUE ||
                        newStatus == RentalStatus.CANCELLED;
            case COMPLETED:
            case CANCELLED:
            case OVERDUE:
                return false; // Terminal states cannot transition
            default:
                return false;
        }
    }

    /**
     *: Transition to new status with validation
     */
    public void transitionTo(RentalStatus newStatus) {
        if (!canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    String.format("Invalid status transition from %s to %s", this.status, newStatus)
            );
        }
        this.status = newStatus;
    }
}
