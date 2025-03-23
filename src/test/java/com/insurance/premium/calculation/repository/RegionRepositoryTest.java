package com.insurance.premium.calculation.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import com.insurance.premium.calculation.domain.Region;
import com.insurance.premium.calculation.domain.RegionFactor;

@DataJpaTest
@ActiveProfiles("test")
class RegionRepositoryTest {

    @Autowired
    private RegionRepository regionRepository;
    
    @Autowired
    private RegionFactorRepository regionFactorRepository;
    
    @Test
    void findByPostalCode_ShouldReturnRegion_WhenPostalCodeExists() {
        // Arrange
        RegionFactor regionFactor = new RegionFactor();
        regionFactor.setFederalState("Berlin");
        regionFactor.setFactor(new BigDecimal("1.2"));
        regionFactorRepository.save(regionFactor);
        
        Region region = new Region();
        region.setPostalCode("10115");
        region.setCity("Berlin");
        region.setFederalState("Berlin");
        region.setCountry("Germany");
        region.setRegionFactor(regionFactor);
        regionRepository.save(region);
        
        // Act
        List<Region> foundRegions = regionRepository.findByPostalCode("10115");
        
        // Assert
        assertFalse(foundRegions.isEmpty());
        assertEquals(1, foundRegions.size());
        assertEquals("10115", foundRegions.get(0).getPostalCode());
        assertEquals("Berlin", foundRegions.get(0).getCity());
        assertEquals("Berlin", foundRegions.get(0).getFederalState());
    }
    
    @Test
    void findByPostalCode_ShouldReturnEmptyList_WhenPostalCodeDoesNotExist() {
        // Act
        List<Region> foundRegions = regionRepository.findByPostalCode("99999");
        
        // Assert
        assertTrue(foundRegions.isEmpty());
    }
    
    @Test
    void findByPostalCodeStartingWith_ShouldReturnMatchingRegions() {
        // Arrange
        RegionFactor berlinFactor = new RegionFactor();
        berlinFactor.setFederalState("Berlin");
        berlinFactor.setFactor(new BigDecimal("1.2"));
        regionFactorRepository.save(berlinFactor);
        
        Region region1 = new Region();
        region1.setPostalCode("10115");
        region1.setCity("Berlin");
        region1.setFederalState("Berlin");
        region1.setCountry("Germany");
        region1.setRegionFactor(berlinFactor);
        
        Region region2 = new Region();
        region2.setPostalCode("10117");
        region2.setCity("Berlin");
        region2.setFederalState("Berlin");
        region2.setCountry("Germany");
        region2.setRegionFactor(berlinFactor);
        
        Region region3 = new Region();
        region3.setPostalCode("20095");
        region3.setCity("Hamburg");
        region3.setFederalState("Hamburg");
        region3.setCountry("Germany");
        region3.setRegionFactor(berlinFactor); // Using same factor for simplicity
        
        regionRepository.saveAll(List.of(region1, region2, region3));
        
        // Act
        List<Region> foundRegions = regionRepository.findByPostalCodeStartingWith("101");
        
        // Assert
        assertEquals(2, foundRegions.size());
        assertTrue(foundRegions.stream().anyMatch(r -> r.getPostalCode().equals("10115")));
        assertTrue(foundRegions.stream().anyMatch(r -> r.getPostalCode().equals("10117")));
    }
    
    @Test
    void findAll_WithPagination_ShouldReturnPagedResults() {
        // Arrange
        RegionFactor factor = new RegionFactor();
        factor.setFederalState("Test");
        factor.setFactor(new BigDecimal("1.0"));
        regionFactorRepository.save(factor);
        
        // Create 20 test regions
        for (int i = 0; i < 20; i++) {
            Region region = new Region();
            region.setPostalCode(String.format("%05d", i + 10000));
            region.setCity("City " + i);
            region.setFederalState("State " + (i % 5));
            region.setCountry("Germany");
            region.setRegionFactor(factor);
            regionRepository.save(region);
        }
        
        // Act - Get first page (10 items)
        Pageable firstPageable = PageRequest.of(0, 10, Sort.by("postalCode").ascending());
        Page<Region> firstPage = regionRepository.findAll(firstPageable);
        
        // Act - Get second page (10 items)
        Pageable secondPageable = PageRequest.of(1, 10, Sort.by("postalCode").ascending());
        Page<Region> secondPage = regionRepository.findAll(secondPageable);
        
        // Assert
        assertEquals(10, firstPage.getContent().size());
        assertEquals(10, secondPage.getContent().size());
        assertEquals(0, firstPage.getNumber());
        assertEquals(1, secondPage.getNumber());
        
        // Check that postal codes are sorted correctly
        assertEquals("10000", firstPage.getContent().get(0).getPostalCode());
        assertEquals("10009", firstPage.getContent().get(9).getPostalCode());
        assertEquals("10010", secondPage.getContent().get(0).getPostalCode());
    }
}
