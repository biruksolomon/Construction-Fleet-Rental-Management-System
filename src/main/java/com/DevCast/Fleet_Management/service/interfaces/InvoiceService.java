package com.DevCast.Fleet_Management.service.interfaces;

import com.DevCast.Fleet_Management.model.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Invoice Service Interface
 * Handles invoice generation, payment tracking, and financial reporting
 */
public interface InvoiceService {

    // ==================== Invoice CRUD Operations ====================

    /**
     * Create invoice from rental contract
     */
    Invoice createInvoice(Long contractId);

    /**
     * Get invoice by ID
     */
    Optional<Invoice> getInvoiceById(Long invoiceId);

    /**
     * Get invoice by number
     */
    Optional<Invoice> getInvoiceByNumber(String invoiceNumber);

    /**
     * Update invoice
     */
    Invoice updateInvoice(Long invoiceId, Invoice invoice);

    /**
     * Delete draft invoice
     */
    void deleteInvoice(Long invoiceId);

    /**
     * Get all invoices in company
     */
    Page<Invoice> getInvoicesByCompany(Long companyId, Pageable pageable);

    /**
     * Get invoices by client
     */
    Page<Invoice> getInvoicesByClient(Long clientId, Pageable pageable);

    /**
     * Get invoices by status
     */
    Page<Invoice> getInvoicesByStatus(Long companyId, String status, Pageable pageable);

    // ==================== Invoice Status Management ====================

    /**
     * Send invoice to client
     */
    void sendInvoice(Long invoiceId, String emailAddress);

    /**
     * Mark invoice as sent
     */
    void markAsSent(Long invoiceId);

    /**
     * Mark invoice as paid
     */
    void markAsPaid(Long invoiceId, BigDecimal paidAmount);

    /**
     * Mark invoice as partially paid
     */
    void markAsPartiallyPaid(Long invoiceId, BigDecimal paidAmount);

    /**
     * Mark invoice as overdue
     */
    void markAsOverdue(Long invoiceId);

    /**
     * Cancel invoice
     */
    void cancelInvoice(Long invoiceId, String reason);

    /**
     * Check invoice status
     */
    Optional<String> getInvoiceStatus(Long invoiceId);

    // ==================== Invoice Calculation ====================

    /**
     * Calculate invoice total
     */
    InvoiceCalculation calculateInvoiceTotal(Long invoiceId);

    /**
     * Calculate taxes
     */
    BigDecimal calculateTaxes(Long invoiceId);

    /**
     * Calculate subtotal
     */
    BigDecimal calculateSubtotal(Long invoiceId);

    /**
     * Apply discount
     */
    void applyDiscount(Long invoiceId, BigDecimal discountAmount, String reason);

    /**
     * Remove discount
     */
    void removeDiscount(Long invoiceId);

    /**
     * Add line item
     */
    void addLineItem(Long invoiceId, InvoiceLineItem item);

    /**
     * Remove line item
     */
    void removeLineItem(Long invoiceId, Long lineItemId);

    /**
     * Get invoice line items
     */
    List<InvoiceLineItem> getLineItems(Long invoiceId);

    // ==================== Payment Management ====================

    /**
     * Record payment
     */
    void recordPayment(Long invoiceId, BigDecimal amount, String paymentMethod, String reference);

    /**
     * Get payment history
     */
    List<PaymentRecord> getPaymentHistory(Long invoiceId);

    /**
     * Get outstanding balance
     */
    BigDecimal getOutstandingBalance(Long invoiceId);

    /**
     * Calculate partial payment remaining
     */
    BigDecimal getPartialPaymentRemaining(Long invoiceId);

    /**
     * Apply credit note
     */
    void applyCreditNote(Long invoiceId, Long creditNoteId);

    /**
     * Generate credit note
     */
    Long generateCreditNote(Long invoiceId, String reason);

    /**
     * Refund payment
     */
    void refundPayment(Long invoiceId, BigDecimal amount);

    // ==================== Invoice Terms & Conditions ====================

    /**
     * Set payment terms
     */
    void setPaymentTerms(Long invoiceId, int daysUntilDue);

    /**
     * Get payment due date
     */
    Optional<Long> getPaymentDueDate(Long invoiceId);

    /**
     * Calculate late fees
     */
    BigDecimal calculateLateFees(Long invoiceId);

