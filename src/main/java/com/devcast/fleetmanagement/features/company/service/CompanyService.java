package com.devcast.fleetmanagement.features.company.service;

import com.devcast.fleetmanagement.features.company.dto.*;
import com.devcast.fleetmanagement.features.company.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Company Service Interface (DTO-Based)
 *
 * Defines contract for company-related operations using DTOs to separate
 * API contracts from entity persistence.
 *
 * Design Principles:
 * 1. All requests use *Request DTOs (no client-provided IDs or timestamps)
 * 2. All responses use *Response DTOs (complete information for display)
 * 3. Service never exposes raw entities through API contracts
 * 4. RBAC checks performed in implementation
 *
 * Includes:
 * - Company CRUD operations
 * - Company settings management
 * - Pricing rules management
 * - Client management
 * - Company analytics and statistics
 */
public interface CompanyService {

    // ==================== Company CRUD Operations ====================

    /**
     * Create a new company
     * Request: CompanyCreateRequest (no id, timestamps)
     * Response: CompanyResponse (complete entity representation)
     * RBAC: OWNER only
     */
    CompanyResponse createCompany(CompanyCreateRequest request);

    /**
     * Get company by ID with multi-tenant check
     * RBAC: OWNER (all companies), Others (own company only)
     */
    Optional<CompanyResponse> getCompanyById(Long companyId);

    /**
     * Get company by name
     * RBAC: OWNER only
     */
    Optional<CompanyResponse> getCompanyByName(String name);

    /**
     * Update company details
     * Request: CompanyUpdateRequest (all fields optional, no id/timestamps)
     * Response: CompanyResponse (updated entity representation)
     * RBAC: OWNER, ADMIN (own company only)
     */
    CompanyResponse updateCompany(Long companyId, CompanyUpdateRequest request);

    /**
     * Delete company and cascade all related data
     * RBAC: OWNER only
     * WARNING: Destructive operation - all related data deleted
     */
    void deleteCompany(Long companyId);

    /**
     * Get all companies with pagination
     * RBAC: OWNER (all), Others (own company filtered)
     */
    Page<CompanyResponse> getAllCompanies(Pageable pageable);

    /**
     * Get company status
     * RBAC: Multi-tenant check
     */
    Company.CompanyStatus getCompanyStatus(Long companyId);

    /**
     * Suspend company - prevents all operations
     * RBAC: OWNER only
     *
     * @param companyId the company to suspend
     * @param reason optional reason for suspension (logged for audit)
     */
    void suspendCompany(Long companyId, String reason);

    /**
     * Activate company - resumes normal operations
     * RBAC: OWNER only
     */
    void activateCompany(Long companyId);

    // ==================== Company Settings Management ====================

    /**
     * Save or update company settings
     * Request: CompanySettingRequest (validated setting data)
     * Response: CompanySettingResponse (complete setting representation)
     * RBAC: OWNER, ADMIN (own company only)
     */
    CompanySettingResponse saveSetting(Long companyId, CompanySettingRequest request);

    /**
     * Get all settings for a company
     * RBAC: Multi-tenant check
     */
    List<CompanySettingResponse> getCompanySettings(Long companyId);

    /**
     * Get specific setting value
     * RBAC: Multi-tenant check
     */
    Optional<String> getSettingValue(Long companyId, String key);

    /**
     * Update setting value
     * RBAC: OWNER, ADMIN (own company only)
     *
     * @param companyId the company whose setting to update
     * @param key the setting key
     * @param value the new value
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
     * Request: PricingRuleRequest (validated rule data)
     * Response: PricingRuleResponse (complete rule representation)
     * RBAC: OWNER only
     */
    PricingRuleResponse createPricingRule(Long companyId, PricingRuleRequest request);

    /**
     * Get all pricing rules for company
     * RBAC: Multi-tenant check
     */
    List<PricingRuleResponse> getPricingRules(Long companyId);

    /**
     * Get active pricing rules for company
     * RBAC: Multi-tenant check
     */
    List<PricingRuleResponse> getActivePricingRules(Long companyId);

    /**
     * Get pricing rules by type
     * RBAC: Multi-tenant check
     */
    List<PricingRuleResponse> getPricingRulesByType(Long companyId, String pricingType);

    /**
     * Update pricing rule
     * Request: PricingRuleRequest (validated rule data, no id/timestamps)
     * Response: PricingRuleResponse (updated rule representation)
     * RBAC: OWNER only
     */
    PricingRuleResponse updatePricingRule(Long companyId, Long ruleId, PricingRuleRequest request);

    /**
     * Deactivate pricing rule - rule no longer used for calculations
     * RBAC: OWNER only
     */
    void deactivatePricingRule(Long companyId, Long ruleId);

    /**
     * Activate pricing rule - rule available for use
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
     * Request: ClientRequest (validated client data)
     * Response: ClientResponse (complete client representation)
     * RBAC: OWNER, ADMIN, FLEET_MANAGER (own company)
     */
    ClientResponse createClient(Long companyId, ClientRequest request);

    /**
     * Get client by ID
     * RBAC: Multi-tenant check
     */
    Optional<ClientResponse> getClientById(Long companyId, Long clientId);

    /**
     * Get all clients for company
     * RBAC: Multi-tenant check
     */
    List<ClientResponse> getCompanyClients(Long companyId);

    /**
     * Search clients by name or email
     * RBAC: Multi-tenant check
     */
    List<ClientResponse> searchClients(Long companyId, String searchTerm);

    /**
     * Update client
     * Request: ClientRequest (validated client data, all fields optional)
     * Response: ClientResponse (updated client representation)
     * RBAC: OWNER, ADMIN, FLEET_MANAGER (own company)
     */
    ClientResponse updateClient(Long companyId, Long clientId, ClientRequest request);

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
     *
     * @param companyId the company
     * @param period format: "YYYY-MM" for monthly or "YYYY" for yearly
     */
    RevenueMetrics getRevenueMetrics(Long companyId, String period);

    /**
     * Check if company is active and subscription valid
     * RBAC: None (utility method)
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

    /**
     * Get company features availability map
     * RBAC: Multi-tenant check
     * Returns which features are enabled for the company based on subscription
     */
    java.util.Map<String, Boolean> getCompanyFeatures(Long companyId);
}
