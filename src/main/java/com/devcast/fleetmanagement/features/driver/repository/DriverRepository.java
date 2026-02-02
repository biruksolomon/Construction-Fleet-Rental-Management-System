package com.devcast.fleetmanagement.features.driver.repository;

import com.devcast.fleetmanagement.features.driver.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByLicenseNumber(String licenseNumber);
    List<Driver> findByCompanyId(Long companyId);
    List<Driver> findByCompanyIdAndStatus(Long companyId, Driver.DriverStatus status);
}
