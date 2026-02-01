package com.DevCast.Fleet_Management.repository;

import com.DevCast.Fleet_Management.model.RentalContract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RentalContractRepository extends JpaRepository<RentalContract, Long> {
    Optional<RentalContract> findByContractNumber(String contractNumber);
    List<RentalContract> findByCompanyId(Long companyId);
    List<RentalContract> findByClientId(Long clientId);
    List<RentalContract> findByCompanyIdAndStatus(Long companyId, RentalContract.ContractStatus status);
    List<RentalContract> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
}
