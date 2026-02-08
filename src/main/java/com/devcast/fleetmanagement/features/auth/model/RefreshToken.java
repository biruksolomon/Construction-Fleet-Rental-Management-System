package com.devcast.fleetmanagement.features.auth.model;

import com.devcast.fleetmanagement.features.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * RefreshToken Entity
 * Stores refresh tokens in database for enhanced security and token management
 * Allows token revocation, rotation tracking, and device isolation
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
        @Index(name = "idx_token", columnList = "token"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_expiry_time", columnList = "expiry_time"),
        @Index(name = "idx_revoked", columnList = "revoked")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, columnDefinition = "TEXT")
    private String token;

    @Column(nullable = false)
    private String deviceIdentifier;

    @Column(nullable = false)
    private String userAgent;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime expiryTime;

    @Column(nullable = false)
    private Boolean revoked = false;

    @Column(nullable = false)
    private Boolean rotated = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_token_id")
    private RefreshToken parentToken;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Check if token is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }

    /**
     * Check if token is valid (not expired, not revoked, not rotated)
     */
    public boolean isValid() {
        return !isExpired() && !revoked && !rotated;
    }

    /**
     * Revoke the token
     */
    public void revoke() {
        this.revoked = true;
    }

    /**
     * Mark token as rotated (used for refresh)
     */
    public void markRotated() {
        this.rotated = true;
    }
}
