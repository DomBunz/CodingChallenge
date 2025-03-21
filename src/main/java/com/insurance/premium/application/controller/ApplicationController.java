package com.insurance.premium.application.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import com.insurance.premium.application.domain.Application;
import com.insurance.premium.application.domain.Application.Status;
import com.insurance.premium.application.dto.ApplicationRequest;
import com.insurance.premium.application.dto.ApplicationResponse;
import com.insurance.premium.application.dto.ErrorResponse;
import com.insurance.premium.application.service.ApplicationService;

import jakarta.validation.Valid;

import java.util.Optional;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    
    private static final Logger logger = LoggerFactory.getLogger(ApplicationController.class);
    
    private final ApplicationService applicationService;
    
    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }
    
    @PostMapping
    public ResponseEntity<Object> createApplication(@Valid @RequestBody ApplicationRequest request) {
        String validationError = request.getValidationError();
        if (validationError != null) {
            return ResponseEntity.badRequest().body(ErrorResponse.validation(validationError));
        }
        
        try {
            Application application = applicationService.createApplication(request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApplicationResponse.fromEntity(application));
        } catch (Exception e) {
            logger.error("Error creating application", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.serverError("Error creating application: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Object> getApplication(@PathVariable Long id) {
        Optional<Application> application = applicationService.getApplication(id);
        
        return application
            .map(app -> ResponseEntity.ok((Object)ApplicationResponse.fromEntity(app)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<Page<ApplicationResponse>> getAllApplications(Pageable pageable) {
        Page<ApplicationResponse> applications = applicationService.getAllApplications(pageable)
            .map(ApplicationResponse::fromEntity);
        
        return ResponseEntity.ok(applications);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ApplicationResponse>> getApplicationsByStatus(
            @PathVariable Status status, 
            Pageable pageable) {
        
        Page<ApplicationResponse> applications = applicationService.getApplicationsByStatus(status, pageable)
            .map(ApplicationResponse::fromEntity);
        
        return ResponseEntity.ok(applications);
    }
    
    @PutMapping("/{id}/status/{status}")
    public ResponseEntity<Object> updateApplicationStatus(
            @PathVariable Long id, 
            @PathVariable Status status) {
        
        Optional<Application> updatedApplication = applicationService.updateApplicationStatus(id, status);
        
        return updatedApplication
            .map(app -> ResponseEntity.ok((Object)ApplicationResponse.fromEntity(app)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }
}
