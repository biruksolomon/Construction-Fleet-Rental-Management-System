package com.devcast.fleetmanagement.features.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * PasswordResetCode Entity
 * Stores password reset codes with expiration tracking
 * Each code is valid for 24 hours and single-use
 */
@Entity
@Table(name = "password_reset_codes", indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_code", columnList = "code"),
        @Index(name = "idx_expiry", columnList = "expiry_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    @Column(nullable = false)
    private Boolean used = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Check if reset code is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }

    /**
     * Check if code can be used (not expired and not already used)
     */
    public boolean isValid() {
        return !isExpired() && !used;
    }
}
