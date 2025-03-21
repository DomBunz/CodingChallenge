package com.insurance.premium.common.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.premium.common.domain.SystemConfiguration;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ConfigurationServiceIntegrationTest {

    @Autowired
    private ConfigurationService configurationService;
    
    @Test
    void getBasePremium_ReturnsDefaultValueWhenNotSet() {
        // Act
        BigDecimal basePremium = configurationService.getBasePremium();
        
        // Assert
        assertEquals(new BigDecimal("500.00"), basePremium);
    }
    
    @Test
    void updateBasePremium_PersistsNewValueAndCanBeRetrieved() {
        // Arrange
        BigDecimal newBasePremium = new BigDecimal("750.00");
        
        // Act
        SystemConfiguration updated = configurationService.updateBasePremium(newBasePremium);
        BigDecimal retrievedBasePremium = configurationService.getBasePremium();
        
        // Assert
        assertNotNull(updated);
        assertEquals(newBasePremium.toString(), updated.getValue());
        assertEquals(newBasePremium, retrievedBasePremium);
    }
    
    @Test
    void getConfigValue_WithNonExistentKey_ReturnsDefaultValue() {
        // Act
        String value = configurationService.getConfigValue("NON_EXISTENT_KEY", "default");
        
        // Assert
        assertEquals("default", value);
    }
    
    @Test
    void updateConfiguration_CreatesNewConfigurationAndRetrievesIt() {
        // Arrange
        String key = "TEST_CONFIG";
        String value = "test_value";
        String description = "Test configuration";
        
        // Act
        SystemConfiguration created = configurationService.updateConfiguration(key, value, description);
        String retrievedValue = configurationService.getConfigValue(key, "default");
        
        // Assert
        assertNotNull(created);
        assertEquals(key, created.getKey());
        assertEquals(value, created.getValue());
        assertEquals(description, created.getDescription());
        assertEquals(value, retrievedValue);
    }
}
