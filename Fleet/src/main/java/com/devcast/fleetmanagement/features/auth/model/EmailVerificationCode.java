package com.devcast.fleetmanagement.features.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Email Verification Code Entity
 * Stores temporary verification codes sent to user emails
 * Expires after 24 hours
 */
@Entity
@Table(name = "email_verification_codes", indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_code", columnList = "code"),
        @Index(name = "idx_expires_at", columnList = "expires_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean verified;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        expiresAt = LocalDateTime.now().plusHours(24);
        verified = false;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isExpired() && !verified;
    }
}
