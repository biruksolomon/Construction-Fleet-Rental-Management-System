package com.devcast.fleetmanagement.features.company.repository;

import com.devcast.fleetmanagement.features.company.model.PricingRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing pricing rules
 * Provides CRUD operations for company-specific pricing configurations
 */
@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {

    /**
     * Find all pricing rules for a company
     */
    List<PricingRule> findByCompanyId(Long companyId);

    /**
     * Find all active pricing rules for a company
     */
    List<PricingRule> findByCompanyIdAndActive(Long companyId, boolean active);

    /**
     * Find pricing rule by ID and company ID (multi-tenant verification)
     */
    Optional<PricingRule> findByIdAndCompanyId(Long id, Long companyId);

    /**
     * Find pricing rules by company ID and applies to type
     */
    List<PricingRule> findByCompanyIdAndAppliesToType(Long companyId, PricingRule.AppliesToType appliesToType);

    /**
     * Find pricing rules by company ID and pricing type
     */
    List<PricingRule> findByCompanyIdAndPricingType(Long companyId, PricingRule.PricingType pricingType);

    /**
     * Find active pricing rules by company and type
     */
    List<PricingRule> findByCompanyIdAndActiveAndPricingType(Long companyId, boolean active, PricingRule.PricingType pricingType);

    /**
     * Count pricing rules for a company
     */
    long countByCompanyId(Long companyId);

    /**
     * Delete all pricing rules for a company
     */
    void deleteByCompanyId(Long companyId);
}
