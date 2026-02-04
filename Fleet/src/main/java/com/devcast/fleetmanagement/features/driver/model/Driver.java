package com.devcast.fleetmanagement.features.driver.model;

import com.devcast.fleetmanagement.features.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "drivers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private com.devcast.fleetmanagement.features.company.model.Company company;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String licenseNumber;

    @Column(nullable = false)
    private LocalDate licenseExpiry;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyWage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status;

    public enum EmploymentType {
        FULL_TIME,
        PART_TIME,
        CONTRACT
    }

    public enum DriverStatus {
        ACTIVE,
        SUSPENDED
    }
}
