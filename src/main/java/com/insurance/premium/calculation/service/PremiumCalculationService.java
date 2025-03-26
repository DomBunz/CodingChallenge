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
import org.springframework.validation.annotation.Validated;

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

import jakarta.validation.Valid;

@Service
@Validated
public class PremiumCalculationService {
    
    private static final Logger logger = LoggerFactory.getLogger(PremiumCalculationService.class);
    
    // Logging message constants
    private static final String LOG_FACTOR_NOT_FOUND = "{} not found for {}={}";
    
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
    public PremiumCalculationResult calculatePremium(@Valid PremiumCalculationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        logger.debug("Processing premium calculation request: {}", request);
        
        // Get base premium from configuration
        BigDecimal basePremium = configService.getBasePremium();
        
        // Get region factor
        RegionFactor regionFactor = findRegionFactor(request.postalCode());
        
        // Get vehicle type factor
        BigDecimal vehicleFactor = findVehicleTypeFactor(request.vehicleType());
        
        // Get mileage factor
        BigDecimal mileageFactor = findMileageFactor(request.annualMileage());
        
        // Calculate premium
        BigDecimal calculatedPremium = basePremium
                .multiply(regionFactor.getFactor())
                .multiply(vehicleFactor)
                .multiply(mileageFactor)
                .setScale(2, RoundingMode.HALF_UP);
        
        logger.info("Calculated premium={} [basePremium={}, regionFactor={}, vehicleFactor={}, mileageFactor={}] for postalCode={}, vehicleType={}, annualMileage={}",
                calculatedPremium, basePremium, regionFactor.getFactor(), vehicleFactor, mileageFactor,
                request.postalCode(), request.vehicleType(), request.annualMileage());
        
        return new PremiumCalculationResult(
                request.postalCode(), 
                request.vehicleType(), 
                request.annualMileage(),
                basePremium, 
                mileageFactor, 
                vehicleFactor, 
                regionFactor.getFactor(), 
                calculatedPremium
        );
    }
    
    /**
     * Find the region factor for a postal code
     * 
     * @param postalCode The postal code
     * @return The region factor
     */
    private RegionFactor findRegionFactor(String postalCode) {
        logger.debug("Finding region factor for postalCode={}", postalCode);
        
        List<Region> regions = regionRepository.findByPostalCode(postalCode);
        // if exactly one region is found or all regions have the same federal state
        if (regions.size() == 1 || regions.stream().map(Region::getFederalState).collect(Collectors.toSet()).size() == 1) {
            Region region = regions.get(0);
            RegionFactor regionFactor = region.getRegionFactor();
            logger.debug("Found region factor={} for postalCode={} (state={})", 
                    regionFactor.getFactor(), postalCode, region.getFederalState());
            return regionFactor;
        }
        
        logger.warn(LOG_FACTOR_NOT_FOUND, "Region factor", "postalCode", postalCode);
        throw new IllegalArgumentException("No region factor found for postal code: " + postalCode);
    }
    
    /**
     * Find the vehicle type factor for a vehicle type
     * 
     * @param vehicleTypeName The vehicle type name
     * @return The vehicle type factor
     */
    private BigDecimal findVehicleTypeFactor(String vehicleTypeName) {
        logger.debug("Finding vehicle factor for vehicleType={}", vehicleTypeName);
        
        Optional<VehicleType> vehicleTypeOpt = vehicleTypeRepository.findByName(vehicleTypeName);
        if (vehicleTypeOpt.isPresent()) {
            VehicleType vehicleType = vehicleTypeOpt.get();
            logger.debug("Found vehicle factor={} for vehicleType={}", 
                    vehicleType.getFactor(), vehicleTypeName);
            return vehicleType.getFactor();
        }
        
        logger.warn(LOG_FACTOR_NOT_FOUND, "Vehicle type", "name", vehicleTypeName);
        throw new IllegalArgumentException("Unknown vehicle type: " + vehicleTypeName);
    }
    
    /**
     * Find the mileage factor for an annual mileage
     * 
     * @param annualMileage The annual mileage
     * @return The mileage factor
     */
    private BigDecimal findMileageFactor(int annualMileage) {
        logger.debug("Finding mileage factor for annualMileage={}", annualMileage);
        
        Optional<MileageFactor> factor = mileageFactorRepository.findByMileage(annualMileage);
        if (factor.isPresent()) {
            MileageFactor mileageFactor = factor.get();
            logger.debug("Found mileage factor={} for annualMileage={} (range: {}-{})", 
                    mileageFactor.getFactor(), annualMileage, mileageFactor.getMinMileage(), mileageFactor.getMaxMileage());
            return mileageFactor.getFactor();
        }
        
        logger.warn(LOG_FACTOR_NOT_FOUND, "Mileage factor", "annualMileage", annualMileage);
        throw new IllegalArgumentException("No mileage factor found for mileage: " + annualMileage);
    }
}
