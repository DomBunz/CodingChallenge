package com.insurance.premium.calculation.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.converters.models.PageableAsQueryParam;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/premium")
@Tag(name = "Premium Calculation", description = "API for premium calculations and related data")
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
    @Operation(summary = "Calculate premium", description = "Calculates insurance premium based on postal code, vehicle type, and annual mileage")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Premium calculated successfully", 
                content = @Content(schema = @Schema(implementation = PremiumCalculationResult.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Region, vehicle type, or mileage factor not found", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Object> calculatePremium(
            @Parameter(description = "Premium calculation request with postal code, vehicle type, and annual mileage", required = true)
            @Valid @RequestBody PremiumCalculationRequest request) {
        try {
            logger.debug("REST request to calculate premium: {}", request);
            PremiumCalculationResult result = calculationService.calculatePremium(request);
            logger.debug("Premium calculation completed successfully");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid premium calculation request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ErrorResponse.validation(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error calculating premium", e);
            return ResponseEntity.internalServerError().body(ErrorResponse.serverError("An unexpected error occurred"));
        }
    }
    
    @GetMapping("/factors")
    @Operation(summary = "Get all factors", description = "Returns all available factors for premium calculation (region, vehicle type, mileage)")
    public ResponseEntity<Map<String, List<FactorResponse>>> getAllFactors() {
        Map<String, List<FactorResponse>> factors = new HashMap<>();
        factors.put("regionFactors", getAllRegionFactors());
        factors.put("vehicleTypeFactors", getAllVehicleFactors());
        factors.put("mileageFactors", getAllMileageFactors());
        logger.debug("Returning all factors for premium calculation");
        return ResponseEntity.ok(factors);
    }
    
    @GetMapping("/factors/region")
    @Operation(summary = "Get region factors", description = "Returns all available region factors for premium calculation")
    public ResponseEntity<List<FactorResponse>> getRegionFactors() {
        logger.debug("Returning region factors for premium calculation");
        return ResponseEntity.ok(getAllRegionFactors());
    }
    
    @GetMapping("/factors/vehicle")
    @Operation(summary = "Get vehicle type factors", description = "Returns all available vehicle type factors for premium calculation")
    public ResponseEntity<List<FactorResponse>> getVehicleFactors() {
        logger.debug("Returning vehicle type factors for premium calculation");
        return ResponseEntity.ok(getAllVehicleFactors());
    }
    
    @GetMapping("/factors/mileage")
    @Operation(summary = "Get mileage factors", description = "Returns all available mileage factors for premium calculation")
    public ResponseEntity<List<FactorResponse>> getMileageFactors() {
        logger.debug("Returning mileage factors for premium calculation");
        return ResponseEntity.ok(getAllMileageFactors());
    }
    
    @GetMapping("/postcodes")
    @PageableAsQueryParam
    @Operation(summary = "Get all postcodes", description = "Returns all available postcodes with pagination support")
    public ResponseEntity<Page<PostcodeResponse>> getPostcodes(
            @Parameter(description = "Pagination parameters (page, size, sort)", hidden = true)
            Pageable pageable) {
        logger.debug("Returning all postcodes with pagination support");
        Page<PostcodeResponse> postcodes = regionRepository.findAll(pageable)
                .map(PostcodeResponse::fromRegion);
        
        return ResponseEntity.ok(postcodes);
    }
    
    @GetMapping("/postcodes/search/{prefix}")
    @Operation(summary = "Search postcodes by prefix", description = "Returns postcodes that start with the given prefix")
    public ResponseEntity<List<PostcodeResponse>> getPostcodesByPrefix(
            @Parameter(description = "Postal code prefix to search for", required = true)
            @PathVariable String prefix) {
        logger.debug("Searching postcodes by prefix: {}", prefix);
        List<PostcodeResponse> postcodes = regionRepository.findByPostalCodeStartingWith(prefix).stream()
                .map(PostcodeResponse::fromRegion)
                .toList();
        
        return ResponseEntity.ok(postcodes);
    }
    
    private List<FactorResponse> getAllRegionFactors() {
        logger.debug("Returning all region factors for premium calculation");
        // Get unique region factors
        return regionRepository.findAll().stream()
                .map(Region::getRegionFactor)
                .distinct()
                .map(FactorResponse::fromRegionFactor)
                .toList();
    }
    
    private List<FactorResponse> getAllVehicleFactors() {
        logger.debug("Returning all vehicle type factors for premium calculation");
        return vehicleTypeRepository.findAll().stream()
                .map(FactorResponse::fromVehicleType)
                .toList();
    }
    
    private List<FactorResponse> getAllMileageFactors() {
        logger.debug("Returning all mileage factors for premium calculation");
        return mileageFactorRepository.findAllByOrderByMinMileageAsc().stream()
                .map(FactorResponse::fromMileageFactor)
                .toList();
    }
}
