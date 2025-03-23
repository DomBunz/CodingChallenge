package com.insurance.premium.calculation.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.premium.application.dto.ErrorResponse;
import com.insurance.premium.calculation.domain.Region;
import com.insurance.premium.calculation.dto.FactorResponse;
import com.insurance.premium.calculation.dto.PostcodeResponse;
import com.insurance.premium.calculation.dto.PremiumCalculationRequest;
import com.insurance.premium.calculation.dto.PremiumCalculationResult;
import com.insurance.premium.calculation.repository.MileageFactorRepository;
import com.insurance.premium.calculation.repository.RegionRepository;
import com.insurance.premium.calculation.repository.VehicleTypeRepository;
import com.insurance.premium.calculation.service.PremiumCalculationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/premium")
public class PremiumCalculationController {
    
    private static final Logger logger = LoggerFactory.getLogger(PremiumCalculationController.class);
    
    private final PremiumCalculationService calculationService;
    private final RegionRepository regionRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final MileageFactorRepository mileageFactorRepository;
    
    public PremiumCalculationController(
            PremiumCalculationService calculationService,
            RegionRepository regionRepository,
            VehicleTypeRepository vehicleTypeRepository,
            MileageFactorRepository mileageFactorRepository) {
        this.calculationService = calculationService;
        this.regionRepository = regionRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.mileageFactorRepository = mileageFactorRepository;
    }
    
    @PostMapping("/calculate")
    public ResponseEntity<Object> calculatePremium(@Valid @RequestBody PremiumCalculationRequest request) {
        try {
            String validationError = request.getValidationError();
            if (validationError != null) {
                return ResponseEntity.badRequest().body(ErrorResponse.validation(validationError));
            }
            
            PremiumCalculationResult result = calculationService.calculatePremium(request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.error("Error calculating premium", e);
            return ResponseEntity.badRequest().body(ErrorResponse.validation(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error calculating premium", e);
            return ResponseEntity.internalServerError().body(
                    ErrorResponse.serverError("Error calculating premium: " + e.getMessage()));
        }
    }
    
    @GetMapping("/factors")
    public ResponseEntity<List<FactorResponse>> getAllFactors() {
        List<FactorResponse> factors = getAllRegionFactors();
        factors.addAll(getAllVehicleFactors());
        factors.addAll(getAllMileageFactors());
        
        return ResponseEntity.ok(factors);
    }
    
    @GetMapping("/factors/region")
    public ResponseEntity<List<FactorResponse>> getRegionFactors() {
        return ResponseEntity.ok(getAllRegionFactors());
    }
    
    @GetMapping("/factors/vehicle")
    public ResponseEntity<List<FactorResponse>> getVehicleFactors() {
        return ResponseEntity.ok(getAllVehicleFactors());
    }
    
    @GetMapping("/factors/mileage")
    public ResponseEntity<List<FactorResponse>> getMileageFactors() {
        return ResponseEntity.ok(getAllMileageFactors());
    }
    
    @GetMapping("/postcodes")
    public ResponseEntity<Page<PostcodeResponse>> getPostcodes(Pageable pageable) {
        Page<PostcodeResponse> postcodes = regionRepository.findAll(pageable)
                .map(PostcodeResponse::fromRegion);
        
        return ResponseEntity.ok(postcodes);
    }
    
    @GetMapping("/postcodes/search/{prefix}")
    public ResponseEntity<List<PostcodeResponse>> getPostcodesByPrefix(@PathVariable String prefix) {
        List<PostcodeResponse> postcodes = regionRepository.findByPostalCodeStartingWith(prefix).stream()
                .map(PostcodeResponse::fromRegion)
                .toList();
        
        return ResponseEntity.ok(postcodes);
    }
    
    private List<FactorResponse> getAllRegionFactors() {
        // Get unique region factors
        return regionRepository.findAll().stream()
                .map(Region::getRegionFactor)
                .distinct()
                .map(FactorResponse::fromRegionFactor)
                .toList();
    }
    
    private List<FactorResponse> getAllVehicleFactors() {
        return vehicleTypeRepository.findAll().stream()
                .map(FactorResponse::fromVehicleType)
                .toList();
    }
    
    private List<FactorResponse> getAllMileageFactors() {
        return mileageFactorRepository.findAllByOrderByMinMileageAsc().stream()
                .map(FactorResponse::fromMileageFactor)
                .toList();
    }
}
