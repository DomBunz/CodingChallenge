package com.insurance.premium.common.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.insurance.premium.common.domain.SystemConfiguration;

@DataJpaTest
@ActiveProfiles("test")
class SystemConfigurationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SystemConfigurationRepository repository;

    @Test
    void findByKey_WithExistingKey_ReturnsConfiguration() {
        // Arrange
        SystemConfiguration config = new SystemConfiguration();
        config.setKey("TEST_KEY");
        config.setValue("test_value");
        config.setDescription("Test description");
        config.setLastModified(LocalDateTime.now());
        
        entityManager.persist(config);
        entityManager.flush();
        
        // Act
        Optional<SystemConfiguration> found = repository.findByKey("TEST_KEY");
        
        // Assert
        assertTrue(found.isPresent());
        assertEquals("TEST_KEY", found.get().getKey());
        assertEquals("test_value", found.get().getValue());
    }
    
    @Test
    void findByKey_WithNonExistentKey_ReturnsEmptyOptional() {
        // Act
        Optional<SystemConfiguration> found = repository.findByKey("NON_EXISTENT_KEY");
        
        // Assert
        assertFalse(found.isPresent());
    }
    
    @Test
    void save_NewConfiguration_PersistsToDatabase() {
        // Arrange
        SystemConfiguration config = new SystemConfiguration();
        config.setKey("NEW_KEY");
        config.setValue("new_value");
        config.setDescription("New configuration");
        config.setLastModified(LocalDateTime.now());
        
        // Act
        SystemConfiguration saved = repository.save(config);
        
        // Assert
        assertNotNull(saved.getId());
        
        SystemConfiguration found = entityManager.find(SystemConfiguration.class, saved.getId());
        assertNotNull(found);
        assertEquals("NEW_KEY", found.getKey());
        assertEquals("new_value", found.getValue());
    }
}
