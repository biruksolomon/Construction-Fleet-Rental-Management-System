package com.devcast.fleetmanagement.features.auth.model;

import com.devcast.fleetmanagement.features.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * LoginAttempt Entity
 * Tracks login attempts for security purposes
 * Implements account lockout mechanism after configurable failed attempts
 */
@Entity
@Table(name = "login_attempts", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_attempt_time", columnList = "attempt_time"),
        @Index(name = "idx_success", columnList = "success")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Boolean success;

    @Column
    private String ipAddress;

    @Column
    private String userAgent;

    @Column
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime attemptTime;

    @PrePersist
    protected void onCreate() {
        attemptTime = LocalDateTime.now();
    }
}
