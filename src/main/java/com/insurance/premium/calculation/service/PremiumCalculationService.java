package com.insurance.premium.calculation.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.premium.calculation.domain.MileageFactor;
import com.insurance.premium.calculation.domain.Region;
import com.insurance.premium.calculation.domain.RegionFactor;
import com.insurance.premium.calculation.domain.VehicleType;
import com.insurance.premium.calculation.dto.PremiumCalculationRequest;
import com.insurance.premium.calculation.dto.PremiumCalculationResult;
import com.insurance.premium.calculation.repository.MileageFactorRepository;
import com.insurance.premium.calculation.repository.RegionRepository;
import com.insurance.premium.calculation.repository.VehicleTypeRepository;
import com.insurance.premium.common.service.ConfigurationService;

@Service
public class PremiumCalculationService {
    
    private static final Logger logger = LoggerFactory.getLogger(PremiumCalculationService.class);
    
    private final RegionRepository regionRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final MileageFactorRepository mileageFactorRepository;
    private final ConfigurationService configService;
    
    public PremiumCalculationService(
            RegionRepository regionRepository,
            VehicleTypeRepository vehicleTypeRepository,
            MileageFactorRepository mileageFactorRepository,
            ConfigurationService configService) {
        this.regionRepository = regionRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.mileageFactorRepository = mileageFactorRepository;
        this.configService = configService;
    }
    
    /**
     * Calculate the premium based on the provided request
     * 
     * @param request The premium calculation request
     * @return The premium calculation result
     */
    @Transactional(readOnly = true)
    public PremiumCalculationResult calculatePremium(PremiumCalculationRequest request) {
        String err = null;
        if (request == null || (err = request.getValidationError()) != null) {
            throw new IllegalArgumentException("Invalid request: " + 
                    (request == null ? "Request is null" : err));
        }
        
        RegionFactor region = findRegionFactor(request.postalCode());
        BigDecimal basePremium = configService.getBasePremium(),
             vehicleTypeFactor = findVehicleTypeFactor(request.vehicleType()),
                 mileageFactor = findMileageFactor(request.annualMileage());
        
        BigDecimal premium = basePremium
                .multiply(region.getFactor())
                .multiply(vehicleTypeFactor)
                .multiply(mileageFactor)
                .setScale(2, RoundingMode.HALF_UP);
        
        logger.info("Calculated premium: {} (Base: {}, Region: {} x {}, Vehicle: {} x {}, Mileage: {} x {})",
                premium, basePremium, region.getFederalState(), region.getFactor(),
                request.vehicleType(), vehicleTypeFactor, request.annualMileage(), mileageFactor);
        
        return new PremiumCalculationResult(request.postalCode(), request.vehicleType(), request.annualMileage(),
                basePremium, mileageFactor, vehicleTypeFactor, region.getFactor(), premium);
    }
    
    /**
     * Find the vehicle type factor for a given vehicle type name
     * 
     * @param vehicleTypeName The vehicle type name
     * @return The vehicle type factor
     */
    private BigDecimal findVehicleTypeFactor(String vehicleTypeName) {
        Optional<VehicleType> vehicleType = vehicleTypeRepository.findByName(vehicleTypeName);
        if (vehicleType.isPresent()) {
            return vehicleType.get().getFactor();
        }
        throw new IllegalArgumentException("Unknown vehicle type: " + vehicleTypeName);
    }
    
    /**
     * Find the mileage factor for a given annual mileage
     * 
     * @param annualMileage The annual mileage
     * @return The mileage factor
     */
    private BigDecimal findMileageFactor(int annualMileage) {
        Optional<MileageFactor> factor = mileageFactorRepository.findByMileage(annualMileage);
        if (factor.isPresent()) {
            return factor.get().getFactor();
        }
        throw new IllegalArgumentException("No mileage factor found for mileage: " + annualMileage);
    }

    private RegionFactor findRegionFactor(String postalCode) {
        List<Region> regions = regionRepository.findByPostalCode(postalCode);
        // if exactly one region is found or all regions have the same federal state
        if (regions.size() == 1 || regions.stream().map(Region::getFederalState).collect(Collectors.toSet()).size() == 1) {
            return regions.get(0).getRegionFactor();
        }
        throw new IllegalArgumentException("No region factor found for postal code: " + postalCode);
    }
}
