package com.insurance.premium.common.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.premium.common.domain.SystemConfiguration;

@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, Long> {
    
    /**
     * Find a configuration by its key
     * 
     * @param key The configuration key
     * @return Optional containing the configuration if found
     */
    Optional<SystemConfiguration> findByKey(String key);
}
