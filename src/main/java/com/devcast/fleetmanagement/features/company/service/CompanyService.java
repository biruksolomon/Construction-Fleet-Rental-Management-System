package com.devcast.fleetmanagement.features.company.service;

import com.devcast.fleetmanagement.features.company.dto.CompanyStatistics;
import com.devcast.fleetmanagement.features.company.dto.CompanySubscriptionInfo;
import com.devcast.fleetmanagement.features.company.dto.RevenueMetrics;
import com.devcast.fleetmanagement.features.company.model.Client;
import com.devcast.fleetmanagement.features.company.model.Company;
import com.devcast.fleetmanagement.features.company.model.CompanySetting;
import com.devcast.fleetmanagement.features.company.model.PricingRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Company Service Interface
 * Defines contract for company-related operations including:
 * - Company CRUD operations
 * - Company settings management
 * - Pricing rules management
 * - Client management
 * - Company analytics (statistics, revenue metrics, subscription info)
 *
 * All operations follow RBAC rules defined in implementation
 */
public interface CompanyService {

    // ==================== Company CRUD Operations ====================

    /**
     * Create a new company
     * RBAC: OWNER only
     */
    Company createCompany(Company company);

    /**
     * Get company by ID with multi-tenant check
     * RBAC: OWNER (all companies), Others (own company only)
     */
    Optional<Company> getCompanyById(Long companyId);

    /**
     * Get company by name
     * RBAC: OWNER only
     */
    Optional<Company> getCompanyByName(String name);

    /**
     * Update company details
     * RBAC: OWNER, ADMIN (own company only)
     */
    Company updateCompany(Long companyId, Company companyDetails);

    /**
     * Delete company and cascade all related data
     * RBAC: OWNER only
     * WARNING: Destructive operation
     */
    void deleteCompany(Long companyId);

    /**
     * Get all companies with pagination
     * RBAC: OWNER (all), Others (own company filtered)
     */
    Page<Company> getAllCompanies(Pageable pageable);

    /**
     * Get company status
     * RBAC: Multi-tenant check
     */
    Company.CompanyStatus getCompanyStatus(Long companyId);

    /**
     * Suspend company
     * RBAC: OWNER only
     */
    void suspendCompany(Long companyId, String reason);

    /**
     * Activate company
     * RBAC: OWNER only
     */
    void activateCompany(Long companyId);

    // ==================== Company Settings Management ====================

    /**
     * Save or update company settings
     * RBAC: OWNER, ADMIN (own company only)
     */
    CompanySetting saveSetting(Long companyId, String key, String value, CompanySetting.DataType dataType);

    /**
     * Get all settings for a company
     * RBAC: Multi-tenant check
     */
    List<CompanySetting> getCompanySettings(Long companyId);

    /**
     * Get specific setting value
     * RBAC: Multi-tenant check
     */
    Optional<String> getSettingValue(Long companyId, String key);

    /**
     * Update setting value
     * RBAC: OWNER, ADMIN (own company only)
     */
    void updateSetting(Long companyId, String key, String value);

    /**
     * Delete setting
     * RBAC: OWNER, ADMIN (own company only)
     */
    void deleteSetting(Long companyId, String key);

    // ==================== Pricing Rules Management ====================

    /**
     * Create pricing rule for company
     * RBAC: OWNER only
     */
    PricingRule createPricingRule(Long companyId, PricingRule rule);

    /**
     * Get all pricing rules for company
     * RBAC: Multi-tenant check
     */
    List<PricingRule> getPricingRules(Long companyId);

    /**
     * Get active pricing rules for company
     * RBAC: Multi-tenant check
     */
    List<PricingRule> getActivePricingRules(Long companyId);

    /**
     * Get pricing rules by type
     * RBAC: Multi-tenant check
     */
    List<PricingRule> getPricingRulesByType(Long companyId, PricingRule.PricingType type);

    /**
     * Update pricing rule
     * RBAC: OWNER only
     */
    PricingRule updatePricingRule(Long companyId, Long ruleId, PricingRule rule);

    /**
     * Deactivate pricing rule
     * RBAC: OWNER only
     */
    void deactivatePricingRule(Long companyId, Long ruleId);

    /**
     * Activate pricing rule
     * RBAC: OWNER only
     */
    void activatePricingRule(Long companyId, Long ruleId);

    /**
     * Delete pricing rule
     * RBAC: OWNER only
     */
    void deletePricingRule(Long companyId, Long ruleId);

    // ==================== Client Management ====================

    /**
     * Create client for company
     * RBAC: OWNER, ADMIN, FLEET_MANAGER (own company)
     */
    Client createClient(Long companyId, Client client);

    /**
     * Get client by ID
     * RBAC: Multi-tenant check
     */
    Optional<Client> getClientById(Long companyId, Long clientId);

    /**
     * Get all clients for company
     * RBAC: Multi-tenant check
     */
    List<Client> getCompanyClients(Long companyId);

    /**
     * Search clients by name
     * RBAC: Multi-tenant check
     */
    List<Client> searchClients(Long companyId, String searchTerm);

    /**
     * Update client
     * RBAC: OWNER, ADMIN, FLEET_MANAGER (own company)
     */
    Client updateClient(Long companyId, Long clientId, Client clientDetails);

    /**
     * Delete client
     * RBAC: OWNER, ADMIN (own company only)
     */
    void deleteClient(Long companyId, Long clientId);

    // ==================== Company Analytics ====================

    /**
     * Get company statistics (vehicles, drivers, clients, rentals, revenue)
     * RBAC: Multi-tenant check
     */
    CompanyStatistics getCompanyStatistics(Long companyId);

    /**
     * Get company subscription info
     * RBAC: Multi-tenant check
     */
    CompanySubscriptionInfo getSubscriptionInfo(Long companyId);

    /**
     * Get revenue metrics for period
     * RBAC: OWNER, ADMIN, ACCOUNTANT (own company)
     */
    RevenueMetrics getRevenueMetrics(Long companyId, String period);

    /**
     * Check if company is active and subscription valid
     * RBAC: None (utility method, but can check ownership after)
     */
    boolean isCompanyActive(Long companyId);

    /**
     * Get company vehicle count
     * RBAC: Multi-tenant check
     */
    long getVehicleCount(Long companyId);

    /**
     * Get company driver count
     * RBAC: Multi-tenant check
     */
    long getDriverCount(Long companyId);

    /**
     * Get company user count
     * RBAC: Multi-tenant check
     */
    long getUserCount(Long companyId);

    /**
     * Get active rental count
     * RBAC: Multi-tenant check
     */
    long getActiveRentalCount(Long companyId);
}
