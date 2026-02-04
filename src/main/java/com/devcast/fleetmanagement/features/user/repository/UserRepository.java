package com.devcast.fleetmanagement.features.user.repository;

import com.devcast.fleetmanagement.features.user.model.User;
import com.devcast.fleetmanagement.features.user.model.util.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Basic Queries
    Optional<User> findByEmail(String email);

    List<User> findByCompanyId(Long companyId);

    Page<User> findByCompanyId(Long companyId, Pageable pageable);

    @Query("select count(u) from User u where u.company.id = :companyId")
    Long countByCompanyId(@Param("companyId") Long companyId);

    // Role-based Queries
    List<User> findByCompanyIdAndRole(Long companyId, Role role);

    Page<User> findByCompanyIdAndRole(Long companyId, Role role, Pageable pageable);

    // Status-based Queries
    List<User> findByCompanyIdAndStatus(Long companyId, User.UserStatus status);

    Page<User> findByCompanyIdAndStatus(Long companyId, User.UserStatus status, Pageable pageable);

    // Search Queries
    @Query("select u from User u where u.company.id = :companyId and (lower(u.fullName) like lower(concat('%', :searchTerm, '%')) or lower(u.email) like lower(concat('%', :searchTerm, '%')))")
    Page<User> searchByNameOrEmail(@Param("companyId") Long companyId, @Param("searchTerm") String searchTerm, Pageable pageable);

    // Count Queries
    @Query("select count(u) from User u where u.company.id = :companyId and u.role = :role")
    Long countByCompanyIdAndRole(@Param("companyId") Long companyId, @Param("role") Role role);

    @Query("select count(u) from User u where u.company.id = :companyId and u.status = :status")
    Long countByCompanyIdAndStatus(@Param("companyId") Long companyId, @Param("status") User.UserStatus status);

    // Email exists check
    @Query("select case when count(u) > 0 then true else false end from User u where lower(u.email) = lower(:email) and u.company.id = :companyId")
    boolean existsByEmailAndCompanyId(@Param("email") String email, @Param("companyId") Long companyId);

    // Email and Company check (for updates)
    @Query("select case when count(u) > 0 then true else false end from User u where lower(u.email) = lower(:email) and u.company.id = :companyId and u.id != :userId")
    boolean existsByCompanyIdAndEmailAndIdNot(@Param("companyId") Long companyId, @Param("email") String email, @Param("userId") Long userId);

    // Find by company and email
    @Query("select u from User u where u.company.id = :companyId and lower(u.email) = lower(:email)")
    Optional<User> findByCompanyIdAndEmail(@Param("companyId") Long companyId, @Param("email") String email);

    // Search by company, name or email
    @Query("select u from User u where u.company.id = :companyId and (lower(u.fullName) like lower(concat('%', :searchTerm, '%')) or lower(u.email) like lower(concat('%', :searchTerm, '%')))")
    List<User> searchByCompanyIdAndNameOrEmail(@Param("companyId") Long companyId, @Param("searchTerm") String searchTerm);

    // Complex filter query
    @Query("select u from User u where u.company.id = :companyId " +
            "and (:role is null or u.role = :role) " +
            "and (:status is null or u.status = cast(:status as java.lang.String))")
    List<User> filterByRoleAndStatus(@Param("companyId") Long companyId, @Param("role") Role role, @Param("status") String status);
}
