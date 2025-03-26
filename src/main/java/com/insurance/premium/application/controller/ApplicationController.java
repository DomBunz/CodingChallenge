package com.insurance.premium.application.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.premium.application.domain.Application;
import com.insurance.premium.application.domain.Application.Status;
import com.insurance.premium.application.dto.ApplicationRequest;
import com.insurance.premium.application.dto.ApplicationResponse;
import com.insurance.premium.application.dto.ErrorResponse;
import com.insurance.premium.application.service.ApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.Optional;

@RestController
@RequestMapping("/api/applications")
@Tag(name = "Application Management", description = "API for managing insurance applications")
@Validated
public class ApplicationController {
    
    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);
    
    private final ApplicationService applicationService;
    
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }
    
    @PostMapping
    @Operation(summary = "Create application", description = "Creates a new insurance application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Application created successfully", 
                content = @Content(schema = @Schema(implementation = ApplicationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Object> createApplication(@Valid @RequestBody ApplicationRequest request) {
        logger.debug("REST request to create application: {}", request);
        try {
            Application application = applicationService.createApplication(request);
            logger.debug("Application created [id={}]", application.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApplicationResponse.fromEntity(application));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid application request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(ErrorResponse.validation(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating application", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.serverError("Error creating application: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get application by ID", description = "Retrieves an application by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application found", 
                content = @Content(schema = @Schema(implementation = ApplicationResponse.class))),
        @ApiResponse(responseCode = "404", description = "Application not found", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Object> getApplication(
            @Parameter(description = "Application ID", required = true)
            @PathVariable Long id) {
        logger.debug("REST request to get application [id={}]", id);
        
        Optional<Application> applicationOpt = applicationService.getApplication(id);
        if (applicationOpt.isPresent()) {
            logger.debug("Application found [id={}]", id);
            return ResponseEntity.ok(ApplicationResponse.fromEntity(applicationOpt.get()));
        } else {
            logger.debug("Application not found [id={}]", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("Application not found with ID: " + id));
        }
    }
    
    @GetMapping
    @PageableAsQueryParam
    @Operation(summary = "Get all applications", description = "Returns all applications with pagination support")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Applications retrieved successfully", 
                content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<ApplicationResponse>> getAllApplications(
            @Parameter(description = "Pagination parameters (page, size, sort)", hidden = true)
            Pageable pageable) {
        logger.debug("REST request to get all applications");
        Page<ApplicationResponse> applications = applicationService.getAllApplications(pageable)
            .map(ApplicationResponse::fromEntity);
        logger.debug("Applications retrieved successfully");
        return ResponseEntity.ok(applications);
    }
    
    @GetMapping("/status/{status}")
    @PageableAsQueryParam
    @Operation(summary = "Get applications by status", description = "Returns applications filtered by status with pagination support")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Applications retrieved successfully", 
                content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<ApplicationResponse>> getApplicationsByStatus(
            @Parameter(description = "Application status (e.g., PENDING, APPROVED, REJECTED)", required = true)
            @PathVariable Status status, 
            @Parameter(description = "Pagination parameters (page, size, sort)", hidden = true)
            Pageable pageable) {
        logger.debug("REST request to get applications by status [status={}]", status);
        Page<ApplicationResponse> applications = applicationService.getApplicationsByStatus(status, pageable)
            .map(ApplicationResponse::fromEntity);
        logger.debug("Applications retrieved successfully");
        return ResponseEntity.ok(applications);
    }
    
    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "Update application status", description = "Updates the status of an existing application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Application status updated successfully", 
                content = @Content(schema = @Schema(implementation = ApplicationResponse.class))),
        @ApiResponse(responseCode = "404", description = "Application not found", 
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Object> updateApplicationStatus(
            @Parameter(description = "Application ID", required = true)
            @PathVariable Long id, 
            @Parameter(description = "New application status", required = true)
            @PathVariable Status status) {
        logger.debug("REST request to update application status [id={}, status={}]", id, status);
        Optional<Application> updatedApplication = applicationService.updateApplicationStatus(id, status);
        if (updatedApplication.isPresent()) {
            logger.debug("Application status updated successfully [id={}]", id);
            return ResponseEntity.ok(ApplicationResponse.fromEntity(updatedApplication.get()));
        } else {
            logger.debug("Application not found [id={}]", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of("Application not found with ID: " + id));
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete application", description = "Deletes an application by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Application deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Application not found")
    })
    public ResponseEntity<Void> deleteApplication(
            @Parameter(description = "Application ID", required = true)
            @PathVariable Long id) {
        logger.debug("REST request to delete application [id={}]", id);
        applicationService.deleteApplication(id);
        logger.debug("Application deleted successfully [id={}]", id);
        return ResponseEntity.noContent().build();
    }
}
