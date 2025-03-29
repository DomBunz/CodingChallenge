package com.insurance.premium.calculation.controller;

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
import com.insurance.premium.calculation.dto.FactorResponse;
import com.insurance.premium.calculation.dto.PostcodeResponse;
import com.insurance.premium.calculation.dto.PremiumCalculationRequest;
import com.insurance.premium.calculation.dto.PremiumCalculationResult;
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
    
    public PremiumCalculationController(PremiumCalculationService calculationService) {
        this.calculationService = calculationService;
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
        logger.debug("REST request to get all factors for premium calculation");
        Map<String, List<FactorResponse>> factors = calculationService.getAllFactors();
        return ResponseEntity.ok(factors);
    }
    
    @GetMapping("/factors/region")
    @Operation(summary = "Get region factors", description = "Returns all available region factors for premium calculation")
    public ResponseEntity<List<FactorResponse>> getRegionFactors() {
        logger.debug("REST request to get region factors");
        List<FactorResponse> factors = calculationService.getAllRegionFactors();
        return ResponseEntity.ok(factors);
    }
    
    @GetMapping("/factors/vehicle")
    @Operation(summary = "Get vehicle type factors", description = "Returns all available vehicle type factors for premium calculation")
    public ResponseEntity<List<FactorResponse>> getVehicleFactors() {
        logger.debug("REST request to get vehicle type factors");
        List<FactorResponse> factors = calculationService.getAllVehicleFactors();
        return ResponseEntity.ok(factors);
    }
    
    @GetMapping("/factors/mileage")
    @Operation(summary = "Get mileage factors", description = "Returns all available mileage factors for premium calculation")
    public ResponseEntity<List<FactorResponse>> getMileageFactors() {
        logger.debug("REST request to get mileage factors");
        List<FactorResponse> factors = calculationService.getAllMileageFactors();
        return ResponseEntity.ok(factors);
    }
    
    @GetMapping("/postcodes")
    @PageableAsQueryParam
    @Operation(summary = "Get all postcodes", description = "Returns all available postcodes with pagination support")
    public ResponseEntity<Page<PostcodeResponse>> getPostcodes(
            @Parameter(description = "Pagination parameters (page, size, sort)", hidden = true)
            Pageable pageable) {
        logger.debug("REST request to get all postcodes with pagination");
        Page<PostcodeResponse> postcodes = calculationService.getAllPostcodes(pageable);
        return ResponseEntity.ok(postcodes);
    }
    
    @GetMapping("/postcodes/search/{prefix}")
    @Operation(summary = "Search postcodes by prefix", description = "Returns postcodes that start with the given prefix")
    public ResponseEntity<List<PostcodeResponse>> getPostcodesByPrefix(
            @Parameter(description = "Postal code prefix to search for", required = true)
            @PathVariable String prefix) {
        logger.debug("REST request to search postcodes by prefix: {}", prefix);
        List<PostcodeResponse> postcodes = calculationService.getPostcodesByPrefix(prefix);
        return ResponseEntity.ok(postcodes);
    }
    
    @GetMapping("/postcodes/search/term/{searchTerm}")
    @Operation(summary = "Search postcodes by area, city, or district", 
               description = "Returns postcodes where area, city, or district contains the given search term")
    public ResponseEntity<List<PostcodeResponse>> getPostcodesByAreaCityOrDistrict(
            @Parameter(description = "Search term to look for in area, city, or district", required = true)
            @PathVariable String searchTerm) {
        logger.debug("REST request to search postcodes by area, city, or district containing: {}", searchTerm);
        List<PostcodeResponse> postcodes = calculationService.getPostcodesByAreaCityOrDistrict(searchTerm);
        return ResponseEntity.ok(postcodes);
    }
}
