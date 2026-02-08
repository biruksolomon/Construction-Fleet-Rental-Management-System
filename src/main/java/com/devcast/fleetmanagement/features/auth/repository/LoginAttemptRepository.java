package com.devcast.fleetmanagement.features.auth.repository;

import com.devcast.fleetmanagement.features.auth.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    /**
     * Count failed login attempts for a user in the last N minutes
     */
    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.email = :email AND la.success = false AND la.attemptTime > :since")
    long countFailedAttemptsSince(@Param("email") String email, @Param("since") LocalDateTime since);

    /**
     * Get failed login attempts for a user
     */
    List<LoginAttempt> findByEmailAndSuccessFalseOrderByAttemptTimeDesc(String email);

    /**
     * Get last successful login for a user
     */
    @Query("SELECT la FROM LoginAttempt la WHERE la.email = :email AND la.success = true ORDER BY la.attemptTime DESC LIMIT 1")
    LoginAttempt findLastSuccessfulLogin(@Param("email") String email);

    /**
     * Count attempts from specific IP in timeframe
     */
    @Query("SELECT COUNT(la) FROM LoginAttempt la WHERE la.ipAddress = :ipAddress AND la.attemptTime > :since")
    long countAttemptsFromIp(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
}
