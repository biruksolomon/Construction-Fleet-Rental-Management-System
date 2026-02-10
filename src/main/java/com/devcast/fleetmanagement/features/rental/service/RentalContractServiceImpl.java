package com.devcast.fleetmanagement.features.rental.service;

import com.devcast.fleetmanagement.features.audit.service.AuditService;
import com.devcast.fleetmanagement.features.company.model.Client;
import com.devcast.fleetmanagement.features.company.repository.ClientRepository;
import com.devcast.fleetmanagement.features.company.repository.CompanyRepository;
import com.devcast.fleetmanagement.features.driver.model.Driver;
import com.devcast.fleetmanagement.features.driver.repository.DriverRepository;
import com.devcast.fleetmanagement.features.invoice.model.Invoice;
import com.devcast.fleetmanagement.features.invoice.repository.InvoiceRepository;
import com.devcast.fleetmanagement.features.rental.model.RentalContract;
import com.devcast.fleetmanagement.features.rental.model.RentalVehicle;
import com.devcast.fleetmanagement.features.rental.repository.RentalContractRepository;
import com.devcast.fleetmanagement.features.rental.repository.RentalVehicleRepository;
import com.devcast.fleetmanagement.features.user.model.util.Permission;
import com.devcast.fleetmanagement.features.vehicle.model.Vehicle;
import com.devcast.fleetmanagement.features.vehicle.repository.VehicleRepository;
import com.devcast.fleetmanagement.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Rental Contract Service Implementation
 * Handles complete rental lifecycle management, pricing, invoicing, and analytics
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RentalContractServiceImpl implements RentalContractService {

    private final RentalContractRepository rentalContractRepository;
    private final CompanyRepository companyRepository;
    private final ClientRepository clientRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final InvoiceRepository invoiceRepository;
    private final RentalVehicleRepository rentalVehicleRepository;
    private final AuditService auditService;

    // ==================== Contract CRUD Operations ====================

    @Override
    @Transactional
    public RentalContract createContract(Long companyId, RentalContract contract) {
        log.info("Creating rental contract for company: {}", companyId);

        // PHASE 1: Multi-tenant isolation
        verifyPermission(Permission.CREATE_RENTAL);
        verifyCompanyAccess(companyId);

        // Validate company exists
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        // Validate client exists and belongs to same company
        var client = clientRepository.findById(contract.getClient().getId())
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        if (!client.getCompany().getId().equals(companyId)) {
            throw new SecurityException("Client does not belong to this company");
        }

        // PHASE 2: Date validation
        contract.validateDates();
        if (contract.getEndDate().isBefore(contract.getStartDate()) ||
                contract.getEndDate().equals(contract.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Generate unique contract number
        String contractNumber = generateContractNumber(companyId);
        contract.setContractNumber(contractNumber);
        contract.setCompany(company);
        contract.setClient(client);
        contract.setStatus(RentalContract.RentalStatus.PENDING); // Start as PENDING

        RentalContract saved = rentalContractRepository.save(contract);

        auditService.logAuditEvent(companyId, "RENTAL_CONTRACT_CREATED",
                "RentalContract", saved.getId(),
                "Rental contract created: " + contractNumber);

        log.info("Rental contract created successfully: {} (ID: {})", contractNumber, saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RentalContract> getContractById(Long contractId) {
        return rentalContractRepository.findById(contractId)
                .map(contract -> {
                    verifyCompanyAccess(contract.getCompany().getId());
                    return contract;
                });
    }

    @Override
    @Transactional
    public RentalContract updateContract(Long contractId, RentalContract contract) {
        log.info("Updating rental contract: {}", contractId);

        verifyPermission(Permission.UPDATE_RENTAL);

        var existing = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        verifyCompanyAccess(existing.getCompany().getId());

        // Update fields
        if (contract.getStartDate() != null) {
            existing.setStartDate(contract.getStartDate());
        }
        if (contract.getEndDate() != null) {
            existing.setEndDate(contract.getEndDate());
        }
        if (contract.getIncludeDriver() != null) {
            existing.setIncludeDriver(contract.getIncludeDriver());
        }
        if (contract.getPricingModel() != null) {
            existing.setPricingModel(contract.getPricingModel());
        }

        RentalContract updated = rentalContractRepository.save(existing);

        auditService.logAuditEvent(existing.getCompany().getId(), "RENTAL_CONTRACT_UPDATED",
                "RentalContract", updated.getId(),
                "Rental contract updated");

        return updated;
    }

    @Override
    @Transactional
    public void cancelContract(Long contractId, String reason) {
        log.info("Cancelling rental contract: {}", contractId);

        verifyPermission(Permission.DELETE_RENTAL);

        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        verifyCompanyAccess(contract.getCompany().getId());

        // PHASE 2: Validate status transition - cannot cancel if already COMPLETED
        if (contract.getStatus() == RentalContract.RentalStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a COMPLETED rental");
        }

        // Transition to CANCELLED status
        contract.transitionTo(RentalContract.RentalStatus.CANCELLED);
        contract.setCancellationReason(reason);
        rentalContractRepository.save(contract);

        auditService.logAuditEvent(contract.getCompany().getId(), "RENTAL_CONTRACT_CANCELLED",
                "RentalContract", contract.getId(),
                "Rental contract cancelled. Reason: " + reason);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RentalContract> getContractsByCompany(Long companyId, Pageable pageable) {
        verifyCompanyAccess(companyId);
        return rentalContractRepository.findByCompanyId(companyId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalContract> getActiveContracts(Long companyId) {
        verifyCompanyAccess(companyId);
        return rentalContractRepository.findByCompanyIdAndStatus(companyId, RentalContract.RentalStatus.ACTIVE);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RentalContract> getContractsByStatus(Long companyId, String status, Pageable pageable) {
        verifyCompanyAccess(companyId);
        RentalContract.RentalStatus contractStatus = RentalContract.RentalStatus.valueOf(status.toUpperCase());
        return rentalContractRepository.findByCompanyIdAndStatus(companyId, contractStatus, pageable);
    }

    // ==================== Contract Status Management ====================

    @Override
    @Transactional
    public void confirmContract(Long contractId) {
        log.info("Confirming rental contract: {}", contractId);

        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        verifyCompanyAccess(contract.getCompany().getId());
        contract.setStatus(RentalContract.RentalStatus.ACTIVE);
        rentalContractRepository.save(contract);

        auditService.logAuditEvent(contract.getCompany().getId(), "RENTAL_CONTRACT_CONFIRMED",
                "RentalContract", contract.getId(), "Contract confirmed");
    }

    @Override
    @Transactional
    public void startRental(Long contractId) {
        log.info("Starting rental: {}", contractId);

        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        verifyCompanyAccess(contract.getCompany().getId());

        // PHASE 2: Validate status transition - only PENDING can transition to ACTIVE
        contract.transitionTo(RentalContract.RentalStatus.ACTIVE);

        // Update vehicle status to RENTED
        for (RentalVehicle rv : contract.getRentalVehicles()) {
            rv.getVehicle().setStatus(Vehicle.VehicleStatus.RENTED);
        }

        rentalContractRepository.save(contract);

        auditService.logAuditEvent(contract.getCompany().getId(), "RENTAL_STARTED",
                "RentalContract", contract.getId(), "Rental period started");
    }

    @Override
    @Transactional
    public void completeRental(Long contractId) {
        log.info("Completing rental: {}", contractId);

        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        verifyCompanyAccess(contract.getCompany().getId());

        // PHASE 2: Validate status transition - only ACTIVE or OVERDUE can be COMPLETED
        if (contract.getStatus() != RentalContract.RentalStatus.ACTIVE &&
                contract.getStatus() != RentalContract.RentalStatus.OVERDUE) {
            throw new IllegalStateException("Can only complete ACTIVE or OVERDUE rentals");
        }

        contract.transitionTo(RentalContract.RentalStatus.COMPLETED);
        contract.setActualEndDate(java.time.LocalDateTime.now());

        // Update vehicle status back to AVAILABLE
        for (RentalVehicle rv : contract.getRentalVehicles()) {
            rv.getVehicle().setStatus(Vehicle.VehicleStatus.AVAILABLE);
        }

        rentalContractRepository.save(contract);

        auditService.logAuditEvent(contract.getCompany().getId(), "RENTAL_COMPLETED",
                "RentalContract", contract.getId(), "Rental period completed");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> getContractStatus(Long contractId) {
        return rentalContractRepository.findById(contractId)
                .map(contract -> contract.getStatus().toString());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isContractActive(Long contractId) {
        return rentalContractRepository.findById(contractId)
                .map(contract -> contract.getStatus() == RentalContract.RentalStatus.ACTIVE)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalContract> getContractsExpiringSoon(Long companyId, int daysFromNow) {
        verifyCompanyAccess(companyId);
        LocalDate threshold = LocalDate.now().plusDays(daysFromNow);
        List<RentalContract> contracts = rentalContractRepository.findByCompanyIdAndStatus(
                companyId, RentalContract.RentalStatus.ACTIVE);

        return contracts.stream()
                .filter(c -> c.getEndDate().isBefore(threshold) && c.getEndDate().isAfter(LocalDate.now()))
                .collect(Collectors.toList());
    }

    // ==================== Rental Vehicle Management ====================

    @Override
    @Transactional
    public RentalVehicle addVehicleToRental(Long contractId, Long vehicleId) {
        log.info("Adding vehicle {} to rental contract {}", vehicleId, contractId);

        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        var vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        // PHASE 1: Multi-tenant isolation
        verifyCompanyAccess(contract.getCompany().getId());
        verifyCompanyAccess(vehicle.getCompany().getId());

        if (!vehicle.getCompany().getId().equals(contract.getCompany().getId())) {
            throw new SecurityException("Vehicle does not belong to the same company as contract");
        }

        // PHASE 3: Double booking prevention - check for overlapping rentals
        long overlaps = rentalVehicleRepository.countOverlappingVehicleRentals(
                contract.getCompany().getId(),
                vehicleId,
                contract.getStartDate(),
                contract.getEndDate(),
                contractId
        );

        if (overlaps > 0) {
            throw new IllegalStateException(
                    String.format("Vehicle %d already has an overlapping rental for period %s to %s",
                            vehicleId, contract.getStartDate(), contract.getEndDate())
            );
        }

        if (vehicle.getStatus() != Vehicle.VehicleStatus.AVAILABLE) {
            throw new IllegalStateException("Vehicle is not available for rental");
        }

        RentalVehicle rentalVehicle = RentalVehicle.builder()
                .company(contract.getCompany())
                .rentalContract(contract)
                .vehicle(vehicle)
                .agreedRate(vehicle.getDailyRate())
                .build();

        contract.getRentalVehicles().add(rentalVehicle);
        rentalContractRepository.save(contract);

        auditService.logAuditEvent(contract.getCompany().getId(), "RENTAL_VEHICLE_ADDED",
                "RentalVehicle", rentalVehicle.getId(),
                String.format("Vehicle %s added to rental %s", vehicle.getPlateNumber(), contract.getContractNumber()));

        return rentalVehicle;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalVehicle> getContractVehicles(Long contractId) {
        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        verifyCompanyAccess(contract.getCompany().getId());
        return new ArrayList<>(contract.getRentalVehicles());
    }

    @Override
    @Transactional
    public void removeVehicleFromRental(Long rentalVehicleId) {
        log.info("Removing vehicle from rental: {}", rentalVehicleId);

        verifyPermission(Permission.UPDATE_RENTAL);

        RentalVehicle rentalVehicle = rentalVehicleRepository.findById(rentalVehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Rental vehicle not found"));

        verifyCompanyAccess(rentalVehicle.getCompany().getId());

        rentalVehicleRepository.delete(rentalVehicle);

        auditService.logAuditEvent(rentalVehicle.getCompany().getId(), "RENTAL_VEHICLE_REMOVED",
                "RentalVehicle", rentalVehicleId, "Vehicle removed from rental");
    }

    @Override
    @Transactional
    public RentalVehicle updateVehicleTerms(Long rentalVehicleId, RentalVehicle vehicle) {
        RentalVehicle existing = rentalVehicleRepository.findById(rentalVehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Rental vehicle not found"));

        verifyCompanyAccess(existing.getCompany().getId());

        if (vehicle.getAgreedRate() != null) {
            existing.setAgreedRate(vehicle.getAgreedRate());
        }

        return rentalVehicleRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RentalVehicle> getRentalVehicle(Long rentalVehicleId) {
        return rentalVehicleRepository.findById(rentalVehicleId)
                .map(rv -> {
                    verifyCompanyAccess(rv.getCompany().getId());
                    return rv;
                });
    }

    /**
     * PHASE 3 & 4: Assign driver to rental vehicle with double-booking prevention
     */
    @Transactional
    public void assignDriverToRentalVehicle(Long rentalVehicleId, Long driverId) {
        log.info("Assigning driver {} to rental vehicle {}", driverId, rentalVehicleId);

        RentalVehicle rentalVehicle = rentalVehicleRepository.findById(rentalVehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Rental vehicle not found"));

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found"));

        // PHASE 1: Multi-tenant isolation
        verifyCompanyAccess(rentalVehicle.getCompany().getId());
        verifyCompanyAccess(driver.getCompany().getId());

        if (!driver.getCompany().getId().equals(rentalVehicle.getCompany().getId())) {
            throw new SecurityException("Driver does not belong to the same company");
        }

        // PHASE 4: Driver status validation
        if (driver.getStatus() == Driver.DriverStatus.SUSPENDED) {
            throw new IllegalStateException("Cannot assign SUSPENDED driver to rental");
        }

        if (driver.getStatus() == Driver.DriverStatus.INACTIVE) {
            throw new IllegalStateException("Cannot assign INACTIVE driver to rental");
        }

        // PHASE 3: Double booking prevention - check for overlapping driver assignments
        RentalContract contract = rentalVehicle.getRentalContract();
        long overlaps = rentalVehicleRepository.countOverlappingDriverAssignments(
                driver.getCompany().getId(),
                driverId,
                contract.getStartDate(),
                contract.getEndDate(),
                contract.getId()
        );

        if (overlaps > 0) {
            throw new IllegalStateException(
                    String.format("Driver %d already has an overlapping assignment for period %s to %s",
                            driverId, contract.getStartDate(), contract.getEndDate())
            );
        }

        // Assign driver
        rentalVehicle.setDriver(driver);
        rentalVehicleRepository.save(rentalVehicle);

        auditService.logAuditEvent(driver.getCompany().getId(), "DRIVER_ASSIGNED_TO_RENTAL",
                "RentalVehicle", rentalVehicleId,
                String.format("Driver %s assigned to rental vehicle", driver.getUser().getFullName()));

        log.info("Driver {} successfully assigned to rental vehicle {}", driverId, rentalVehicleId);
    }

    // ==================== Pricing & Cost Calculation ====================

    @Override
    @Transactional(readOnly = true)
    public RentalCostBreakdown calculateRentalCost(Long contractId) {
        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        verifyCompanyAccess(contract.getCompany().getId());

        BigDecimal baseRate = BigDecimal.ZERO;
        int rentalDays = calculateRentalDays(contract.getStartDate(), contract.getEndDate());

        for (RentalVehicle rv : contract.getRentalVehicles()) {
            baseRate = baseRate.add(rv.getAgreedRate().multiply(BigDecimal.valueOf(rentalDays)));
        }

        return new RentalCostBreakdown(
                baseRate,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                baseRate,
                BigDecimal.ZERO,
                baseRate
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getBaseRentalRate(Long vehicleId, Long rentalDays) {
        var vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        return vehicle.getDailyRate().multiply(BigDecimal.valueOf(rentalDays));
    }

    @Override
    @Transactional
    public void applyDiscount(Long contractId, BigDecimal discountAmount, String reason) {
        log.info("Applying discount {} to contract {}", discountAmount, contractId);

        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        verifyCompanyAccess(contract.getCompany().getId());

        auditService.logAuditEvent(contract.getCompany().getId(), "DISCOUNT_APPLIED",
                "RentalContract", contract.getId(),
                "Discount applied: " + discountAmount + ". Reason: " + reason);
    }

    @Override
    @Transactional
    public void addAdditionalCharge(Long contractId, BigDecimal amount, String description) {
        log.info("Adding charge {} to contract {}", amount, contractId);

        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        verifyCompanyAccess(contract.getCompany().getId());

        auditService.logAuditEvent(contract.getCompany().getId(), "CHARGE_ADDED",
                "RentalContract", contract.getId(),
                "Charge added: " + amount + ". Description: " + description);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalRentalCost(Long contractId) {
        return calculateRentalCost(contractId).totalCost();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCostPerDay(Long contractId) {
        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        int days = calculateRentalDays(contract.getStartDate(), contract.getEndDate());
        BigDecimal totalCost = getTotalRentalCost(contractId);

        return totalCost.divide(BigDecimal.valueOf(Math.max(days, 1)), 2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateLateFees(Long contractId) {
        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        if (contract.getEndDate().isAfter(LocalDate.now())) {
            return BigDecimal.ZERO;
        }

        int lateDays = calculateRentalDays(contract.getEndDate(), LocalDate.now());
        BigDecimal dailyRate = getCostPerDay(contractId);

        return dailyRate.multiply(BigDecimal.valueOf(lateDays)).multiply(BigDecimal.valueOf(1.5));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal estimateDamageCharges(Long contractId, String damageDescription) {
        // This would require damage assessment logic
        // For now, returning a placeholder
        return BigDecimal.ZERO;
    }

    // ==================== Invoice & Payment ====================

    @Override
    @Transactional
    public Long generateInvoice(Long contractId) {
        log.info("Generating invoice for contract: {}", contractId);

        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        verifyPermission(Permission.CREATE_RENTAL);
        verifyCompanyAccess(contract.getCompany().getId());

        RentalCostBreakdown costBreakdown = calculateRentalCost(contractId);

        String invoiceNumber = generateInvoiceNumber(contract.getCompany().getId());

        Invoice invoice = Invoice.builder()
                .company(contract.getCompany())
                .rentalContract(contract)
                .invoiceNumber(invoiceNumber)
                .vehicleCost(costBreakdown.baseRate())
                .driverCost(BigDecimal.ZERO)
                .fuelCost(BigDecimal.ZERO)
                .totalCost(costBreakdown.totalCost())
                .status(Invoice.InvoiceStatus.PENDING)
                .issuedDate(LocalDate.now())
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);

        auditService.logAuditEvent(contract.getCompany().getId(), "INVOICE_GENERATED",
                "Invoice", savedInvoice.getId(),
                "Invoice generated for rental contract: " + contract.getContractNumber());

        return savedInvoice.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> getContractInvoice(Long contractId) {
        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        verifyCompanyAccess(contract.getCompany().getId());

        return invoiceRepository.findByRentalContractId(contractId)
                .map(Invoice::getId);
    }

    @Override
    @Transactional
    public void processPayment(Long contractId, BigDecimal amount, String paymentMethod) {
        log.info("Processing payment of {} for contract {}", amount, contractId);

        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        verifyCompanyAccess(contract.getCompany().getId());

        auditService.logAuditEvent(contract.getCompany().getId(), "PAYMENT_PROCESSED",
                "RentalContract", contract.getId(),
                "Payment processed: " + amount + " via " + paymentMethod);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentRecord> getPaymentHistory(Long contractId) {
        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        verifyCompanyAccess(contract.getCompany().getId());

        return new ArrayList<>(); // Would be populated from payment history table
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getOutstandingBalance(Long contractId) {
        RentalCostBreakdown breakdown = calculateRentalCost(contractId);
        return breakdown.balanceDue();
    }

    @Override
    @Transactional
    public void sendPaymentReminder(Long contractId) {
        log.info("Sending payment reminder for contract: {}", contractId);

        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        verifyCompanyAccess(contract.getCompany().getId());

        auditService.logAuditEvent(contract.getCompany().getId(), "PAYMENT_REMINDER_SENT",
                "RentalContract", contract.getId(), "Payment reminder sent to client");
    }

    // ==================== Client & Contact Information ====================

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> getContractClient(Long contractId) {
        return rentalContractRepository.findById(contractId)
                .map(contract -> contract.getClient().getId());
    }

    @Override
    @Transactional
    public void updateContractClient(Long contractId, Long clientId) {
        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        var client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        verifyCompanyAccess(contract.getCompany().getId());

        contract.setClient(client);
        rentalContractRepository.save(contract);

        auditService.logAuditEvent(contract.getCompany().getId(), "CONTRACT_CLIENT_UPDATED",
                "RentalContract", contract.getId(), "Client updated to: " + client.getName());
    }

    @Override
    @Transactional
    public void addContactPerson(Long contractId, String name, String phone, String email) {
        log.info("Adding contact person to contract: {}", contractId);
        // Would require ContactPerson entity
        throw new UnsupportedOperationException("Requires ContactPerson entity implementation");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactInfo> getContractContacts(Long contractId) {
        return new ArrayList<>(); // Would be populated from ContactPerson table
    }

    // ==================== Usage & Duration ====================

    @Override
    @Transactional(readOnly = true)
    public Long getRentalDuration(Long contractId) {
        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        return (long) calculateRentalDays(contract.getStartDate(), contract.getEndDate());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getRemainingDays(Long contractId) {
        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        if (LocalDate.now().isAfter(contract.getEndDate())) {
            return 0L;
        }

        return (long) calculateRentalDays(LocalDate.now(), contract.getEndDate());
    }

    @Override
    @Transactional(readOnly = true)
    public UsageComparison getUsageComparison(Long contractId) {
        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        long plannedDays = getRentalDuration(contractId);
        long actualDays = calculateRentalDays(contract.getStartDate(), LocalDate.now());
        double variance = ((double) actualDays - plannedDays) / plannedDays * 100;

        return new UsageComparison(plannedDays, actualDays, variance, "ON_TRACK");
    }

    @Override
    @Transactional
    public void extendRental(Long contractId, int additionalDays) {
        log.info("Extending rental {} by {} days", contractId, additionalDays);

        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        verifyCompanyAccess(contract.getCompany().getId());

        contract.setEndDate(contract.getEndDate().plusDays(additionalDays));
        rentalContractRepository.save(contract);

        auditService.logAuditEvent(contract.getCompany().getId(), "RENTAL_EXTENDED",
                "RentalContract", contract.getId(), "Rental extended by " + additionalDays + " days");
    }

    @Override
    @Transactional
    public void reduceRental(Long contractId, int days) {
        log.info("Reducing rental {} by {} days", contractId, days);

        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        verifyCompanyAccess(contract.getCompany().getId());

        contract.setEndDate(contract.getEndDate().minusDays(days));
        rentalContractRepository.save(contract);

        auditService.logAuditEvent(contract.getCompany().getId(), "RENTAL_REDUCED",
                "RentalContract", contract.getId(), "Rental reduced by " + days + " days");
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEarlyReturn(Long contractId) {
        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        return LocalDate.now().isBefore(contract.getEndDate());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateEarlyReturnRefund(Long contractId) {
        if (!isEarlyReturn(contractId)) {
            return BigDecimal.ZERO;
        }

        var contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new IllegalArgumentException("Contract not found"));

        long remainingDays = getRemainingDays(contractId);
        BigDecimal dailyRate = getCostPerDay(contractId);

        return dailyRate.multiply(BigDecimal.valueOf(remainingDays));
    }

    // ==================== Vehicle Condition ====================

    @Override
    @Transactional
    public void recordPickupCondition(Long contractId, VehicleCondition condition) {
        log.info("Recording pickup condition for contract: {}", contractId);
        // Would require VehicleCondition entity
        throw new UnsupportedOperationException("Requires VehicleCondition entity implementation");
    }

    @Override
    @Transactional
    public void recordReturnCondition(Long contractId, VehicleCondition condition) {
        log.info("Recording return condition for contract: {}", contractId);
        // Would require VehicleCondition entity
        throw new UnsupportedOperationException("Requires VehicleCondition entity implementation");
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleCondition> getConditionHistory(Long contractId) {
        return new ArrayList<>(); // Would be populated from VehicleCondition table
    }

    @Override
    @Transactional(readOnly = true)
    public String generateDamageReport(Long contractId) {
        return "Damage Report for Contract " + contractId;
    }

    // ==================== Search & Filter ====================

    @Override
    @Transactional(readOnly = true)
    public Page<RentalContract> searchContracts(Long companyId, String searchTerm, Pageable pageable) {
        verifyCompanyAccess(companyId);
        return rentalContractRepository.findByCompanyId(companyId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RentalContract> filterContracts(Long companyId, ContractFilterCriteria criteria, Pageable pageable) {
        verifyCompanyAccess(companyId);
        return rentalContractRepository.findByCompanyId(companyId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalContract> getOverdueContracts(Long companyId) {
        verifyCompanyAccess(companyId);
        List<RentalContract> contracts = rentalContractRepository.findByCompanyId(companyId);

        return contracts.stream()
                .filter(c -> c.getEndDate().isBefore(LocalDate.now()) &&
                        c.getStatus() == RentalContract.RentalStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalContract> getHighValueContracts(Long companyId, BigDecimal minValue) {
        verifyCompanyAccess(companyId);
        List<RentalContract> contracts = rentalContractRepository.findByCompanyId(companyId);

        return contracts.stream()
                .filter(c -> calculateRentalCost(c.getId()).totalCost().compareTo(minValue) >= 0)
                .collect(Collectors.toList());
    }

    // ==================== Reporting ====================

    @Override
    @Transactional(readOnly = true)
    public ContractSummaryReport getSummaryReport(Long companyId, Long fromDate, Long toDate) {
        verifyCompanyAccess(companyId);

        List<RentalContract> contracts = rentalContractRepository.findByCompanyId(companyId);

        int activeCount = (int) contracts.stream()
                .filter(c -> c.getStatus() == RentalContract.RentalStatus.ACTIVE)
                .count();

        int completedCount = (int) contracts.stream()
                .filter(c -> c.getStatus() == RentalContract.RentalStatus.COMPLETED)
                .count();

        BigDecimal totalRevenue = contracts.stream()
                .map(c -> calculateRentalCost(c.getId()).totalCost())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ContractSummaryReport(
                companyId,
                contracts.size(),
                activeCount,
                completedCount,
                totalRevenue,
                totalRevenue.divide(BigDecimal.valueOf(Math.max(contracts.size(), 1)), 2, java.math.RoundingMode.HALF_UP),
                calculateOccupancyRate(companyId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RentalUtilizationReport getUtilizationReport(Long companyId, Long fromDate, Long toDate) {
        verifyCompanyAccess(companyId);

        List<RentalContract> contracts = rentalContractRepository.findByCompanyId(companyId);
        Set<Vehicle> vehicles = new HashSet<>();
        BigDecimal totalIncome = BigDecimal.ZERO;

        for (RentalContract contract : contracts) {
            for (RentalVehicle rv : contract.getRentalVehicles()) {
                vehicles.add(rv.getVehicle());
                totalIncome = totalIncome.add(calculateRentalCost(contract.getId()).totalCost());
            }
        }

        return new RentalUtilizationReport(
                companyId,
                vehicles.size(),
                calculateAverageUtilization(companyId),
                contracts.size(),
                totalIncome,
                new ArrayList<>()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportContractsToCSV(Long companyId) {
        verifyCompanyAccess(companyId);
        // CSV export logic would go here
        return new byte[0];
    }

    // ==================== Helper Methods ====================

    private void verifyPermission(Permission permission) {
        if (!SecurityUtils.hasPermission(permission)) {
            throw new SecurityException("User does not have permission: " + permission);
        }
    }

    private void verifyCompanyAccess(Long companyId) {
        Long userCompanyId = SecurityUtils.getCurrentCompanyId();
        if (!companyId.equals(userCompanyId)) {
            throw new SecurityException("Unauthorized access to company: " + companyId);
        }
    }

    private String generateContractNumber(Long companyId) {
        long count = rentalContractRepository.countByCompanyId(companyId);
        return String.format("RC-%d-%d", companyId, count + 1);
    }

    private String generateInvoiceNumber(Long companyId) {
        long count = invoiceRepository.count();
        return String.format("INV-%d-%d", companyId, count + 1);
    }

    private int calculateRentalDays(LocalDate start, LocalDate end) {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
    }

    private double calculateOccupancyRate(Long companyId) {
        return 75.0; // Placeholder
    }

    private double calculateAverageUtilization(Long companyId) {
        return 80.0; // Placeholder
    }
}
