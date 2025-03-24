package com.insurance.premium.calculation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.premium.calculation.domain.MileageFactor;
import com.insurance.premium.calculation.domain.RegionFactor;
import com.insurance.premium.calculation.domain.VehicleType;
import com.insurance.premium.calculation.dto.MileageFactorRequest;
import com.insurance.premium.calculation.dto.RegionFactorRequest;
import com.insurance.premium.calculation.dto.VehicleTypeRequest;
import com.insurance.premium.calculation.repository.MileageFactorRepository;
import com.insurance.premium.calculation.repository.RegionFactorRepository;
import com.insurance.premium.calculation.repository.VehicleTypeRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing premium calculation factors.
 * The data is not checked for correctness, for example:
 *  - leaving gaps in mileage factors
 *  - creating mileage factors with overlapping ranges
 *  - creating mileage factors with invalid ranges
 */
@Service
public class FactorManagementService {
    
    private static final Logger logger = LoggerFactory.getLogger(FactorManagementService.class);
    
    private final RegionFactorRepository regionFactorRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final MileageFactorRepository mileageFactorRepository;
    
    public FactorManagementService(
            RegionFactorRepository regionFactorRepository,
            VehicleTypeRepository vehicleTypeRepository,
            MileageFactorRepository mileageFactorRepository) {
        this.regionFactorRepository = regionFactorRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.mileageFactorRepository = mileageFactorRepository;
    }
    
    // Region Factor methods
    
    /**
     * Get all region factors
     * 
     * @return List of all region factors
     */
    @Transactional(readOnly = true)
    public List<RegionFactor> getAllRegionFactors() {
        return regionFactorRepository.findAll();
    }
    
    /**
     * Get a region factor by ID
     * 
     * @param id The region factor ID
     * @return Optional containing the region factor if found
     */
    @Transactional(readOnly = true)
    public Optional<RegionFactor> getRegionFactorById(Long id) {
        return regionFactorRepository.findById(id);
    }
    
    /**
     * Create a new region factor
     * 
     * @param request The region factor request
     * @return The created region factor
     */
    @Transactional
    public RegionFactor createRegionFactor(RegionFactorRequest request) {
        RegionFactor regionFactor = new RegionFactor();
        regionFactor.setFederalState(request.getFederalState());
        regionFactor.setFactor(request.getFactor());
        
        RegionFactor savedFactor = regionFactorRepository.save(regionFactor);
        logger.info("Created region factor: {}", savedFactor);
        return savedFactor;
    }
    
    /**
     * Update an existing region factor
     * 
     * @param id The region factor ID
     * @param request The updated region factor data
     * @return The updated region factor
     * @throws IllegalArgumentException if the region factor is not found
     */
    @Transactional
    public RegionFactor updateRegionFactor(Long id, RegionFactorRequest request) {
        RegionFactor regionFactor = regionFactorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Region factor not found with ID: " + id));
        
        regionFactor.setFederalState(request.getFederalState());
        regionFactor.setFactor(request.getFactor());
        
        RegionFactor updatedFactor = regionFactorRepository.save(regionFactor);
        logger.info("Updated region factor: {}", updatedFactor);
        return updatedFactor;
    }
    
    /**
     * Delete a region factor
     * 
     * @param id The region factor ID
     * @throws IllegalArgumentException if the region factor is not found
     */
    @Transactional
    public void deleteRegionFactor(Long id) {
        if (!regionFactorRepository.existsById(id)) {
            throw new IllegalArgumentException("Region factor not found with ID: " + id);
        }
        
        regionFactorRepository.deleteById(id);
        logger.info("Deleted region factor with ID: {}", id);
    }
    
    // Vehicle Type methods
    
    /**
     * Get all vehicle types
     * 
     * @return List of all vehicle types
     */
    @Transactional(readOnly = true)
    public List<VehicleType> getAllVehicleTypes() {
        return vehicleTypeRepository.findAll();
    }
    
    /**
     * Get a vehicle type by ID
     * 
     * @param id The vehicle type ID
     * @return Optional containing the vehicle type if found
     */
    @Transactional(readOnly = true)
    public Optional<VehicleType> getVehicleTypeById(Long id) {
        return vehicleTypeRepository.findById(id);
    }
    
    /**
     * Create a new vehicle type
     * 
     * @param request The vehicle type request
     * @return The created vehicle type
     */
    @Transactional
    public VehicleType createVehicleType(VehicleTypeRequest request) {
        VehicleType vehicleType = new VehicleType();
        vehicleType.setName(request.getName());
        vehicleType.setFactor(request.getFactor());
        
        VehicleType savedType = vehicleTypeRepository.save(vehicleType);
        logger.info("Created vehicle type: {}", savedType);
        return savedType;
    }
    
