package com.devcast.fleetmanagement.features.driver.model;

import com.devcast.fleetmanagement.features.company.model.Company;
import com.devcast.fleetmanagement.features.rental.model.RentalContract;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Driver Assignment History
 * Tracks all driver assignments to rentals for auditing and overlap detection
 */
@Entity
@Table(name = "driver_assignment_history", indexes = {
        @Index(name = "idx_driver_id", columnList = "driver_id"),
        @Index(name = "idx_rental_contract_id", columnList = "rental_contract_id"),
        @Index(name = "idx_company_id", columnList = "company_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverAssignmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_contract_id", nullable = false)
    private RentalContract rentalContract;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @Column
    private LocalDateTime unassignedAt;

    @Column
    private String reason;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
    }

    /**
     * Mark assignment as unassigned
     */
    public void unassign(String reason) {
        this.status = AssignmentStatus.UNASSIGNED;
        this.unassignedAt = LocalDateTime.now();
        this.reason = reason;
    }

    /**
     * Check if assignment is currently active
     */
    public boolean isActive() {
        return status == AssignmentStatus.ASSIGNED && unassignedAt == null;
    }

    public enum AssignmentStatus {
        ASSIGNED,
        UNASSIGNED,
        CANCELLED
    }
}
