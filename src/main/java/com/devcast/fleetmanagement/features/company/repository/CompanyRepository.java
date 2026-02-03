package com.devcast.fleetmanagement.features.company.repository;

import com.devcast.fleetmanagement.features.company.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing companies
 * Provides CRUD operations for company entities
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * Find company by name
     */
    Optional<Company> findByName(String name);

    /**
     * Find companies by status
     */
    List<Company> findByStatus(Company.CompanyStatus status);

    /**
     * Find companies by business type
     */
    List<Company> findByBusinessType(Company.BusinessType businessType);

    /**
     * Check if company exists by name
     */
    boolean existsByName(String name);

    /**
     * Count companies by status
     */
    long countByStatus(Company.CompanyStatus status);
}
