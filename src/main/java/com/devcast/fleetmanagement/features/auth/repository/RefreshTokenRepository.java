package com.devcast.fleetmanagement.features.auth.repository;

import com.devcast.fleetmanagement.features.auth.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find refresh token by token string
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all valid tokens for a user
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = false AND rt.rotated = false")
    List<RefreshToken> findValidTokensByUserId(@Param("userId") Long userId);

    /**
     * Find token by user ID and device identifier
     */
    Optional<RefreshToken> findByUserIdAndDeviceIdentifier(Long userId, String deviceIdentifier);

    /**
     * Revoke all user's tokens
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId")
    void revokeAllUserTokens(@Param("userId") Long userId);

    /**
     * Revoke all tokens for specific user/device combination
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId AND rt.deviceIdentifier = :deviceIdentifier")
    void revokeDeviceTokens(@Param("userId") Long userId, @Param("deviceIdentifier") String deviceIdentifier);

    /**
     * Delete expired tokens (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryTime < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Count valid tokens for a user
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.revoked = false AND rt.rotated = false")
    long countValidTokensByUserId(@Param("userId") Long userId);
}
