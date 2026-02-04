package com.devcast.fleetmanagement.features.company.controller;

import com.devcast.fleetmanagement.features.auth.dto.ApiResponse;
import com.devcast.fleetmanagement.features.company.dto.*;;
import com.devcast.fleetmanagement.features.company.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Company Management REST Controller
 *
 * Endpoints for managing companies, settings, pricing rules, and clients
 * All endpoints require authentication and RBAC authorization
 */
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Companies", description = "Company management, configuration, and analytics endpoints")
public class CompanyController {

    private final CompanyService companyService;

    // ==================== Company CRUD Operations ====================

    /**
     * Create a new company
     * POST /api/companies
     *
     * Request Body: CompanyCreateRequest (no id, timestamps, or status)
     * Response: CompanyResponse (complete company information)
     */
    @PostMapping
    @Operation(summary = "Create company", description = "Create a new company (OWNER only)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Company created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(
            @Valid @RequestBody CompanyCreateRequest request
    ) {
        try {
            log.info("Creating company: {}", request.getName());
            CompanyResponse created = companyService.createCompany(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(created, "Company created successfully"));
        } catch (Exception e) {
            log.error("Error creating company", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create company: " + e.getMessage()));
        }
    }

    /**
     * Get company by ID
     * GET /api/companies/{companyId}
     *
     * Response: CompanyResponse (complete company information)
     */
    @GetMapping("/{companyId}")
    @Operation(summary = "Get company", description = "Get company details by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Company found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Company not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompany(
            @PathVariable Long companyId
    ) {
        try {
            return companyService.getCompanyById(companyId)
                    .map(company -> ResponseEntity.ok(ApiResponse.success(company, "Company retrieved successfully")))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving company {}", companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve company: " + e.getMessage()));
        }
    }

    /**
     * Get all companies with pagination
     * GET /api/companies
     *
     * Response: Page<CompanyResponse> (paginated company list)
     */
    @GetMapping
    @Operation(summary = "List companies", description = "Get paginated list of companies")
    public ResponseEntity<ApiResponse<Page<CompanyResponse>>> listCompanies(
            Pageable pageable
    ) {
        try {
            Page<CompanyResponse> companies = companyService.getAllCompanies(pageable);
            return ResponseEntity.ok(ApiResponse.success(companies, "Companies retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving companies", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve companies: " + e.getMessage()));
        }
    }

    /**
     * Update company
     * PUT /api/companies/{companyId}
     *
     * Request Body: CompanyUpdateRequest (all fields optional, no id/timestamps)
     * Response: CompanyResponse (complete updated company information)
     */
    @PutMapping("/{companyId}")
    @Operation(summary = "Update company", description = "Update company details")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Company updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Company not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<CompanyResponse>> updateCompany(
            @PathVariable Long companyId,
            @Valid @RequestBody CompanyUpdateRequest request
    ) {
        try {
            log.info("Updating company: {}", companyId);
            CompanyResponse updated = companyService.updateCompany(companyId, request);
            return ResponseEntity.ok(ApiResponse.success(updated, "Company updated successfully"));
        } catch (Exception e) {
            log.error("Error updating company {}", companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update company: " + e.getMessage()));
        }
    }

    /**
     * Delete company
     * DELETE /api/companies/{companyId}
     */
    @DeleteMapping("/{companyId}")
    @Operation(summary = "Delete company", description = "Delete company and cascade all related data (OWNER only)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Company deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Company not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    public ResponseEntity<ApiResponse<Void>> deleteCompany(
            @PathVariable Long companyId
    ) {
        try {
            log.warn("Deleting company: {}", companyId);
            companyService.deleteCompany(companyId);
            return ResponseEntity.ok(ApiResponse.success(null, "Company deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting company {}", companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete company: " + e.getMessage()));
        }
    }

    /**
     * Suspend company
     * POST /api/companies/{companyId}/suspend
     */
    @PostMapping("/{companyId}/suspend")
    @Operation(summary = "Suspend company", description = "Suspend company operations")
    public ResponseEntity<ApiResponse<Void>> suspendCompany(
            @PathVariable Long companyId,
            @RequestParam(required = false, defaultValue = "No reason provided") String reason
    ) {
        try {
            log.warn("Suspending company: {} - Reason: {}", companyId, reason);
            companyService.suspendCompany(companyId, reason);
            return ResponseEntity.ok(ApiResponse.success(null, "Company suspended successfully"));
        } catch (Exception e) {
            log.error("Error suspending company {}", companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to suspend company: " + e.getMessage()));
        }
    }

    /**
     * Activate company
     * POST /api/companies/{companyId}/activate
     */
    @PostMapping("/{companyId}/activate")
    @Operation(summary = "Activate company", description = "Activate suspended company")
    public ResponseEntity<ApiResponse<Void>> activateCompany(
            @PathVariable Long companyId
    ) {
        try {
            log.info("Activating company: {}", companyId);
            companyService.activateCompany(companyId);
            return ResponseEntity.ok(ApiResponse.success(null, "Company activated successfully"));
        } catch (Exception e) {
            log.error("Error activating company {}", companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to activate company: " + e.getMessage()));
        }
    }

    // ==================== Company Settings ====================

    /**
     * Get all settings for company
     * GET /api/companies/{companyId}/settings
     *
     * Response: List<CompanySettingResponse> (all company settings)
     */
    @GetMapping("/{companyId}/settings")
    @Operation(summary = "Get company settings", description = "Retrieve all company configuration settings")
    public ResponseEntity<ApiResponse<List<CompanySettingResponse>>> getSettings(
            @PathVariable Long companyId
    ) {
        try {
            List<CompanySettingResponse> settings = companyService.getCompanySettings(companyId);
            return ResponseEntity.ok(ApiResponse.success(settings, "Settings retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving company settings {}", companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve settings: " + e.getMessage()));
        }
    }

    /**
     * Save or update setting
     * POST /api/companies/{companyId}/settings
     *
     * Request Body: CompanySettingRequest (setting configuration)
     * Response: CompanySettingResponse (saved setting details)
     */
    @PostMapping("/{companyId}/settings")
    @Operation(summary = "Save setting", description = "Save or update a company configuration setting")
    public ResponseEntity<ApiResponse<CompanySettingResponse>> saveSetting(
            @PathVariable Long companyId,
            @Valid @RequestBody CompanySettingRequest request
    ) {
        try {
            log.info("Saving setting for company {}: {}", companyId, request.getKey());
            CompanySettingResponse setting = companyService.saveSetting(companyId, request);
            return ResponseEntity.ok(ApiResponse.success(setting, "Setting saved successfully"));
        } catch (Exception e) {
            log.error("Error saving setting for company {}", companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to save setting: " + e.getMessage()));
        }
    }

    /**
     * Update setting value
     * PUT /api/companies/{companyId}/settings/{key}
     */
    @PutMapping("/{companyId}/settings/{key}")
    @Operation(summary = "Update setting", description = "Update a company configuration setting value")
    public ResponseEntity<ApiResponse<Void>> updateSetting(
            @PathVariable Long companyId,
            @PathVariable String key,
            @RequestParam String value
    ) {
        try {
            log.info("Updating setting for company {}: {}", companyId, key);
            companyService.updateSetting(companyId, key, value);
            return ResponseEntity.ok(ApiResponse.success(null, "Setting updated successfully"));
        } catch (Exception e) {
            log.error("Error updating setting for company {}", companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update setting: " + e.getMessage()));
        }
    }

    /**
     * Delete setting
     * DELETE /api/companies/{companyId}/settings/{key}
     */
    @DeleteMapping("/{companyId}/settings/{key}")
    @Operation(summary = "Delete setting", description = "Delete a company configuration setting")
    public ResponseEntity<ApiResponse<Void>> deleteSetting(
            @PathVariable Long companyId,
            @PathVariable String key
    ) {
        try {
            log.info("Deleting setting for company {}: {}", companyId, key);
            companyService.deleteSetting(companyId, key);
            return ResponseEntity.ok(ApiResponse.success(null, "Setting deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting setting for company {}", companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete setting: " + e.getMessage()));
        }
    }

    // ==================== Pricing Rules ====================

    /**
     * Get all pricing rules
     * GET /api/companies/{companyId}/pricing-rules
     *
     * Response: List<PricingRuleResponse> (all pricing rules)
     */
    @GetMapping("/{companyId}/pricing-rules")
    @Operation(summary = "List pricing rules", description = "Get all pricing rules for company")
    public ResponseEntity<ApiResponse<List<PricingRuleResponse>>> getPricingRules(
            @PathVariable Long companyId
    ) {
        try {
            List<PricingRuleResponse> rules = companyService.getPricingRules(companyId);
            return ResponseEntity.ok(ApiResponse.success(rules, "Pricing rules retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving pricing rules for company {}", companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve pricing rules: " + e.getMessage()));
        }
    }

    /**
     * Get active pricing rules
     * GET /api/companies/{companyId}/pricing-rules/active
     *
     * Response: List<PricingRuleResponse> (active pricing rules only)
     */
    @GetMapping("/{companyId}/pricing-rules/active")
    @Operation(summary = "List active pricing rules", description = "Get active pricing rules for company")
    public ResponseEntity<ApiResponse<List<PricingRuleResponse>>> getActivePricingRules(
            @PathVariable Long companyId
    ) {
        try {
            List<PricingRuleResponse> rules = companyService.getActivePricingRules(companyId);
            return ResponseEntity.ok(ApiResponse.success(rules, "Active pricing rules retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving active pricing rules for company {}", companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve pricing rules: " + e.getMessage()));
        }
    }

    /**
     * Create pricing rule
     * POST /api/companies/{companyId}/pricing-rules
     *
     * Request Body: PricingRuleRequest (no id/timestamps, active defaults to true)
     * Response: PricingRuleResponse (created rule details)
     */
    @PostMapping("/{companyId}/pricing-rules")
    @Operation(summary = "Create pricing rule", description = "Create new pricing rule for company")
    public ResponseEntity<ApiResponse<PricingRuleResponse>> createPricingRule(
            @PathVariable Long companyId,
            @Valid @RequestBody PricingRuleRequest request
    ) {
        try {
            log.info("Creating pricing rule for company {}", companyId);
            PricingRuleResponse created = companyService.createPricingRule(companyId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(created, "Pricing rule created successfully"));
        } catch (Exception e) {
            log.error("Error creating pricing rule for company {}", companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create pricing rule: " + e.getMessage()));
        }
    }

    /**
     * Update pricing rule
     * PUT /api/companies/{companyId}/pricing-rules/{ruleId}
     *
     * Request Body: PricingRuleRequest (no id/timestamps)
     * Response: PricingRuleResponse (updated rule details)
     */
    @PutMapping("/{companyId}/pricing-rules/{ruleId}")
    @Operation(summary = "Update pricing rule", description = "Update existing pricing rule")
    public ResponseEntity<ApiResponse<PricingRuleResponse>> updatePricingRule(
            @PathVariable Long companyId,
            @PathVariable Long ruleId,
            @Valid @RequestBody PricingRuleRequest request
    ) {
        try {
            log.info("Updating pricing rule {} for company {}", ruleId, companyId);
            PricingRuleResponse updated = companyService.updatePricingRule(companyId, ruleId, request);
            return ResponseEntity.ok(ApiResponse.success(updated, "Pricing rule updated successfully"));
        } catch (Exception e) {
            log.error("Error updating pricing rule {} for company {}", ruleId, companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update pricing rule: " + e.getMessage()));
        }
    }

    /**
     * Activate pricing rule
     * POST /api/companies/{companyId}/pricing-rules/{ruleId}/activate
     */
    @PostMapping("/{companyId}/pricing-rules/{ruleId}/activate")
    @Operation(summary = "Activate pricing rule", description = "Activate a pricing rule")
    public ResponseEntity<ApiResponse<Void>> activatePricingRule(
            @PathVariable Long companyId,
            @PathVariable Long ruleId
    ) {
        try {
            log.info("Activating pricing rule {} for company {}", ruleId, companyId);
            companyService.activatePricingRule(companyId, ruleId);
            return ResponseEntity.ok(ApiResponse.success(null, "Pricing rule activated successfully"));
        } catch (Exception e) {
            log.error("Error activating pricing rule {} for company {}", ruleId, companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to activate pricing rule: " + e.getMessage()));
        }
    }

    /**
     * Deactivate pricing rule
     * POST /api/companies/{companyId}/pricing-rules/{ruleId}/deactivate
     */
    @PostMapping("/{companyId}/pricing-rules/{ruleId}/deactivate")
    @Operation(summary = "Deactivate pricing rule", description = "Deactivate a pricing rule")
    public ResponseEntity<ApiResponse<Void>> deactivatePricingRule(
            @PathVariable Long companyId,
            @PathVariable Long ruleId
    ) {
        try {
            log.info("Deactivating pricing rule {} for company {}", ruleId, companyId);
            companyService.deactivatePricingRule(companyId, ruleId);
            return ResponseEntity.ok(ApiResponse.success(null, "Pricing rule deactivated successfully"));
        } catch (Exception e) {
            log.error("Error deactivating pricing rule {} for company {}", ruleId, companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to deactivate pricing rule: " + e.getMessage()));
        }
    }

    /**
     * Delete pricing rule
     * DELETE /api/companies/{companyId}/pricing-rules/{ruleId}
     */
    @DeleteMapping("/{companyId}/pricing-rules/{ruleId}")
    @Operation(summary = "Delete pricing rule", description = "Delete a pricing rule")
    public ResponseEntity<ApiResponse<Void>> deletePricingRule(
            @PathVariable Long companyId,
            @PathVariable Long ruleId
    ) {
        try {
            log.info("Deleting pricing rule {} for company {}", ruleId, companyId);
            companyService.deletePricingRule(companyId, ruleId);
            return ResponseEntity.ok(ApiResponse.success(null, "Pricing rule deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting pricing rule {} for company {}", ruleId, companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete pricing rule: " + e.getMessage()));
        }
    }

    // ==================== Company Analytics ====================

    /**
     * Get company statistics
     * GET /api/companies/{companyId}/statistics
     */
    @GetMapping("/{companyId}/statistics")
    @Operation(summary = "Get company statistics", description = "Get company statistics (vehicles, drivers, clients, rentals)")
    public ResponseEntity<ApiResponse<CompanyStatistics>> getStatistics(
            @PathVariable Long companyId
    ) {
        try {
            CompanyStatistics stats = companyService.getCompanyStatistics(companyId);
            return ResponseEntity.ok(ApiResponse.success(stats, "Statistics retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving statistics for company {}", companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve statistics: " + e.getMessage()));
        }
    }

    /**
     * Get subscription info
     * GET /api/companies/{companyId}/subscription
     */
    @GetMapping("/{companyId}/subscription")
    @Operation(summary = "Get subscription info", description = "Get company subscription information")
    public ResponseEntity<ApiResponse<CompanySubscriptionInfo>> getSubscriptionInfo(
            @PathVariable Long companyId
    ) {
        try {
            CompanySubscriptionInfo info = companyService.getSubscriptionInfo(companyId);
            return ResponseEntity.ok(ApiResponse.success(info, "Subscription info retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving subscription info for company {}", companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve subscription info: " + e.getMessage()));
        }
    }

    /**
     * Get revenue metrics
     * GET /api/companies/{companyId}/revenue
     */
    @GetMapping("/{companyId}/revenue")
    @Operation(summary = "Get revenue metrics", description = "Get company revenue metrics for period")
    public ResponseEntity<ApiResponse<RevenueMetrics>> getRevenueMetrics(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "MONTHLY") String period
    ) {
        try {
            RevenueMetrics metrics = companyService.getRevenueMetrics(companyId, period);
            return ResponseEntity.ok(ApiResponse.success(metrics, "Revenue metrics retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving revenue metrics for company {}", companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve revenue metrics: " + e.getMessage()));
        }
    }

    // ==================== Clients ====================

    /**
     * Get all clients for company
     * GET /api/companies/{companyId}/clients
     *
     * Response: List<ClientResponse> (all company clients)
     */
    @GetMapping("/{companyId}/clients")
    @Operation(summary = "List clients", description = "Get all clients for company")
    public ResponseEntity<ApiResponse<List<ClientResponse>>> getClients(
            @PathVariable Long companyId
    ) {
        try {
            List<ClientResponse> clients = companyService.getCompanyClients(companyId);
            return ResponseEntity.ok(ApiResponse.success(clients, "Clients retrieved successfully"));
        } catch (Exception e) {
            log.error("Error retrieving clients for company {}", companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve clients: " + e.getMessage()));
        }
    }

    /**
     * Search clients
     * GET /api/companies/{companyId}/clients/search
     *
     * Response: List<ClientResponse> (matching clients)
     */
    @GetMapping("/{companyId}/clients/search")
    @Operation(summary = "Search clients", description = "Search clients by name or email")
    public ResponseEntity<ApiResponse<List<ClientResponse>>> searchClients(
            @PathVariable Long companyId,
            @RequestParam String query
    ) {
        try {
            List<ClientResponse> clients = companyService.searchClients(companyId, query);
            return ResponseEntity.ok(ApiResponse.success(clients, "Clients found successfully"));
        } catch (Exception e) {
            log.error("Error searching clients for company {}", companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to search clients: " + e.getMessage()));
        }
    }

    /**
     * Create client
     * POST /api/companies/{companyId}/clients
     *
     * Request Body: ClientRequest (no id, timestamps, or companyId)
     * Response: ClientResponse (created client details)
     */
    @PostMapping("/{companyId}/clients")
    @Operation(summary = "Create client", description = "Create new client for company")
    public ResponseEntity<ApiResponse<ClientResponse>> createClient(
            @PathVariable Long companyId,
            @Valid @RequestBody ClientRequest request
    ) {
        try {
            log.info("Creating client for company {}", companyId);
            ClientResponse created = companyService.createClient(companyId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(created, "Client created successfully"));
        } catch (Exception e) {
            log.error("Error creating client for company {}", companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to create client: " + e.getMessage()));
        }
    }

    /**
     * Get client by ID
     * GET /api/companies/{companyId}/clients/{clientId}
     *
     * Response: ClientResponse (complete client details)
     */
    @GetMapping("/{companyId}/clients/{clientId}")
    @Operation(summary = "Get client", description = "Get client details by ID")
    public ResponseEntity<ApiResponse<ClientResponse>> getClient(
            @PathVariable Long companyId,
            @PathVariable Long clientId
    ) {
        try {
            return companyService.getClientById(companyId, clientId)
                    .map(client -> ResponseEntity.ok(ApiResponse.success(client, "Client retrieved successfully")))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving client {} for company {}", clientId, companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to retrieve client: " + e.getMessage()));
        }
    }

    /**
     * Update client
     * PUT /api/companies/{companyId}/clients/{clientId}
     *
     * Request Body: ClientRequest (all fields optional)
     * Response: ClientResponse (updated client details)
     */
    @PutMapping("/{companyId}/clients/{clientId}")
    @Operation(summary = "Update client", description = "Update client details")
    public ResponseEntity<ApiResponse<ClientResponse>> updateClient(
            @PathVariable Long companyId,
            @PathVariable Long clientId,
            @Valid @RequestBody ClientRequest request
    ) {
        try {
            log.info("Updating client {} for company {}", clientId, companyId);
            ClientResponse updated = companyService.updateClient(companyId, clientId, request);
            return ResponseEntity.ok(ApiResponse.success(updated, "Client updated successfully"));
        } catch (Exception e) {
            log.error("Error updating client {} for company {}", clientId, companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to update client: " + e.getMessage()));
        }
    }

    /**
     * Delete client
     * DELETE /api/companies/{companyId}/clients/{clientId}
     */
    @DeleteMapping("/{companyId}/clients/{clientId}")
    @Operation(summary = "Delete client", description = "Delete a client")
    public ResponseEntity<ApiResponse<Void>> deleteClient(
            @PathVariable Long companyId,
            @PathVariable Long clientId
    ) {
        try {
            log.info("Deleting client {} for company {}", clientId, companyId);
            companyService.deleteClient(companyId, clientId);
            return ResponseEntity.ok(ApiResponse.success(null, "Client deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting client {} for company {}", clientId, companyId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to delete client: " + e.getMessage()));
        }
    }
}
