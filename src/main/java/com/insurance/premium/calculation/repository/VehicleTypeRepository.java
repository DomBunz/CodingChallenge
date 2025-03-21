package com.insurance.premium.calculation.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.insurance.premium.calculation.domain.VehicleType;

@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, Long> {
    
    /**
     * Find a vehicle type by its name
     * 
     * @param name The vehicle type name
     * @return Optional containing the vehicle type if found
     */
    Optional<VehicleType> findByName(String name);
}
