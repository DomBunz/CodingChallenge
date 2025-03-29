package com.insurance.premium.calculation.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.insurance.premium.calculation.domain.MileageFactor;
import com.insurance.premium.calculation.domain.Region;
import com.insurance.premium.calculation.domain.RegionFactor;
import com.insurance.premium.calculation.domain.VehicleType;
import com.insurance.premium.calculation.dto.FactorResponse;
import com.insurance.premium.calculation.dto.PostcodeResponse;
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
     * Get all factors for premium calculation
     * 
     * @return Map of factor types to factor lists
     */
    @Transactional(readOnly = true)
    public Map<String, List<FactorResponse>> getAllFactors() {
        logger.debug("Getting all factors for premium calculation");
        Map<String, List<FactorResponse>> factors = new HashMap<>();
        factors.put("regionFactors", this.getAllRegionFactors());
        factors.put("vehicleTypeFactors", this.getAllVehicleFactors());
        factors.put("mileageFactors", this.getAllMileageFactors());
        return factors;
    }
    
    /**
     * Get all region factors
     * 
     * @return List of region factors
     */
    @Transactional(readOnly = true)
    public List<FactorResponse> getAllRegionFactors() {
        logger.debug("Getting all region factors");
        // Get unique region factors
        return regionRepository.findAll().stream()
                .map(Region::getRegionFactor)
                .distinct()
                .map(FactorResponse::fromRegionFactor)
                .toList();
    }
    
    /**
     * Get all vehicle type factors
     * 
     * @return List of vehicle type factors
     */
    @Transactional(readOnly = true)
    public List<FactorResponse> getAllVehicleFactors() {
        logger.debug("Getting all vehicle type factors");
        return vehicleTypeRepository.findAll().stream()
                .map(FactorResponse::fromVehicleType)
                .toList();
    }
    
    /**
     * Get all mileage factors
     * 
     * @return List of mileage factors
     */
    @Transactional(readOnly = true)
    public List<FactorResponse> getAllMileageFactors() {
        logger.debug("Getting all mileage factors");
        return mileageFactorRepository.findAllByOrderByMinMileageAsc().stream()
                .map(FactorResponse::fromMileageFactor)
                .toList();
    }
    
    /**
     * Get all postcodes with pagination
     * 
     * @param pageable Pagination information
     * @return Page of postcode responses
     */
    @Transactional(readOnly = true)
    public Page<PostcodeResponse> getAllPostcodes(Pageable pageable) {
        logger.debug("Getting all postcodes with pagination");
        return regionRepository.findAll(pageable)
                .map(PostcodeResponse::fromRegion);
    }
    
    /**
     * Search postcodes by prefix
     * 
     * @param prefix Postal code prefix
     * @return List of matching postcode responses
     */
    @Transactional(readOnly = true)
    public List<PostcodeResponse> getPostcodesByPrefix(String prefix) {
        logger.debug("Searching postcodes by prefix: {}", prefix);
        return regionRepository.findByPostalCodeStartingWith(prefix).stream()
                .map(PostcodeResponse::fromRegion)
                .toList();
    }
    
    /**
     * Search postcodes by area, city, or district containing the given search term
     * 
     * @param searchTerm The search term to look for in area, city, or district
     * @return List of matching postcode responses
     */
    @Transactional(readOnly = true)
    public List<PostcodeResponse> getPostcodesByAreaCityOrDistrict(String searchTerm) {
        logger.debug("Searching postcodes by area, city, or district containing: {}", searchTerm);
        return regionRepository.findByAreaCityOrDistrictContaining(searchTerm).stream()
                .map(PostcodeResponse::fromRegion)
                .toList();
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
