package com.DevCast.Fleet_Management.repository;

import com.DevCast.Fleet_Management.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByCompanyId(Long companyId);
    List<Client> findByCompanyIdAndNameContainingIgnoreCase(Long companyId, String name);
}
