package com.devcast.fleetmanagement.features.company.repository;

import com.devcast.fleetmanagement.features.company.model.CompanySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing company settings
 * Provides CRUD operations for company configuration settings
 */
@Repository
public interface CompanySettingRepository extends JpaRepository<CompanySetting, Long> {

    /**
     * Find all settings for a company
     */
    List<CompanySetting> findByCompanyId(Long companyId);

    /**
     * Find setting by company ID and key
     */
    Optional<CompanySetting> findByCompanyIdAndSettingKey(Long companyId, String settingKey);

    /**
     * Check if a setting exists for a company
     */
    boolean existsByCompanyIdAndSettingKey(Long companyId, String settingKey);

    /**
     * Delete setting by company ID and key
     */
    void deleteByCompanyIdAndSettingKey(Long companyId, String settingKey);

    /**
     * Delete all settings for a company
     */
    void deleteByCompanyId(Long companyId);

    /**
     * Find settings by data type
     */
    List<CompanySetting> findByCompanyIdAndDataType(Long companyId, CompanySetting.DataType dataType);
}
