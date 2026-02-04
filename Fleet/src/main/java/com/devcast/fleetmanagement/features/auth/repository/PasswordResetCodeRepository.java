package com.devcast.fleetmanagement.features.auth.repository;

import com.devcast.fleetmanagement.features.auth.model.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * PasswordResetCode Repository
 * Handles database operations for password reset codes
 */
@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {

    /**
     * Find reset code by email and code
     */
    Optional<PasswordResetCode> findByEmailAndCode(String email, String code);

    /**
     * Find reset code by code only
     */
    Optional<PasswordResetCode> findByCode(String code);

    /**
     * Find most recent reset code for email
     */
    Optional<PasswordResetCode> findFirstByEmailOrderByCreatedAtDesc(String email);
}
