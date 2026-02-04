package com.devcast.fleetmanagement.features.auth.repository;

import com.devcast.fleetmanagement.features.auth.model.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing email verification codes
 */
@Repository
public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long> {

    /**
     * Find verification code by email and code
     */
    Optional<EmailVerificationCode> findByEmailAndCode(String email, String code);

    /**
     * Find most recent verification code for email
     */
    Optional<EmailVerificationCode> findFirstByEmailOrderByCreatedAtDesc(String email);

    /**
     * Delete expired codes
     */
    @Query("DELETE FROM EmailVerificationCode e WHERE e.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpiredCodes();
}
