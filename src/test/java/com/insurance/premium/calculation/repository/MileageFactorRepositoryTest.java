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

import com.insurance.premium.calculation.domain.MileageFactor;

@DataJpaTest
@ActiveProfiles("test")
class MileageFactorRepositoryTest {

    @Autowired
    private MileageFactorRepository mileageFactorRepository;
    
    @BeforeEach
    void setUp() {
        mileageFactorRepository.deleteAll();
    }
    
    @Test
    void findByMileage_ShouldReturnCorrectFactor_ForMileageInRange() {
        // Arrange
        MileageFactor factor1 = new MileageFactor();
        factor1.setMinMileage(0);
        factor1.setMaxMileage(5000);
        factor1.setFactor(new BigDecimal("0.8"));
        
        MileageFactor factor2 = new MileageFactor();
        factor2.setMinMileage(5001);
        factor2.setMaxMileage(15000);
        factor2.setFactor(new BigDecimal("1.0"));
        
        MileageFactor factor3 = new MileageFactor();
        factor3.setMinMileage(15001);
        factor3.setMaxMileage(30000);
        factor3.setFactor(new BigDecimal("1.2"));
        
        MileageFactor factor4 = new MileageFactor();
        factor4.setMinMileage(30001);
        factor4.setMaxMileage(null); // Unlimited
        factor4.setFactor(new BigDecimal("1.5"));
        
        mileageFactorRepository.saveAll(List.of(factor1, factor2, factor3, factor4));
        
        // Act & Assert
        // Test lower range
        Optional<MileageFactor> result1 = mileageFactorRepository.findByMileage(3000);
        assertTrue(result1.isPresent());
        assertEquals(0, new BigDecimal("0.8").compareTo(result1.get().getFactor()));
        
        // Test middle range
        Optional<MileageFactor> result2 = mileageFactorRepository.findByMileage(10000);
        assertTrue(result2.isPresent());
        assertEquals(0, new BigDecimal("1.0").compareTo(result2.get().getFactor()));
        
        // Test upper range
        Optional<MileageFactor> result3 = mileageFactorRepository.findByMileage(20000);
        assertTrue(result3.isPresent());
        assertEquals(0, new BigDecimal("1.2").compareTo(result3.get().getFactor()));
        
        // Test unlimited upper range
        Optional<MileageFactor> result4 = mileageFactorRepository.findByMileage(50000);
        assertTrue(result4.isPresent());
        assertEquals(0, new BigDecimal("1.5").compareTo(result4.get().getFactor()));
    }
    
    @Test
    void findByMileage_ShouldReturnEmpty_WhenNoFactorMatches() {
        // Arrange
        MileageFactor factor = new MileageFactor();
        factor.setMinMileage(5000);
        factor.setMaxMileage(10000);
        factor.setFactor(new BigDecimal("1.0"));
        mileageFactorRepository.save(factor);
        
        // Act
        Optional<MileageFactor> result = mileageFactorRepository.findByMileage(2000);
        
        // Assert
        assertTrue(result.isEmpty());
    }
    
    @Test
    void findByMileage_ShouldReturnHighestMinMileage_WhenMultipleFactorsMatch() {
        // This test ensures the ORDER BY in the query works correctly
        
        // Arrange
        MileageFactor factor1 = new MileageFactor();
        factor1.setMinMileage(0);
        factor1.setMaxMileage(20000);
        factor1.setFactor(new BigDecimal("0.9"));
        
        MileageFactor factor2 = new MileageFactor();
        factor2.setMinMileage(10000);
        factor2.setMaxMileage(30000);
        factor2.setFactor(new BigDecimal("1.2"));
        
        mileageFactorRepository.saveAll(List.of(factor1, factor2));
        
        // Act - A mileage of 15000 matches both factors
        // We need to limit the result to 1 since the query returns multiple matches
        Optional<MileageFactor> result = mileageFactorRepository.findByMileage(15000);
        
        // Assert - Should return the one with higher min mileage (factor2)
        assertTrue(result.isPresent());
        assertEquals(0, new BigDecimal("1.2").compareTo(result.get().getFactor()));
        assertEquals(10000, result.get().getMinMileage());
    }
    
    @Test
    void findAllByOrderByMinMileageAsc_ShouldReturnFactorsInAscendingOrder() {
        // Arrange
        MileageFactor factor1 = new MileageFactor();
        factor1.setMinMileage(20000);
        factor1.setMaxMileage(30000);
        factor1.setFactor(new BigDecimal("1.2"));
        
        MileageFactor factor2 = new MileageFactor();
        factor2.setMinMileage(0);
        factor2.setMaxMileage(5000);
        factor2.setFactor(new BigDecimal("0.8"));
        
        MileageFactor factor3 = new MileageFactor();
        factor3.setMinMileage(5001);
        factor3.setMaxMileage(20000);
        factor3.setFactor(new BigDecimal("1.0"));
        
        // Save in random order
        mileageFactorRepository.saveAll(List.of(factor1, factor2, factor3));
        
        // Act
        List<MileageFactor> orderedFactors = mileageFactorRepository.findAllByOrderByMinMileageAsc();
        
        // Assert
        assertEquals(3, orderedFactors.size());
        assertEquals(0, orderedFactors.get(0).getMinMileage());
        assertEquals(5001, orderedFactors.get(1).getMinMileage());
        assertEquals(20000, orderedFactors.get(2).getMinMileage());
    }
}
