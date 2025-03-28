package com.insurance.premium.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.insurance.premium.application.domain.Application;
import com.insurance.premium.application.domain.Application.Status;
import com.insurance.premium.application.dto.ApplicationRequest;
import com.insurance.premium.application.repository.ApplicationRepository;
import com.insurance.premium.calculation.dto.PremiumCalculationRequest;
import com.insurance.premium.calculation.dto.PremiumCalculationResult;
import com.insurance.premium.calculation.service.PremiumCalculationService;
import com.insurance.premium.security.domain.User;
import com.insurance.premium.security.service.UserService;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Validated
public class ApplicationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);
    
    // Logging message constants
    private static final String LOG_RETRIEVED_APPLICATIONS = "Retrieved {} applications";
    private static final String LOG_APPLICATION_NOT_FOUND = "Application not found [id={}]";
    
    private final ApplicationRepository applicationRepository;
    private final PremiumCalculationService calculationService;
    private final UserService userService;
    
    public ApplicationService(ApplicationRepository applicationRepository, 
                             PremiumCalculationService calculationService,
                             UserService userService) {
        this.applicationRepository = applicationRepository;
        this.calculationService = calculationService;
        this.userService = userService;
    }
    
    /**
     * Create a new application and calculate the premium
     * 
     * @param request The application request
     * @return The created application with calculated premium
     */
    @Transactional
    public Application createApplication(@Valid ApplicationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        logger.debug("Processing application request: {}", request);
        
        PremiumCalculationRequest calcRequest = new PremiumCalculationRequest(
                request.postalCode(), request.vehicleType(), request.annualMileage());
        PremiumCalculationResult result = calculationService.calculatePremium(calcRequest);
        
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> currentUser = userService.findByUsername(username);
        
        // Create the application with the correct constructor
        Application application = new Application(
                request.annualMileage(), request.vehicleType(), request.postalCode(), result.basePremium(),
                result.mileageFactor(), result.vehicleTypeFactor(), result.regionFactor(), result.premium(),
                LocalDateTime.now(), Status.NEW, currentUser.orElse(null));
        
        Application savedApplication = applicationRepository.save(application);
        logger.info("Created application [id={}] with premium={} for postalCode={}, vehicleType={}, annualMileage={}",
                savedApplication.getId(), savedApplication.getCalculatedPremium(), 
                savedApplication.getPostalCode(), savedApplication.getVehicleType(), 
                savedApplication.getAnnualMileage());
        
        return savedApplication;
    }
    
    /**
     * Get an application by ID
     * 
     * @param id The application ID
     * @return Optional containing the application if found
     */
    @Transactional(readOnly = true)
    public Optional<Application> getApplication(Long id) {
        logger.debug("Retrieving application [id={}]", id);
        Optional<Application> application = applicationRepository.findById(id);
        if (application.isPresent()) {
            logger.debug("Found application [id={}]", id);
        } else {
            logger.debug(LOG_APPLICATION_NOT_FOUND, id);
        }
        return application;
    }
    
    /**
     * Get all applications with pagination
     * 
     * @param pageable Pagination information
     * @return Page of applications
     */
    @Transactional(readOnly = true)
    public Page<Application> getAllApplications(Pageable pageable) {
        logger.debug("Retrieving all applications with pagination [page={}, size={}]", 
                pageable.getPageNumber(), pageable.getPageSize());
        Page<Application> applications = applicationRepository.findAll(pageable);
        logger.debug(LOG_RETRIEVED_APPLICATIONS, applications.getTotalElements());
        return applications;
    }
    
    /**
     * Get all applications
     * 
     * @return List of all applications
     */
    @Transactional(readOnly = true)
    public List<Application> getAllApplications() {
        logger.debug("Retrieving all applications");
        List<Application> applications = applicationRepository.findAll();
        logger.debug(LOG_RETRIEVED_APPLICATIONS, applications.size());
        return applications;
    }
    
    /**
     * Get applications by status with pagination
     * 
     * @param status The application status to filter by
     * @param pageable Pagination information
     * @return Page of applications with the given status
     */
    @Transactional(readOnly = true)
    public Page<Application> getApplicationsByStatus(Status status, Pageable pageable) {
        logger.debug("Retrieving applications with status {} and pagination [page={}, size={}]", 
                status, pageable.getPageNumber(), pageable.getPageSize());
        Page<Application> applications = applicationRepository.findByStatus(status, pageable);
        logger.debug(LOG_RETRIEVED_APPLICATIONS, applications.getTotalElements());
        return applications;
    }
    
    /**
     * Get applications by status
     * 
     * @param status The application status to filter by
     * @return List of applications with the given status
     */
    @Transactional(readOnly = true)
    public List<Application> getApplicationsByStatus(Status status) {
        logger.debug("Retrieving applications with status {}", status);
        List<Application> applications = applicationRepository.findByStatus(status);
        logger.debug(LOG_RETRIEVED_APPLICATIONS, applications.size());
        return applications;
    }
    
    /**
     * Get applications created by the current authenticated user with pagination
     * 
     * @param pageable Pagination information
     * @return Page of applications created by the current user
     */
    @Transactional(readOnly = true)
    public Page<Application> getMyApplications(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> currentUser = userService.findByUsername(username);
        
        if (currentUser.isPresent()) {
            logger.debug("Retrieving applications for user {} with pagination [page={}, size={}]", 
                    username, pageable.getPageNumber(), pageable.getPageSize());
            Page<Application> applications = applicationRepository.findByCreatedBy(currentUser.get(), pageable);
            logger.debug(LOG_RETRIEVED_APPLICATIONS, applications.getTotalElements());
            return applications;
        } else {
            logger.warn("User not found: {}", username);
            return Page.empty(pageable);
        }
    }
    
    /**
     * Get applications created by the current authenticated user with a specific status and pagination
     * 
     * @param status The application status to filter by
     * @param pageable Pagination information
     * @return Page of applications created by the current user with the given status
     */
    @Transactional(readOnly = true)
    public Page<Application> getMyApplicationsByStatus(Status status, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> currentUser = userService.findByUsername(username);
        
        if (currentUser.isPresent()) {
            logger.debug("Retrieving applications for user {} with status {} and pagination [page={}, size={}]", 
                    username, status, pageable.getPageNumber(), pageable.getPageSize());
            Page<Application> applications = applicationRepository.findByCreatedByAndStatus(currentUser.get(), status, pageable);
            logger.debug(LOG_RETRIEVED_APPLICATIONS, applications.getTotalElements());
            return applications;
        } else {
            logger.warn("User not found: {}", username);
            return Page.empty(pageable);
        }
    }
    
    /**
     * Update the status of an application
     * 
     * @param id The application ID
     * @param status The new status
     * @return Optional containing the updated application if found
     */
    @Transactional
    public Optional<Application> updateApplicationStatus(Long id, Status status) {
        logger.debug("Updating application [id={}] status to {}", id, status);
        
        Optional<Application> applicationOpt = applicationRepository.findById(id);
        if (applicationOpt.isPresent()) {
            Application application = applicationOpt.get();
            application.setStatus(status);
            Application updatedApplication = applicationRepository.save(application);
            logger.info("Updated application [id={}] status to {}", updatedApplication.getId(), updatedApplication.getStatus());
            return Optional.of(updatedApplication);
        }
        
        logger.warn(LOG_APPLICATION_NOT_FOUND, id);
        return Optional.empty();
    }
    
    /**
     * Delete an application
     * 
     * @param id The application ID
     */
    @Transactional
    public void deleteApplication(Long id) {
        logger.debug("Deleting application [id={}]", id);
        applicationRepository.deleteById(id);
        logger.info("Deleted application [id={}]", id);
    }
}
