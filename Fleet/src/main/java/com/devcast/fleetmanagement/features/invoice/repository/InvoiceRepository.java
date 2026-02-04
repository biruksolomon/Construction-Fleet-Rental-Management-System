package com.devcast.fleetmanagement.features.invoice.repository;

import com.devcast.fleetmanagement.features.invoice.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findByCompanyId(Long companyId);
    List<Invoice> findByCompanyIdAndStatus(Long companyId, Invoice.InvoiceStatus status);
    List<Invoice> findByIssuedDateBetween(LocalDate startDate, LocalDate endDate);
}
