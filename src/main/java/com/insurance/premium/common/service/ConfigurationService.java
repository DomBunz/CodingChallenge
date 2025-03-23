package com.insurance.premium.common.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.premium.common.domain.SystemConfiguration;
import com.insurance.premium.common.repository.SystemConfigurationRepository;

@Service
public class ConfigurationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);
    private static final String BASE_PREMIUM_KEY = "BASE_PREMIUM";
    
    // Set of allowed configuration keys
    private static final Set<String> ALLOWED_KEYS = Set.of(BASE_PREMIUM_KEY);
    
    private final SystemConfigurationRepository configurationRepository;
    
    // Self injection so transaction proxies are not bypassed by direct method calls
    @SuppressWarnings("java:S6813") // self injection
    @Autowired
    @Lazy
    private ConfigurationService self;
    
    public ConfigurationService(SystemConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }
    
    /**
     * Get the base premium amount for insurance calculations
     * 
     * @return The base premium as a BigDecimal
     * @throws RuntimeException if the base premium configuration is not found
     */
    @Transactional(readOnly = true)
    public BigDecimal getBasePremium() {
        return new BigDecimal(self.getConfigValue(BASE_PREMIUM_KEY, "500.00"));
    }
    
    /**
     * Update the base premium amount
     * 
     * @param basePremium The new base premium amount
     * @return The updated SystemConfiguration
     */
    @Transactional
    public SystemConfiguration updateBasePremium(BigDecimal basePremium) {
        return self.updateConfiguration(BASE_PREMIUM_KEY, basePremium.toString(), 
                "Base premium amount in EUR for insurance calculations");
    }
    
    /**
     * Get a configuration value by its key
     * 
     * @param key The configuration key
     * @param defaultValue The default value to return if the configuration is not found
     * @return The configuration value, or the default value if not found
     */
    @Transactional(readOnly = true)
    public String getConfigValue(String key, String defaultValue) {
        return configurationRepository.findByKey(key)
                .map(SystemConfiguration::getValue)
                .orElse(defaultValue);
    }
    
    /**
     * Get all system configurations
     * 
     * @return List of all configurations
     */
    @Transactional(readOnly = true)
    public List<SystemConfiguration> getAllConfigurations() {
        return configurationRepository.findAll();
    }
    
    /**
     * Get a configuration by its key
     * 
     * @param key The configuration key
     * @return Optional containing the configuration if found
     */
    @Transactional(readOnly = true)
    public Optional<SystemConfiguration> getConfigurationByKey(String key) {
        return configurationRepository.findByKey(key);
    }
    
    /**
     * Check if a configuration key is allowed
     * 
     * @param key The configuration key to check
     * @return true if the key is allowed, false otherwise
     */
    public boolean isKeyAllowed(String key) {
        return ALLOWED_KEYS.contains(key);
    }
    
    /**
     * Update a configuration value
     * 
     * @param key The configuration key
     * @param value The new value
     * @param description The configuration description (optional, only used when creating a new configuration)
     * @return The updated or created SystemConfiguration
     * @throws IllegalArgumentException if the key is not allowed
     */
    @Transactional
    public SystemConfiguration updateConfiguration(String key, String value, String description) {
        if (!isKeyAllowed(key)) {
            throw new IllegalArgumentException("Configuration key not allowed: " + key);
        }
        
        SystemConfiguration config = configurationRepository.findByKey(key)
                .orElse(new SystemConfiguration());
        
        if (config.getId() == null) {
            config.setKey(key);
            config.setDescription(description);
        }
        
        config.setValue(value);
        config.setLastModified(LocalDateTime.now());
        
        SystemConfiguration savedConfig = configurationRepository.save(config);
        logger.info("Updated configuration: {}", savedConfig);
        return savedConfig;
    }
}
