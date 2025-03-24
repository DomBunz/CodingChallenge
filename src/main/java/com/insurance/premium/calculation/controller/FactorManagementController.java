package com.insurance.premium.calculation.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.premium.calculation.domain.MileageFactor;
import com.insurance.premium.calculation.domain.RegionFactor;
import com.insurance.premium.calculation.domain.VehicleType;
import com.insurance.premium.calculation.dto.MileageFactorRequest;
import com.insurance.premium.calculation.dto.RegionFactorRequest;
import com.insurance.premium.calculation.dto.VehicleTypeRequest;
import com.insurance.premium.calculation.service.FactorManagementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/premium/management")
@Tag(name = "Factor Administration", description = "API for managing premium calculation factors (admin only)")
public class FactorManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(FactorManagementController.class);
    
    private final FactorManagementService factorManagementService;
    
    public FactorManagementController(FactorManagementService factorManagementService) {
        this.factorManagementService = factorManagementService;
    }
    
    // Region Factor endpoints
    
    @GetMapping("/regions")
    @Operation(
        summary = "Get all region factors", 
        description = "Returns all region factors used in premium calculations"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Region factors retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegionFactor.class))
        ),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<List<RegionFactor>> getAllRegionFactors() {
        return ResponseEntity.ok(factorManagementService.getAllRegionFactors());
    }
    
    @GetMapping("/regions/{id}")
    @Operation(
        summary = "Get region factor by ID", 
        description = "Returns a region factor by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Region factor found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegionFactor.class))
        ),
        @ApiResponse(responseCode = "404", description = "Region factor not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<RegionFactor> getRegionFactorById(
            @Parameter(description = "Region factor ID", required = true)
            @PathVariable Long id) {
        Optional<RegionFactor> regionFactor = factorManagementService.getRegionFactorById(id);
        return regionFactor.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/regions")
    @Operation(
        summary = "Create region factor", 
        description = "Creates a new region factor with the provided data"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Region factor created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegionFactor.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<RegionFactor> createRegionFactor(
            @Parameter(description = "Region factor data", required = true, schema = @Schema(implementation = RegionFactorRequest.class))
            @Valid @RequestBody RegionFactorRequest request) {
        try {
            RegionFactor createdFactor = factorManagementService.createRegionFactor(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFactor);
        } catch (IllegalArgumentException e) {
            logger.error("Error creating region factor: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/regions/{id}")
    @Operation(
        summary = "Update region factor", 
        description = "Updates an existing region factor with the provided data"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Region factor updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegionFactor.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Region factor not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<RegionFactor> updateRegionFactor(
            @Parameter(description = "Region factor ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated region factor data", required = true, schema = @Schema(implementation = RegionFactorRequest.class))
            @Valid @RequestBody RegionFactorRequest request) {
        try {
            RegionFactor updatedFactor = factorManagementService.updateRegionFactor(id, request);
            return ResponseEntity.ok(updatedFactor);
        } catch (IllegalArgumentException e) {
            logger.error("Error updating region factor: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/regions/{id}")
    @Operation(
        summary = "Delete region factor", 
        description = "Deletes a region factor by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Region factor deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Region factor not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<Void> deleteRegionFactor(
            @Parameter(description = "Region factor ID", required = true)
            @PathVariable Long id) {
        try {
            factorManagementService.deleteRegionFactor(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.error("Error deleting region factor: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    // Vehicle Type endpoints
    
    @GetMapping("/vehicles")
    @Operation(
        summary = "Get all vehicle types", 
        description = "Returns all vehicle types used in premium calculations"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Vehicle types retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleType.class))
        ),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<List<VehicleType>> getAllVehicleTypes() {
        return ResponseEntity.ok(factorManagementService.getAllVehicleTypes());
    }
    
    @GetMapping("/vehicles/{id}")
    @Operation(
        summary = "Get vehicle type by ID", 
        description = "Returns a vehicle type by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Vehicle type found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleType.class))
        ),
        @ApiResponse(responseCode = "404", description = "Vehicle type not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<VehicleType> getVehicleTypeById(
            @Parameter(description = "Vehicle type ID", required = true)
            @PathVariable Long id) {
        Optional<VehicleType> vehicleType = factorManagementService.getVehicleTypeById(id);
        return vehicleType.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/vehicles")
    @Operation(
        summary = "Create vehicle type", 
        description = "Creates a new vehicle type with the provided data"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Vehicle type created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleType.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<VehicleType> createVehicleType(
            @Parameter(description = "Vehicle type data", required = true, schema = @Schema(implementation = VehicleTypeRequest.class))
            @Valid @RequestBody VehicleTypeRequest request) {
        try {
            VehicleType createdType = factorManagementService.createVehicleType(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdType);
        } catch (IllegalArgumentException e) {
            logger.error("Error creating vehicle type: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/vehicles/{id}")
    @Operation(
        summary = "Update vehicle type", 
        description = "Updates an existing vehicle type with the provided data"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Vehicle type updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = VehicleType.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Vehicle type not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<VehicleType> updateVehicleType(
            @Parameter(description = "Vehicle type ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated vehicle type data", required = true, schema = @Schema(implementation = VehicleTypeRequest.class))
            @Valid @RequestBody VehicleTypeRequest request) {
        try {
            VehicleType updatedType = factorManagementService.updateVehicleType(id, request);
            return ResponseEntity.ok(updatedType);
        } catch (IllegalArgumentException e) {
            logger.error("Error updating vehicle type: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/vehicles/{id}")
    @Operation(
        summary = "Delete vehicle type", 
        description = "Deletes a vehicle type by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Vehicle type deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Vehicle type not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<Void> deleteVehicleType(
            @Parameter(description = "Vehicle type ID", required = true)
            @PathVariable Long id) {
        try {
            factorManagementService.deleteVehicleType(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.error("Error deleting vehicle type: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    // Mileage Factor endpoints
    
    @GetMapping("/mileages")
    @Operation(
        summary = "Get all mileage factors", 
        description = "Returns all mileage factors used in premium calculations"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Mileage factors retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MileageFactor.class))
        ),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<List<MileageFactor>> getAllMileageFactors() {
        return ResponseEntity.ok(factorManagementService.getAllMileageFactors());
    }
    
    @GetMapping("/mileages/{id}")
    @Operation(
        summary = "Get mileage factor by ID", 
        description = "Returns a mileage factor by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Mileage factor found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MileageFactor.class))
        ),
        @ApiResponse(responseCode = "404", description = "Mileage factor not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<MileageFactor> getMileageFactorById(
            @Parameter(description = "Mileage factor ID", required = true)
            @PathVariable Long id) {
        Optional<MileageFactor> mileageFactor = factorManagementService.getMileageFactorById(id);
        return mileageFactor.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/mileages")
    @Operation(
        summary = "Create mileage factor", 
        description = "Creates a new mileage factor with the provided data"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Mileage factor created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MileageFactor.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<MileageFactor> createMileageFactor(
            @Parameter(description = "Mileage factor data", required = true, schema = @Schema(implementation = MileageFactorRequest.class))
            @Valid @RequestBody MileageFactorRequest request) {
        try {
            MileageFactor createdFactor = factorManagementService.createMileageFactor(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFactor);
        } catch (IllegalArgumentException e) {
            logger.error("Error creating mileage factor: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/mileages/{id}")
    @Operation(
        summary = "Update mileage factor", 
        description = "Updates an existing mileage factor with the provided data"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Mileage factor updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MileageFactor.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "404", description = "Mileage factor not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<MileageFactor> updateMileageFactor(
            @Parameter(description = "Mileage factor ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated mileage factor data", required = true, schema = @Schema(implementation = MileageFactorRequest.class))
            @Valid @RequestBody MileageFactorRequest request) {
        try {
            MileageFactor updatedFactor = factorManagementService.updateMileageFactor(id, request);
            return ResponseEntity.ok(updatedFactor);
        } catch (IllegalArgumentException e) {
            logger.error("Error updating mileage factor: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/mileages/{id}")
    @Operation(
        summary = "Delete mileage factor", 
        description = "Deletes a mileage factor by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Mileage factor deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Mileage factor not found"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<Void> deleteMileageFactor(
            @Parameter(description = "Mileage factor ID", required = true)
            @PathVariable Long id) {
        try {
            factorManagementService.deleteMileageFactor(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.error("Error deleting mileage factor: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
