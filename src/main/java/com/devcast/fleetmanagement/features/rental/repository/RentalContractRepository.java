package com.devcast.fleetmanagement.features.rental.repository;

import com.devcast.fleetmanagement.features.rental.model.RentalContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentalContractRepository extends JpaRepository<RentalContract, Long> {
    Optional<RentalContract> findByContractNumber(String contractNumber);
    List<RentalContract> findByCompanyId(Long companyId);
    org.springframework.data.domain.Page<RentalContract> findByCompanyId(Long companyId, org.springframework.data.domain.Pageable pageable);
    List<RentalContract> findByClientId(Long clientId);
    List<RentalContract> findByCompanyIdAndStatus(Long companyId, RentalContract.ContractStatus status);
    org.springframework.data.domain.Page<RentalContract> findByCompanyIdAndStatus(Long companyId, RentalContract.ContractStatus status, org.springframework.data.domain.Pageable pageable);
    List<RentalContract> findByStartDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Count active rental contracts for a company
     */
    long countByCompanyIdAndStatus(Long companyId, RentalContract.ContractStatus status);

    /**
     * Count contracts for a company
     */
    long countByCompanyId(Long companyId);
}
