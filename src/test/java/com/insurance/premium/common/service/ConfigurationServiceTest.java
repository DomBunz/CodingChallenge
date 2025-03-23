package com.insurance.premium.common.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.insurance.premium.common.domain.SystemConfiguration;
import com.insurance.premium.common.repository.SystemConfigurationRepository;

@ExtendWith(MockitoExtension.class)
class ConfigurationServiceTest {

    @Mock
    private SystemConfigurationRepository configurationRepository;
    
    @InjectMocks
    @Spy
    private ConfigurationService configurationService;
    
    @BeforeEach
    void setUp() {
        // Set up the self reference for the service
        ReflectionTestUtils.setField(configurationService, "self", configurationService);
    }
    
    @Test
    void getBasePremium_WithExistingConfig_ReturnsCorrectValue() {
        // Arrange
        String configKey = "BASE_PREMIUM";
        String configValue = "750.00";
        
        SystemConfiguration config = new SystemConfiguration();
        config.setKey(configKey);
        config.setValue(configValue);
        
        when(configurationRepository.findByKey(configKey)).thenReturn(Optional.of(config));
        
        // Act
        BigDecimal result = configurationService.getBasePremium();
        
        // Assert
        assertEquals(new BigDecimal(configValue), result);
        verify(configurationRepository).findByKey(configKey);
    }
    
    @Test
    void getBasePremium_WithNoConfig_ReturnsDefaultValue() {
        // Arrange
        String configKey = "BASE_PREMIUM";
        when(configurationRepository.findByKey(configKey)).thenReturn(Optional.empty());
        
        // Act
        BigDecimal result = configurationService.getBasePremium();
        
        // Assert
        assertEquals(new BigDecimal("500.00"), result);
        verify(configurationRepository).findByKey(configKey);
    }
    
    @Test
    void getConfigValue_WithExistingConfig_ReturnsValue() {
        // Arrange
        String configKey = "BASE_PREMIUM";
        String configValue = "test_value";
        String defaultValue = "default_value";
        
        SystemConfiguration config = new SystemConfiguration();
        config.setKey(configKey);
        config.setValue(configValue);
        
        when(configurationRepository.findByKey(configKey)).thenReturn(Optional.of(config));
        
        // Act
        String result = configurationService.getConfigValue(configKey, defaultValue);
        
        // Assert
        assertEquals(configValue, result);
        verify(configurationRepository).findByKey(configKey);
    }
    
    @Test
    void getConfigValue_WithNoConfig_ReturnsDefaultValue() {
        // Arrange
        String configKey = "BASE_PREMIUM";
        String defaultValue = "default_value";
        
        when(configurationRepository.findByKey(configKey)).thenReturn(Optional.empty());
        
        // Act
        String result = configurationService.getConfigValue(configKey, defaultValue);
        
        // Assert
        assertEquals(defaultValue, result);
        verify(configurationRepository).findByKey(configKey);
    }
    
    @Test
    void updateConfiguration_WithNewConfig_CreatesNewEntity() {
        // Arrange
        String configKey = "BASE_PREMIUM";
        String configValue = "new_value";
        String description = "Test description";
        
        when(configurationRepository.findByKey(configKey)).thenReturn(Optional.empty());
        
        SystemConfiguration savedConfig = new SystemConfiguration();
        savedConfig.setId(1L);
        savedConfig.setKey(configKey);
        savedConfig.setValue(configValue);
        savedConfig.setDescription(description);
        savedConfig.setLastModified(LocalDateTime.now());
        
        when(configurationRepository.save(any(SystemConfiguration.class))).thenReturn(savedConfig);
        
        // Act
        SystemConfiguration result = configurationService.updateConfiguration(configKey, configValue, description);
        
        // Assert
        assertNotNull(result);
        assertEquals(configKey, result.getKey());
        assertEquals(configValue, result.getValue());
        assertEquals(description, result.getDescription());
        verify(configurationRepository).findByKey(configKey);
        verify(configurationRepository).save(any(SystemConfiguration.class));
    }
    
    @Test
    void updateConfiguration_WithExistingConfig_UpdatesEntity() {
        // Arrange
        String configKey = "BASE_PREMIUM";
        String oldValue = "old_value";
        String newValue = "new_value";
        String description = "Test description";
        
        SystemConfiguration existingConfig = new SystemConfiguration();
        existingConfig.setId(1L);
        existingConfig.setKey(configKey);
        existingConfig.setValue(oldValue);
        existingConfig.setDescription(description);
        existingConfig.setLastModified(LocalDateTime.now().minusDays(1));
        
        when(configurationRepository.findByKey(configKey)).thenReturn(Optional.of(existingConfig));
        
        SystemConfiguration savedConfig = new SystemConfiguration();
        savedConfig.setId(1L);
        savedConfig.setKey(configKey);
        savedConfig.setValue(newValue);
        savedConfig.setDescription(description);
        savedConfig.setLastModified(LocalDateTime.now());
        
        when(configurationRepository.save(any(SystemConfiguration.class))).thenReturn(savedConfig);
        
        // Act
        SystemConfiguration result = configurationService.updateConfiguration(configKey, newValue, description);
        
        // Assert
        assertNotNull(result);
        assertEquals(configKey, result.getKey());
        assertEquals(newValue, result.getValue());
        assertEquals(description, result.getDescription());
        verify(configurationRepository).findByKey(configKey);
        verify(configurationRepository).save(any(SystemConfiguration.class));
    }
    
    @Test
    void updateBasePremium_CallsUpdateConfiguration() {
        // Arrange
        BigDecimal newBasePremium = new BigDecimal("800.00");
        
        SystemConfiguration savedConfig = new SystemConfiguration();
        savedConfig.setId(1L);
        savedConfig.setKey("BASE_PREMIUM");
        savedConfig.setValue(newBasePremium.toString());
        
        doReturn(savedConfig).when(configurationService).updateConfiguration(
            eq("BASE_PREMIUM"), 
            eq(newBasePremium.toString()), 
            anyString()
        );
        
        // Act
        SystemConfiguration result = configurationService.updateBasePremium(newBasePremium);
        
        // Assert
        assertNotNull(result);
        assertEquals("BASE_PREMIUM", result.getKey());
        assertEquals(newBasePremium.toString(), result.getValue());
        verify(configurationService).updateConfiguration(
            eq("BASE_PREMIUM"), 
            eq(newBasePremium.toString()), 
            anyString()
        );
    }
}
