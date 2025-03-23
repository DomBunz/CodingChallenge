package com.insurance.premium.calculation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    /**
     * Find regions by postal code prefix
     * 
     * @param prefix The postal code prefix
     * @return List of regions with postal codes starting with the given prefix
     */
    @Query("SELECT r FROM Region r WHERE r.postalCode LIKE :prefix%")
    List<Region> findByPostalCodeStartingWith(@Param("prefix") String prefix);
}