    /**
     * Is invoice overdue
     */
    boolean isOverdue(Long invoiceId);

    /**
     * Get days overdue
     */
    Integer getDaysOverdue(Long invoiceId);

    // ==================== Reporting & Analytics ====================

    /**
     * Get revenue by period
     */
    BigDecimal getRevenue(Long companyId, Long fromDate, Long toDate);

    /**
     * Get outstanding invoices
     */
    List<OutstandingInvoice> getOutstandingInvoices(Long companyId);

    /**
     * Get overdue invoices
     */
    List<OverdueInvoice> getOverdueInvoices(Long companyId);

    /**
     * Get payment summary
     */
    PaymentSummary getPaymentSummary(Long companyId, Long fromDate, Long toDate);

    /**
     * Get client invoice history
     */
    List<Invoice> getClientInvoiceHistory(Long clientId);

    /**
     * Get client total outstanding
     */
    BigDecimal getClientTotalOutstanding(Long clientId);

    /**
     * Get top clients by revenue
     */
    List<TopClient> getTopClients(Long companyId, int limit);

    /**
     * Get revenue trend
     */
    List<RevenueTrend> getRevenueTrend(Long companyId, Long fromDate, Long toDate);

    /**
     * Get invoice aging report
     */
    InvoiceAgingReport getAgingReport(Long companyId);

    /**
     * Get accounts receivable
     */
    AccountsReceivable getAccountsReceivable(Long companyId);

    // ==================== Invoice Documents ====================

    /**
     * Generate invoice PDF
     */
    byte[] generateInvoicePDF(Long invoiceId);

    /**
     * Send invoice reminder
     */
    void sendPaymentReminder(Long invoiceId);

    /**
     * Send overdue notice
     */
    void sendOverdueNotice(Long invoiceId);

    /**
     * Send duplicate invoice
     */
    void sendDuplicateInvoice(Long invoiceId);

    // ==================== Bulk Operations ====================

    /**
     * Bulk send invoices
     */
    void bulkSendInvoices(List<Long> invoiceIds);

    /**
     * Bulk mark as paid
     */
    void bulkMarkAsPaid(List<Long> invoiceIds);

    /**
     * Export invoices to CSV
     */
    byte[] exportInvoicesToCSV(Long companyId, Long fromDate, Long toDate);

    /**
     * Generate invoice report
     */
    String generateInvoiceReport(Long companyId, Long fromDate, Long toDate);

    // Data Transfer Objects

    record InvoiceCalculation(
            Long invoiceId,
            BigDecimal subtotal,
            BigDecimal tax,
            BigDecimal discount,
            BigDecimal total,
            BigDecimal paidAmount,
            BigDecimal balanceDue
    ) {}

    record InvoiceLineItem(
            Long lineItemId,
            String description,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {}

    record PaymentRecord(
            Long paymentId,
            Long invoiceId,
            BigDecimal amount,
            String paymentMethod,
            Long paymentDate,
            String reference,
            String status
    ) {}

    record OutstandingInvoice(
            Long invoiceId,
            String invoiceNumber,
            Long clientId,
            String clientName,
            BigDecimal amount,
            Long dueDate
    ) {}

    record OverdueInvoice(
            Long invoiceId,
            String invoiceNumber,
            Long clientId,
            String clientName,
            BigDecimal amount,
            int daysOverdue,
            BigDecimal lateFees
    ) {}

    record PaymentSummary(
            Long companyId,
            BigDecimal totalInvoiced,
            BigDecimal totalPaid,
            BigDecimal totalOutstanding,
            Double paymentRate,
            int invoiceCount
    ) {}

    record TopClient(
            Long clientId,
            String clientName,
            BigDecimal totalInvoiced,
            BigDecimal totalPaid,
            int invoiceCount
    ) {}

    record RevenueTrend(
            Long date,
            BigDecimal revenue,
            int invoiceCount,
            BigDecimal avgInvoiceValue
    ) {}

    record InvoiceAgingReport(
            Long companyId,
            BigDecimal current,
            BigDecimal thirtyDays,
            BigDecimal sixtyDays,
            BigDecimal ninetyDays,
            BigDecimal ninetyPlusDays
    ) {}

    record AccountsReceivable(
            Long companyId,
            BigDecimal totalOutstanding,
            BigDecimal overdue,
            int overdueInvoiceCount,
            Double daysPayableOutstanding
    ) {}
}
