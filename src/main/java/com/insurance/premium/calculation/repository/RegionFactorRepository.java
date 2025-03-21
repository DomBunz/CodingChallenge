package com.insurance.premium.calculation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.insurance.premium.calculation.domain.RegionFactor;

@Repository
public interface RegionFactorRepository extends JpaRepository<RegionFactor, Long> {
    
    /**
     * Find the default region factor (the one with null or empty federal state)
     * 
     * @return Optional containing the default region factor if found
     */
    @Query("SELECT rf FROM RegionFactor rf WHERE rf.federalState = 'DEFAULT'")
    Optional<RegionFactor> findDefaultRegionFactor();
    
    // findAll() method is inherited from JpaRepository
}
