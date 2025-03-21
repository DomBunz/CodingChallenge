package com.insurance.premium.calculation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.premium.calculation.domain.Region;

import java.util.List;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    
    /**
     * Find the regions by postal code
     * 
     * @param postalCode The postal code
     * @return List of regions
     */
    List<Region> findByPostalCode(String postalCode);
}
