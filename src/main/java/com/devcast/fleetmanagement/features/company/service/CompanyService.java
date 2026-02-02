package com.devcast.fleetmanagement.features.company.service;

import com.devcast.fleetmanagement.features.company.dto.*;
import com.devcast.fleetmanagement.features.company.model.Company;
import com.devcast.fleetmanagement.features.company.model.CompanySetting;
import com.devcast.fleetmanagement.features.company.model.PricingRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

/**
 * Company Service Interface
 * Handles all company-related business logic including settings and pricing rules
 */
public interface CompanyService {

    // ==================== Company CRUD Operations ====================

    /**
     * Create a new company
     */
    Company createCompany(Company company);

    /**
     * Get company by ID
     */
    Optional<Company> getCompanyById(Long companyId);

    /**
     * Get company by email
     */
    Optional<Company> getCompanyByEmail(String email);

    /**
     * Update company details
     */
    Company updateCompany(Long companyId, Company company);

    /**
     * Delete company and cascade all related data
     */
    void deleteCompany(Long companyId);

    /**
     * Get all companies with pagination
     */
    Page<Company> getAllCompanies(Pageable pageable);

    /**
     * Get company statistics
     */
    CompanyStatistics getCompanyStatistics(Long companyId);

    // ==================== Company Settings Management ====================

    /**
     * Create or update company settings
     */
    CompanySetting saveCompanySettings(Long companyId, CompanySetting settings);

    /**
     * Get settings for a company
     */
    Optional<CompanySetting> getCompanySettings(Long companyId);

    /**
     * Update specific setting
     */
    CompanySetting updateSetting(Long companyId, String key, String value);

    /**
     * Get setting value by key
     */
    Optional<String> getSettingValue(Long companyId, String key);

    // ==================== Pricing Rules Management ====================

    /**
     * Create pricing rule for company
     */
    PricingRule createPricingRule(Long companyId, PricingRule rule);

    /**
     * Get all pricing rules for company
     */
    List<PricingRule> getPricingRules(Long companyId);

    /**
     * Get active pricing rule
     */
    Optional<PricingRule> getActivePricingRule(Long companyId);

    /**
     * Update pricing rule
     */
    PricingRule updatePricingRule(Long ruleId, PricingRule rule);

    /**
     * Delete pricing rule
     */
    void deletePricingRule(Long ruleId);

    /**
     * Activate pricing rule
     */
    void activatePricingRule(Long ruleId);

    // ==================== Company Analysis ====================

    /**
     * Get company subscription info
     */
    CompanySubscriptionInfo getSubscriptionInfo(Long companyId);

    /**
     * Get company revenue metrics
     */
    RevenueMetrics getRevenueMetrics(Long companyId, String period);

    /**
     * Check if company is active and not expired
     */
    boolean isCompanyActive(Long companyId);

    /**
     * Renew company subscription
     */
    void renewSubscription(Long companyId, int months);

    // Data Transfer Objects





}
