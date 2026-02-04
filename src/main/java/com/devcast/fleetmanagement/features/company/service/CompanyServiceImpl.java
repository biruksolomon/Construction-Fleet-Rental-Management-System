package com.devcast.fleetmanagement.features.company.service;

import com.devcast.fleetmanagement.features.company.dto.CompanyStatistics;
import com.devcast.fleetmanagement.features.company.dto.CompanySubscriptionInfo;
import com.devcast.fleetmanagement.features.company.dto.RevenueMetrics;
import com.devcast.fleetmanagement.features.company.model.Client;
import com.devcast.fleetmanagement.features.company.model.Company;
import com.devcast.fleetmanagement.features.company.model.CompanySetting;
import com.devcast.fleetmanagement.features.company.model.CompanySubscription;
import com.devcast.fleetmanagement.features.company.model.PricingRule;
import com.devcast.fleetmanagement.features.company.repository.ClientRepository;
import com.devcast.fleetmanagement.features.company.repository.CompanyRepository;
import com.devcast.fleetmanagement.features.company.repository.CompanySettingRepository;
import com.devcast.fleetmanagement.features.company.repository.CompanySubscriptionRepository;
import com.devcast.fleetmanagement.features.company.repository.PricingRuleRepository;
import com.devcast.fleetmanagement.features.driver.repository.DriverRepository;
import com.devcast.fleetmanagement.features.rental.repository.RentalContractRepository;
import com.devcast.fleetmanagement.features.rental.model.RentalContract;
import com.devcast.fleetmanagement.features.user.model.util.Permission;
import com.devcast.fleetmanagement.features.user.repository.UserRepository;
import com.devcast.fleetmanagement.features.vehicle.repository.VehicleRepository;
import com.devcast.fleetmanagement.security.annotation.RequirePermission;
import com.devcast.fleetmanagement.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Company Service Implementation
 * Handles all company-related business logic with RBAC enforcement
 *
 * Security Model:
 * - OWNER: Full access to all companies
 * - ADMIN: Access to own company only
 * - FLEET_MANAGER: Read-only access to own company
 * - ACCOUNTANT: Financial data access to own company
 * - DRIVER: No direct company access
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final ClientRepository clientRepository;
    private final CompanySettingRepository companySettingRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final CompanySubscriptionRepository companySubscriptionRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final RentalContractRepository rentalContractRepository;

    // ==================== Company CRUD Operations ====================

    @Override
    @RequirePermission(Permission.CREATE_COMPANY)
    public Company createCompany(Company company) {
        log.info("Creating new company: {}", company.getName());

        validateCompanyInput(company);

        company.setStatus(Company.CompanyStatus.ACTIVE);
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());

        Company saved = companyRepository.save(company);
        log.info("Company created successfully with ID: {}", saved.getId());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Company> getCompanyById(Long companyId) {
        verifyCompanyAccess(companyId);
        return companyRepository.findById(companyId);
    }

    @Override
    @RequirePermission(Permission.CREATE_COMPANY)
    @Transactional(readOnly = true)
    public Optional<Company> getCompanyByName(String name) {
        return companyRepository.findByName(name);
    }

    @Override
    @RequirePermission(Permission.UPDATE_COMPANY)
    public Company updateCompany(Long companyId, Company companyDetails) {
        verifyCompanyAccess(companyId);

        Company existing = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        // Update allowed fields
        if (companyDetails.getName() != null && !companyDetails.getName().isEmpty()) {
            existing.setName(companyDetails.getName());
        }
        if (companyDetails.getBusinessType() != null) {
            existing.setBusinessType(companyDetails.getBusinessType());
        }
        if (companyDetails.getCurrency() != null) {
            existing.setCurrency(companyDetails.getCurrency());
        }
        if (companyDetails.getTimezone() != null) {
            existing.setTimezone(companyDetails.getTimezone());
        }
        if (companyDetails.getLanguage() != null) {
            existing.setLanguage(companyDetails.getLanguage());
        }

        existing.setUpdatedAt(LocalDateTime.now());

        log.info("Company updated: {}", companyId);
        return companyRepository.save(existing);
    }

    @Override
    @RequirePermission(Permission.DELETE_COMPANY)
    public void deleteCompany(Long companyId) {
        verifyCompanyAccess(companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        log.warn("Deleting company: {} - All related data will be cascade deleted", companyId);
        companyRepository.delete(company);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Company> getAllCompanies(Pageable pageable) {
        if (SecurityUtils.hasRole(com.devcast.fleetmanagement.features.user.model.util.Role.OWNER)) {
            return companyRepository.findAll(pageable);
        } else {
            // Non-owner users can only see their own company
            Long companyId = SecurityUtils.getCurrentCompanyId();
            Optional<Company> company = companyRepository.findById(companyId);

            if (company.isPresent()) {
                return new PageImpl<>(List.of(company.get()), pageable, 1);
            }

            return new PageImpl<>(List.of());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Company.CompanyStatus getCompanyStatus(Long companyId) {
        verifyCompanyAccess(companyId);

        return companyRepository.findById(companyId)
                .map(Company::getStatus)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));
    }

    @Override
    @RequirePermission(Permission.UPDATE_COMPANY)
    public void suspendCompany(Long companyId, String reason) {
        verifyCompanyAccess(companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        company.setStatus(Company.CompanyStatus.SUSPENDED);
        company.setUpdatedAt(LocalDateTime.now());
        companyRepository.save(company);

        log.warn("Company suspended: {} - Reason: {}", companyId, reason);
    }

    @Override
    @RequirePermission(Permission.UPDATE_COMPANY)
    public void activateCompany(Long companyId) {
        verifyCompanyAccess(companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        company.setStatus(Company.CompanyStatus.ACTIVE);
        company.setUpdatedAt(LocalDateTime.now());
        companyRepository.save(company);

        log.info("Company activated: {}", companyId);
    }

    // ==================== Company Settings Management ====================

    @Override
    @RequirePermission(Permission.MANAGE_COMPANY_SETTINGS)
    public CompanySetting saveSetting(Long companyId, String key, String value, CompanySetting.DataType dataType) {
        verifyCompanyAccess(companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        // Check if setting already exists and update it
        Optional<CompanySetting> existing = companySettingRepository.findByCompanyIdAndSettingKey(companyId, key);

        CompanySetting setting;
        if (existing.isPresent()) {
            setting = existing.get();
            setting.setSettingValue(value);
            setting.setDataType(dataType);
            log.info("Setting updated for company {}: {} = {}", companyId, key, value);
        } else {
            setting = CompanySetting.builder()
                    .company(company)
                    .settingKey(key)
                    .settingValue(value)
                    .dataType(dataType)
                    .build();
            log.info("Setting created for company {}: {} = {}", companyId, key, value);
        }

        return companySettingRepository.save(setting);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanySetting> getCompanySettings(Long companyId) {
        verifyCompanyAccess(companyId);
        return companySettingRepository.findByCompanyId(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> getSettingValue(Long companyId, String key) {
        verifyCompanyAccess(companyId);
        return companySettingRepository.findByCompanyIdAndSettingKey(companyId, key)
                .map(CompanySetting::getSettingValue);
    }

    @Override
    @RequirePermission(Permission.MANAGE_COMPANY_SETTINGS)
    public void updateSetting(Long companyId, String key, String value) {
        verifyCompanyAccess(companyId);

        CompanySetting setting = companySettingRepository.findByCompanyIdAndSettingKey(companyId, key)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found: " + key));

        setting.setSettingValue(value);
        companySettingRepository.save(setting);
        log.info("Setting updated for company {}: {} = {}", companyId, key, value);
    }

    @Override
    @RequirePermission(Permission.MANAGE_COMPANY_SETTINGS)
    public void deleteSetting(Long companyId, String key) {
        verifyCompanyAccess(companyId);

        if (!companySettingRepository.existsByCompanyIdAndSettingKey(companyId, key)) {
            throw new IllegalArgumentException("Setting not found: " + key);
        }

        companySettingRepository.deleteByCompanyIdAndSettingKey(companyId, key);
        log.info("Setting deleted for company {}: {}", companyId, key);
    }

    // ==================== Pricing Rules Management ====================

    @Override
    @RequirePermission(Permission.MANAGE_PRICING_RULES)
    public PricingRule createPricingRule(Long companyId, PricingRule rule) {
        verifyCompanyAccess(companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        rule.setCompany(company);
        rule.setActive(true);

        log.info("Pricing rule created for company: {} - Type: {}", companyId, rule.getPricingType());
        return pricingRuleRepository.save(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricingRule> getPricingRules(Long companyId) {
        verifyCompanyAccess(companyId);
        return pricingRuleRepository.findByCompanyId(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricingRule> getActivePricingRules(Long companyId) {
        verifyCompanyAccess(companyId);
        return pricingRuleRepository.findByCompanyIdAndActive(companyId, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricingRule> getPricingRulesByType(Long companyId, PricingRule.PricingType type) {
        verifyCompanyAccess(companyId);
        return pricingRuleRepository.findByCompanyIdAndPricingType(companyId, type);
    }

    @Override
    @RequirePermission(Permission.MANAGE_PRICING_RULES)
    public PricingRule updatePricingRule(Long companyId, Long ruleId, PricingRule rule) {
        verifyCompanyAccess(companyId);

        PricingRule existing = pricingRuleRepository.findByIdAndCompanyId(ruleId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Pricing rule not found or access denied"));

        if (rule.getRate() != null) {
            existing.setRate(rule.getRate());
        }
        if (rule.getOvertimeRate() != null) {
            existing.setOvertimeRate(rule.getOvertimeRate());
        }
        if (rule.getCurrency() != null) {
            existing.setCurrency(rule.getCurrency());
        }
        if (rule.getPricingType() != null) {
            existing.setPricingType(rule.getPricingType());
        }
        if (rule.getAppliesToType() != null) {
            existing.setAppliesToType(rule.getAppliesToType());
        }

        log.info("Pricing rule updated: {} for company: {}", ruleId, companyId);
        return pricingRuleRepository.save(existing);
    }

    @Override
    @RequirePermission(Permission.MANAGE_PRICING_RULES)
    public void deactivatePricingRule(Long companyId, Long ruleId) {
        verifyCompanyAccess(companyId);

        PricingRule rule = pricingRuleRepository.findByIdAndCompanyId(ruleId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Pricing rule not found or access denied"));

        rule.setActive(false);
        pricingRuleRepository.save(rule);
        log.info("Pricing rule deactivated: {} for company: {}", ruleId, companyId);
    }

    @Override
    @RequirePermission(Permission.MANAGE_PRICING_RULES)
    public void activatePricingRule(Long companyId, Long ruleId) {
        verifyCompanyAccess(companyId);

        PricingRule rule = pricingRuleRepository.findByIdAndCompanyId(ruleId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Pricing rule not found or access denied"));

        rule.setActive(true);
        pricingRuleRepository.save(rule);
        log.info("Pricing rule activated: {} for company: {}", ruleId, companyId);
    }

    @Override
    @RequirePermission(Permission.MANAGE_PRICING_RULES)
    public void deletePricingRule(Long companyId, Long ruleId) {
        verifyCompanyAccess(companyId);

        PricingRule rule = pricingRuleRepository.findByIdAndCompanyId(ruleId, companyId)
                .orElseThrow(() -> new IllegalArgumentException("Pricing rule not found or access denied"));

        pricingRuleRepository.delete(rule);
        log.info("Pricing rule deleted: {} for company: {}", ruleId, companyId);
    }

    // ==================== Client Management ====================

    @Override
    @RequirePermission(Permission.CREATE_CLIENT)
    public Client createClient(Long companyId, Client client) {
        verifyCompanyAccess(companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        client.setCompany(company);

        log.info("Client created for company: {} - Name: {}", companyId, client.getName());
        return clientRepository.save(client);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> getClientById(Long companyId, Long clientId) {
        verifyCompanyAccess(companyId);

        return clientRepository.findById(clientId)
                .filter(client -> client.getCompany().getId().equals(companyId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> getCompanyClients(Long companyId) {
        verifyCompanyAccess(companyId);
        return clientRepository.findByCompanyId(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Client> searchClients(Long companyId, String searchTerm) {
        verifyCompanyAccess(companyId);
        return clientRepository.findByCompanyIdAndNameContainingIgnoreCase(companyId, searchTerm);
    }

    @Override
    @RequirePermission(Permission.UPDATE_CLIENT)
    public Client updateClient(Long companyId, Long clientId, Client clientDetails) {
        verifyCompanyAccess(companyId);

        Client existing = clientRepository.findById(clientId)
                .filter(client -> client.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Client not found or access denied"));

        if (clientDetails.getName() != null) {
            existing.setName(clientDetails.getName());
        }
        if (clientDetails.getEmail() != null) {
            existing.setEmail(clientDetails.getEmail());
        }
        if (clientDetails.getPhone() != null) {
            existing.setPhone(clientDetails.getPhone());
        }
        if (clientDetails.getAddress() != null) {
            existing.setAddress(clientDetails.getAddress());
        }

        log.info("Client updated: {} for company: {}", clientId, companyId);
        return clientRepository.save(existing);
    }

    @Override
    @RequirePermission(Permission.DELETE_CLIENT)
    public void deleteClient(Long companyId, Long clientId) {
        verifyCompanyAccess(companyId);

        Client client = clientRepository.findById(clientId)
                .filter(c -> c.getCompany().getId().equals(companyId))
                .orElseThrow(() -> new IllegalArgumentException("Client not found or access denied"));

        log.info("Client deleted: {} for company: {}", clientId, companyId);
        clientRepository.delete(client);
    }

    // ==================== Company Analytics ====================

    @Override
    @Transactional(readOnly = true)
    public CompanyStatistics getCompanyStatistics(Long companyId) {
        verifyCompanyAccess(companyId);

        long vehicleCount = getVehicleCount(companyId);
        long driverCount = getDriverCount(companyId);
        long clientCount = clientRepository.findByCompanyId(companyId).size();
        long activeRentals = getActiveRentalCount(companyId);

        // Calculate total revenue and pending amounts (can be enhanced with actual invoice calculations)
        double totalRevenue = 0.0;
        double pendingAmount = 0.0;

        return new CompanyStatistics(vehicleCount, driverCount, clientCount, activeRentals, totalRevenue, pendingAmount);
    }

    @Override
    @Transactional(readOnly = true)
    public CompanySubscriptionInfo getSubscriptionInfo(Long companyId) {
        verifyCompanyAccess(companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        // Get current user count and vehicle count to check limits
        long currentUsers = getUserCount(companyId);
        long currentVehicles = getVehicleCount(companyId);

        return new CompanySubscriptionInfo(
                "STANDARD",
                company.getStatus().toString(),
                System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000), // 1 year expiry
                100L,  // Max vehicles
                50L,   // Max users
                99.99  // Monthly cost
        );
    }

    @Override
    @RequirePermission(Permission.VIEW_FINANCIAL_REPORTS)
    @Transactional(readOnly = true)
    public RevenueMetrics getRevenueMetrics(Long companyId, String period) {
        verifyCompanyAccess(companyId);

        // This would typically query Invoice and PayrollRecord tables for the specified period
        // For now, returning empty metrics that can be enhanced with actual calculations
        double totalIncome = 0.0;
        double totalExpenses = 0.0;
        double netProfit = totalIncome - totalExpenses;
        long transactionCount = 0L;

        log.info("Fetching revenue metrics for company {} for period: {}", companyId, period);
        return new RevenueMetrics(totalIncome, totalExpenses, netProfit, transactionCount);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCompanyActive(Long companyId) {
        return companyRepository.findById(companyId)
                .map(company -> company.getStatus() == Company.CompanyStatus.ACTIVE)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public long getVehicleCount(Long companyId) {
        return vehicleRepository.countByCompanyId(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getDriverCount(Long companyId) {
        return driverRepository.countByCompanyId(companyId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUserCount(Long companyId) {
        Long count = userRepository.countByCompanyId(companyId);
        return count != null ? count : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveRentalCount(Long companyId) {
        // Count active rental contracts (ACTIVE status)
        return rentalContractRepository.countByCompanyIdAndStatus(companyId, RentalContract.ContractStatus.ACTIVE);
    }

    // ==================== Subscription Management ====================

    /**
     * Get company subscription
     */
    @Transactional(readOnly = true)
    public Optional<CompanySubscription> getCompanySubscription(Long companyId) {
        verifyCompanyAccess(companyId);
        return companySubscriptionRepository.findByCompanyId(companyId);
    }

    /**
     * Create or update company subscription
     */
    @RequirePermission(Permission.MANAGE_COMPANY_SETTINGS)
    public CompanySubscription saveSubscription(Long companyId, CompanySubscription subscription) {
        verifyCompanyAccess(companyId);

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        subscription.setCompany(company);

        log.info("Saving subscription for company {}: Plan={}, Status={}",
                companyId, subscription.getPlan(), subscription.getStatus());
        return companySubscriptionRepository.save(subscription);
    }

    /**
     * Check if company subscription is valid
     */
    @Transactional(readOnly = true)
    public boolean isSubscriptionValid(Long companyId) {
        return companySubscriptionRepository.findByCompanyId(companyId)
                .map(CompanySubscription::isValid)
                .orElse(false);
    }

    /**
     * Check if company subscription is expired
     */
    @Transactional(readOnly = true)
    public boolean isSubscriptionExpired(Long companyId) {
        return companySubscriptionRepository.findByCompanyId(companyId)
                .map(CompanySubscription::isExpired)
                .orElse(true);
    }

    /**
     * Check if subscription will expire soon (within 7 days)
     */
    @Transactional(readOnly = true)
    public boolean isSubscriptionExpiringSoon(Long companyId) {
        return companySubscriptionRepository.findByCompanyId(companyId)
                .map(CompanySubscription::isExpiringsoon)
                .orElse(false);
    }

    /**
     * Renew subscription
     */
    @RequirePermission(Permission.MANAGE_COMPANY_SETTINGS)
    public CompanySubscription renewSubscription(Long companyId) {
        CompanySubscription subscription = companySubscriptionRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new IllegalArgumentException("No subscription found for company"));

        subscription.setStatus(CompanySubscription.SubscriptionStatus.ACTIVE);
        subscription.setExpiryDate(LocalDateTime.now().plusYears(1));

        log.info("Renewing subscription for company {}", companyId);
        return companySubscriptionRepository.save(subscription);
    }

    /**
     * Cancel subscription
     */
    @RequirePermission(Permission.MANAGE_COMPANY_SETTINGS)
    public void cancelSubscription(Long companyId) {
        CompanySubscription subscription = companySubscriptionRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new IllegalArgumentException("No subscription found for company"));

        subscription.setStatus(CompanySubscription.SubscriptionStatus.CANCELLED);
        companySubscriptionRepository.save(subscription);

        log.info("Cancelling subscription for company {}", companyId);
    }

    /**
     * Suspend subscription
     */
    @RequirePermission(Permission.MANAGE_COMPANY_SETTINGS)
    public void suspendSubscription(Long companyId) {
        CompanySubscription subscription = companySubscriptionRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new IllegalArgumentException("No subscription found for company"));

        subscription.setStatus(CompanySubscription.SubscriptionStatus.SUSPENDED);
        companySubscriptionRepository.save(subscription);

        log.info("Suspending subscription for company {}", companyId);
    }

    /**
     * Check if company has reached vehicle limit
     */
    @Transactional(readOnly = true)
    public boolean hasReachedVehicleLimit(Long companyId) {
        Optional<CompanySubscription> subscription = companySubscriptionRepository.findByCompanyId(companyId);
        if (subscription.isEmpty()) {
            return true;
        }

        long vehicleCount = getVehicleCount(companyId);
        return vehicleCount >= subscription.get().getMaxVehicles();
    }

    /**
     * Check if company has reached user limit
     */
    @Transactional(readOnly = true)
    public boolean hasReachedUserLimit(Long companyId) {
        Optional<CompanySubscription> subscription = companySubscriptionRepository.findByCompanyId(companyId);
        if (subscription.isEmpty()) {
            return true;
        }

        long userCount = getUserCount(companyId);
        return userCount >= subscription.get().getMaxUsers();
    }

    /**
     * Check if company has reached driver limit
     */
    @Transactional(readOnly = true)
    public boolean hasReachedDriverLimit(Long companyId) {
        Optional<CompanySubscription> subscription = companySubscriptionRepository.findByCompanyId(companyId);
        if (subscription.isEmpty()) {
            return true;
        }

        long driverCount = getDriverCount(companyId);
        return driverCount >= subscription.get().getMaxDrivers();
    }

    // ==================== Helper Methods ====================

    /**
     * Verify user has access to company (multi-tenant check)
     * OWNER can access any company, others can only access their own
     */
    private void verifyCompanyAccess(Long companyId) {
        if (!SecurityUtils.hasRole(com.devcast.fleetmanagement.features.user.model.util.Role.OWNER)) {
            if (!SecurityUtils.canAccessCompany(companyId)) {
                throw new AccessDeniedException("Access denied to company: " + companyId);
            }
        }
    }

    /**
     * Get company features availability map
     * Determines which features are available based on company subscription status and enabled flags
     *
     * @param companyId The company ID
     * @return Map of feature names to boolean availability
     */
    @Override
    public java.util.Map<String, Boolean> getCompanyFeatures(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        java.util.Map<String, Boolean> features = new java.util.HashMap<>();

        // Core features always available
        features.put("vehicleManagement", true);
        features.put("driverManagement", true);
        features.put("clients", true);
        features.put("companySettings", true); // Admin can always access settings
        features.put("userManagement", true); // Admin can always manage users

        // Check subscription for premium features
        Optional<CompanySubscription> subscriptionOpt = companySubscriptionRepository.findByCompanyId(companyId);

        if (subscriptionOpt.isPresent()) {
            CompanySubscription subscription = subscriptionOpt.get();
            boolean subscriptionValid = subscription.isValid(); // Check if subscription is active and not expired

            // Premium features available only if subscription is valid
            // Use explicit feature flags from subscription model
            features.put("gpsTracking", subscriptionValid && subscription.getGpsTrackingEnabled());
            features.put("fuelTracking", subscriptionValid && subscription.getFuelTrackingEnabled());
            features.put("maintenance", subscriptionValid && subscription.getMaintenanceTrackingEnabled());
            features.put("mobileApp", subscriptionValid && subscription.getMobileAppEnabled());
            features.put("payroll", subscriptionValid && subscription.getPayrollIntegrationEnabled());

            // Features available based on subscription plan tier
            features.put("invoicing", subscriptionValid); // All paid subscriptions get invoicing
            features.put("rentalContracts", subscriptionValid); // All paid subscriptions get rental management
            features.put("auditLogs", subscriptionValid); // Compliance feature for paid subscriptions
        } else {
            // No subscription - limited to core features only
            features.put("gpsTracking", false);
            features.put("fuelTracking", false);
            features.put("maintenance", false);
            features.put("invoicing", false);
            features.put("payroll", false);
            features.put("rentalContracts", false);
            features.put("auditLogs", false);
            features.put("mobileApp", false);
        }

        return features;
    }

    /**
     * Validate company input data
     */
    private void validateCompanyInput(Company company) {
        if (company.getName() == null || company.getName().isEmpty()) {
            throw new IllegalArgumentException("Company name is required");
        }
        if (company.getBusinessType() == null) {
            throw new IllegalArgumentException("Business type is required");
        }
        if (company.getCurrency() == null || company.getCurrency().isEmpty()) {
            throw new IllegalArgumentException("Currency is required");
        }
    }
}
