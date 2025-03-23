package com.insurance.premium.calculation.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.insurance.premium.calculation.domain.VehicleType;

@DataJpaTest
@ActiveProfiles("test")
class VehicleTypeRepositoryTest {

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;
    
    @BeforeEach
    void setUp() {
        vehicleTypeRepository.deleteAll();
    }
    
    @Test
    void findByName_ShouldReturnVehicleType_WhenNameExists() {
        // Arrange
        VehicleType vehicleType = new VehicleType();
        vehicleType.setName("PKW");
        vehicleType.setFactor(new BigDecimal("1.0"));
        vehicleTypeRepository.save(vehicleType);
        
        // Act
        Optional<VehicleType> foundVehicleType = vehicleTypeRepository.findByName("PKW");
        
        // Assert
        assertTrue(foundVehicleType.isPresent());
        assertEquals("PKW", foundVehicleType.get().getName());
        assertEquals(0, new BigDecimal("1.0").compareTo(foundVehicleType.get().getFactor()));
    }
    
    @Test
    void findByName_ShouldReturnEmpty_WhenNameDoesNotExist() {
        // Act
        Optional<VehicleType> foundVehicleType = vehicleTypeRepository.findByName("NONEXISTENT");
        
        // Assert
        assertTrue(foundVehicleType.isEmpty());
    }
    
    @Test
    void findAll_ShouldReturnAllVehicleTypes() {
        // Arrange
        VehicleType type1 = new VehicleType();
        type1.setName("Kompaktklasse");
        type1.setFactor(new BigDecimal("1.0"));
        
        VehicleType type2 = new VehicleType();
        type2.setName("LKW");
        type2.setFactor(new BigDecimal("1.5"));
        
        VehicleType type3 = new VehicleType();
        type3.setName("Kleinwagen");
        type3.setFactor(new BigDecimal("0.8"));
        
        vehicleTypeRepository.saveAll(List.of(type1, type2, type3));
        
        // Act
        List<VehicleType> allTypes = vehicleTypeRepository.findAll();
        
        // Assert
        assertEquals(3, allTypes.size());
        
        // Check that all expected vehicle types are present
        boolean hasKompaktklasse = false;
        boolean hasLKW = false;
        boolean hasKleinwagen = false;
        
        for (VehicleType type : allTypes) {
            if (type.getName().equals("Kompaktklasse")) hasKompaktklasse = true;
            if (type.getName().equals("LKW")) hasLKW = true;
            if (type.getName().equals("Kleinwagen")) hasKleinwagen = true;
        }
        
        assertTrue(hasKompaktklasse);
        assertTrue(hasLKW);
        assertTrue(hasKleinwagen);
    }
    
    @Test
    void save_ShouldPersistVehicleType() {
        // Arrange
        VehicleType vehicleType = new VehicleType();
        vehicleType.setName("SUV");
        vehicleType.setFactor(new BigDecimal("1.2"));
        
        // Act
        VehicleType savedType = vehicleTypeRepository.save(vehicleType);
        
        // Assert
        assertNotNull(savedType.getId());
        
        // Verify it can be retrieved
        Optional<VehicleType> retrievedType = vehicleTypeRepository.findById(savedType.getId());
        assertTrue(retrievedType.isPresent());
        assertEquals("SUV", retrievedType.get().getName());
    }
}