    /**
     * Update an existing vehicle type
     * 
     * @param id The vehicle type ID
     * @param request The updated vehicle type data
     * @return The updated vehicle type
     * @throws IllegalArgumentException if the vehicle type is not found
     */
    @Transactional
    public VehicleType updateVehicleType(Long id, VehicleTypeRequest request) {
        VehicleType vehicleType = vehicleTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle type not found with ID: " + id));
        
        vehicleType.setName(request.getName());
        vehicleType.setFactor(request.getFactor());
        
        VehicleType updatedType = vehicleTypeRepository.save(vehicleType);
        logger.info("Updated vehicle type: {}", updatedType);
        return updatedType;
    }
    
    /**
     * Delete a vehicle type
     * 
     * @param id The vehicle type ID
     * @throws IllegalArgumentException if the vehicle type is not found
     */
    @Transactional
    public void deleteVehicleType(Long id) {
        if (!vehicleTypeRepository.existsById(id)) {
            throw new IllegalArgumentException("Vehicle type not found with ID: " + id);
        }
        
        vehicleTypeRepository.deleteById(id);
        logger.info("Deleted vehicle type with ID: {}", id);
    }
    
    // Mileage Factor methods
    
    /**
     * Get all mileage factors
     * 
     * @return List of all mileage factors
     */
    @Transactional(readOnly = true)
    public List<MileageFactor> getAllMileageFactors() {
        return mileageFactorRepository.findAllByOrderByMinMileageAsc();
    }
    
    /**
     * Get a mileage factor by ID
     * 
     * @param id The mileage factor ID
     * @return Optional containing the mileage factor if found
     */
    @Transactional(readOnly = true)
    public Optional<MileageFactor> getMileageFactorById(Long id) {
        return mileageFactorRepository.findById(id);
    }
    
    /**
     * Create a new mileage factor
     * 
     * @param request The mileage factor request
     * @return The created mileage factor
     */
    @Transactional
    public MileageFactor createMileageFactor(MileageFactorRequest request) {
        validateMileageFactorRequest(request);
        
        MileageFactor mileageFactor = new MileageFactor();
        mileageFactor.setMinMileage(request.getMinMileage());
        mileageFactor.setMaxMileage(request.getMaxMileage());
        mileageFactor.setFactor(request.getFactor());
        
        MileageFactor savedFactor = mileageFactorRepository.save(mileageFactor);
        logger.info("Created mileage factor: {}", savedFactor);
        return savedFactor;
    }
    
    /**
     * Update an existing mileage factor
     * 
     * @param id The mileage factor ID
     * @param request The updated mileage factor data
     * @return The updated mileage factor
     * @throws IllegalArgumentException if the mileage factor is not found
     */
    @Transactional
    public MileageFactor updateMileageFactor(Long id, MileageFactorRequest request) {
        validateMileageFactorRequest(request);
        
        MileageFactor mileageFactor = mileageFactorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mileage factor not found with ID: " + id));
        
        mileageFactor.setMinMileage(request.getMinMileage());
        mileageFactor.setMaxMileage(request.getMaxMileage());
        mileageFactor.setFactor(request.getFactor());
        
        MileageFactor updatedFactor = mileageFactorRepository.save(mileageFactor);
        logger.info("Updated mileage factor: {}", updatedFactor);
        return updatedFactor;
    }
    
    /**
     * Delete a mileage factor
     * 
     * @param id The mileage factor ID
     * @throws IllegalArgumentException if the mileage factor is not found
     */
    @Transactional
    public void deleteMileageFactor(Long id) {
        if (!mileageFactorRepository.existsById(id)) {
            throw new IllegalArgumentException("Mileage factor not found with ID: " + id);
        }
        
        mileageFactorRepository.deleteById(id);
        logger.info("Deleted mileage factor with ID: {}", id);
    }
    
    /**
     * Validate a mileage factor request
     * 
     * @param request The mileage factor request to validate
     * @throws IllegalArgumentException if the request is invalid
     */
    private void validateMileageFactorRequest(MileageFactorRequest request) {
        if (request.getMaxMileage() != null && request.getMinMileage() >= request.getMaxMileage()) {
            throw new IllegalArgumentException("Minimum mileage must be less than maximum mileage");
        }
    }
}
