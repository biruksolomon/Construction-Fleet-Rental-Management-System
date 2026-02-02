package com.devcast.fleetmanagement.features.vehicle.repository;

import com.devcast.fleetmanagement.features.vehicle.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByPlateNumber(String plateNumber);
    List<Vehicle> findByCompanyId(Long companyId);
    List<Vehicle> findByCompanyIdAndStatus(Long companyId, Vehicle.VehicleStatus status);
}
