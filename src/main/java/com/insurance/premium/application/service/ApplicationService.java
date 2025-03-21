package com.insurance.premium.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurance.premium.application.domain.Application;
import com.insurance.premium.application.domain.Application.Status;
import com.insurance.premium.application.dto.ApplicationRequest;
import com.insurance.premium.application.repository.ApplicationRepository;
import com.insurance.premium.calculation.dto.PremiumCalculationRequest;
import com.insurance.premium.calculation.dto.PremiumCalculationResult;
import com.insurance.premium.calculation.service.PremiumCalculationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);
    
    private final ApplicationRepository applicationRepository;
    private final PremiumCalculationService calculationService;
    
    public ApplicationService(ApplicationRepository applicationRepository, 
                             PremiumCalculationService calculationService) {
        this.applicationRepository = applicationRepository;
        this.calculationService = calculationService;
    }
    
    /**
     * Create a new application and calculate the premium
     * 
     * @param request The application request
     * @return The created application with calculated premium
     */
    @Transactional
    public Application createApplication(ApplicationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        } else {
            String err = request.getValidationError();
            if (err != null) {
                throw new IllegalArgumentException("Invalid request: " + err);
            }
        }
        
        logger.info("Creating new application for postalCode: {}, vehicleType: {}, annualMileage: {}", 
                request.postalCode(), request.vehicleType(), request.annualMileage());
        
        PremiumCalculationRequest calcRequest = new PremiumCalculationRequest(
                request.postalCode(), request.vehicleType(), request.annualMileage());
        PremiumCalculationResult result = calculationService.calculatePremium(calcRequest);
        
        Application application = new Application(
                request.annualMileage(), request.vehicleType(), request.postalCode(), result.basePremium(),
                result.mileageFactor(), result.vehicleTypeFactor(), result.regionFactor(), result.premium(),
                LocalDateTime.now(), Status.NEW);
        
        Application savedApplication = applicationRepository.save(application);
        logger.info("Created application with ID: {}, calculated premium: {}", 
                savedApplication.getId(), savedApplication.getCalculatedPremium());
        
        return savedApplication;
    }
    
    /**
     * Get an application by ID
     * 
     * @param id The application ID
     * @return Optional containing the application if found
     */
    public Optional<Application> getApplication(Long id) {
        return applicationRepository.findById(id);
    }
    
    /**
     * Get all applications
     * 
     * @return List of all applications
     */
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }
    
    /**
     * Get applications by status
     * 
     * @param status The application status
     * @return List of applications with the given status
     */
    public List<Application> getApplicationsByStatus(Status status) {
        return applicationRepository.findByStatus(status);
    }
    
    /**
     * Update application status
     * 
     * @param id The application ID
     * @param status The new status
     * @return Updated application or empty if not found
     */
    @Transactional
    public Optional<Application> updateApplicationStatus(Long id, Status status) {
        logger.info("Updating application status, ID: {}, new status: {}", id, status);
        
        Optional<Application> applicationOpt = applicationRepository.findById(id);
        if (applicationOpt.isPresent()) {
            Application application = applicationOpt.get();
            application.setStatus(status);
            return Optional.of(applicationRepository.save(application));
        }
        
        logger.warn("Application not found for ID: {}", id);
        return Optional.empty();
    }
    
    /**
     * Delete an application
     * 
     * @param id The application ID
     */
    @Transactional
    public void deleteApplication(Long id) {
        logger.info("Deleting application with ID: {}", id);
        applicationRepository.deleteById(id);
    }
}
