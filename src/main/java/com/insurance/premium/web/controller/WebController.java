package com.insurance.premium.web.controller;

import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.insurance.premium.application.domain.Application;
import com.insurance.premium.application.domain.Application.Status;
import com.insurance.premium.application.dto.ApplicationRequest;
import com.insurance.premium.application.service.ApplicationService;
import com.insurance.premium.calculation.dto.FactorResponse;
import com.insurance.premium.calculation.dto.PremiumCalculationRequest;
import com.insurance.premium.calculation.service.PremiumCalculationService;

import jakarta.validation.Valid;

/**
 * Controller for web views using Thymeleaf.
 */
@Controller
public class WebController {

    // Role constants
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_AGENT = "ROLE_AGENT";
    private static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
    
    // View names
    private static final String APPLICATIONS_VIEW = "applications";
    private static final String APPLICATION_FORM_VIEW = "application-form";
    private static final String DASHBOARD_VIEW = "dashboard";
    private static final String LOGIN_VIEW = "login";
    private static final String CHANGE_PASSWORD_VIEW = "change-password";
    
    // Redirect paths
    private static final String REDIRECT_APPLICATIONS = "redirect:/applications";
    private static final String REDIRECT_MY_APPLICATIONS = "redirect:/my-applications";
    
    // Model attribute names
    private static final String VEHICLE_TYPES_ATTR = "vehicleTypes";
    private static final String APPLICATION_REQUEST_ATTR = "applicationRequest";
    private static final String PREMIUM_ATTR = "premium";
    private static final String ERROR_MESSAGE_ATTR = "errorMessage";
    private static final String SUCCESS_MESSAGE_ATTR = "successMessage";
    private static final String POSTAL_CODE_ERROR_ATTR = "postalCodeError";
    private static final String CURRENT_STATUS_ATTR = "currentStatus";
    private static final String STATUSES_ATTR = "statuses";
    private static final String IS_ALL_APPLICATIONS_ATTR = "isAllApplications";
    private static final String IS_MY_APPLICATIONS_ATTR = "isMyApplications";
    private static final String USERNAME_ATTR = "username";
    private static final String IS_ADMIN_ATTR = "isAdmin";
    private static final String IS_AGENT_ATTR = "isAgent";
    private static final String IS_CUSTOMER_ATTR = "isCustomer";
    
    // Error messages
    private static final String ERROR_INVALID_POSTAL_CODE = "Invalid postal code. Please enter a valid postal code.";
    private static final String ERROR_CALCULATING_PREMIUM = "Error calculating premium: ";
    private static final String ERROR_NOT_AUTHORIZED = "You are not authorized to update application status";
    private static final String ERROR_UPDATE_FAILED = "Failed to update application status";
    
    // Success messages
    private static final String SUCCESS_APPLICATION_SUBMITTED = "Application submitted successfully with ID: ";
    private static final String SUCCESS_STATUS_UPDATED = "Application status updated successfully";

    private final ApplicationService applicationService;
    private final PremiumCalculationService calculationService;

    public WebController(
            ApplicationService applicationService, 
            PremiumCalculationService calculationService) {
        this.applicationService = applicationService;
        this.calculationService = calculationService;
    }

    /**
     * Login page.
     */
    @GetMapping("/login")
    public String login() {
        return LOGIN_VIEW;
    }

    /**
     * Dashboard page after successful login.
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute(USERNAME_ATTR, principal.getName());
        
        // Add user roles to model
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        boolean isAdmin = authorities.stream().anyMatch(a -> a.getAuthority().equals(ROLE_ADMIN));
        boolean isAgent = authorities.stream().anyMatch(a -> a.getAuthority().equals(ROLE_AGENT));
        boolean isCustomer = authorities.stream().anyMatch(a -> a.getAuthority().equals(ROLE_CUSTOMER));
        
        model.addAttribute(IS_ADMIN_ATTR, isAdmin);
        model.addAttribute(IS_AGENT_ATTR, isAgent);
        model.addAttribute(IS_CUSTOMER_ATTR, isCustomer);
        
        return DASHBOARD_VIEW;
    }

    /**
     * Application form page.
     */
    @GetMapping("/application-form")
    public String applicationForm(Model model) {
        // Add empty application request to the model
        model.addAttribute(APPLICATION_REQUEST_ATTR, new ApplicationRequest("", "", 0));
        
        // Add vehicle types for dropdowns
        List<FactorResponse> vehicleTypes = calculationService.getAllVehicleFactors();
        model.addAttribute(VEHICLE_TYPES_ATTR, vehicleTypes);
        
        return APPLICATION_FORM_VIEW;
    }

    /**
     * Submit application.
     */
    @PostMapping("/submit-application")
    public String submitApplication(@Valid @ModelAttribute ApplicationRequest request, 
                                   BindingResult result, 
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        if (result.hasErrors()) {
            // Add vehicle types for dropdowns
            List<FactorResponse> vehicleTypes = calculationService.getAllVehicleFactors();
            model.addAttribute(VEHICLE_TYPES_ATTR, vehicleTypes);
            return APPLICATION_FORM_VIEW;
        }
        
        // Submit the application
        Application application = applicationService.createApplication(request);
        redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, 
            SUCCESS_APPLICATION_SUBMITTED + application.getId());
        
        // Check if user is customer or agent/admin to determine redirect
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isCustomer = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(ROLE_CUSTOMER));
        
        if (isCustomer) {
            return REDIRECT_MY_APPLICATIONS;
        } else {
            return REDIRECT_APPLICATIONS;
        }
    }
    
    /**
     * Calculate premium without submitting application.
     */
    @PostMapping("/calculate-premium")
    public String calculatePremium(@Valid @ModelAttribute ApplicationRequest request, 
                                  BindingResult result, 
                                  Model model) {
        // Add vehicle types for dropdowns
        List<FactorResponse> vehicleTypes = calculationService.getAllVehicleFactors();
        model.addAttribute(VEHICLE_TYPES_ATTR, vehicleTypes);
        
        if (result.hasErrors()) {
            return APPLICATION_FORM_VIEW;
        }
        
        // Calculate premium
        try {
            // Convert ApplicationRequest to PremiumCalculationRequest
            var calculationRequest = new PremiumCalculationRequest(
                request.postalCode(),
                request.vehicleType(),
                request.annualMileage() != null ? request.annualMileage() : 0
            );
            
            var premiumResult = calculationService.calculatePremium(calculationRequest);
            model.addAttribute(PREMIUM_ATTR, premiumResult);
            model.addAttribute(APPLICATION_REQUEST_ATTR, request);
        } catch (IllegalArgumentException e) {
            // Handle specific error for invalid postal code
            if (e.getMessage().contains("Region factor not found") || e.getMessage().contains("postal")) {
                model.addAttribute(POSTAL_CODE_ERROR_ATTR, ERROR_INVALID_POSTAL_CODE);
            } else {
                model.addAttribute(ERROR_MESSAGE_ATTR, ERROR_CALCULATING_PREMIUM + e.getMessage());
            }
            model.addAttribute(APPLICATION_REQUEST_ATTR, request);
        } catch (Exception e) {
            model.addAttribute(ERROR_MESSAGE_ATTR, ERROR_CALCULATING_PREMIUM + e.getMessage());
            model.addAttribute(APPLICATION_REQUEST_ATTR, request);
        }
        
        return APPLICATION_FORM_VIEW;
    }

    /**
     * All applications overview page (admin and agent only).
     */
    @GetMapping("/applications")
    public String applications(@RequestParam(required = false) Status status,
                              @PageableDefault(size = 10) Pageable pageable,
                              Model model) {
        Page<Application> applications;
        
        if (status != null) {
            applications = applicationService.getApplicationsByStatus(status, pageable);
            model.addAttribute(CURRENT_STATUS_ATTR, status);
        } else {
            applications = applicationService.getAllApplications(pageable);
        }
        
        model.addAttribute(APPLICATIONS_VIEW, applications);
        model.addAttribute(STATUSES_ATTR, Status.values());
        model.addAttribute(IS_ALL_APPLICATIONS_ATTR, true);
        
        return APPLICATIONS_VIEW;
    }
    
    /**
     * My applications overview page (for customers).
     */
    @GetMapping("/my-applications")
    public String myApplications(@RequestParam(required = false) Status status,
                                @PageableDefault(size = 10) Pageable pageable,
                                Model model) {
        Page<Application> applications;
        
        if (status != null) {
            applications = applicationService.getMyApplicationsByStatus(status, pageable);
            model.addAttribute(CURRENT_STATUS_ATTR, status);
        } else {
            applications = applicationService.getMyApplications(pageable);
        }
        
        model.addAttribute(APPLICATIONS_VIEW, applications);
        model.addAttribute(STATUSES_ATTR, Status.values());
        model.addAttribute(IS_MY_APPLICATIONS_ATTR, true);
        
        return APPLICATIONS_VIEW;
    }

    /**
     * Update application status (admin and agent only).
     */
    @PostMapping("/applications/{id}/status")
    public String updateApplicationStatus(@PathVariable Long id, 
                                         @RequestParam Status status,
                                         RedirectAttributes redirectAttributes) {
        // Check if user is authorized to update application status
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(ROLE_ADMIN));
        boolean isAgent = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(ROLE_AGENT));
        
        if (!isAdmin && !isAgent) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, ERROR_NOT_AUTHORIZED);
            return REDIRECT_APPLICATIONS;
        }
        
        Optional<Application> application = applicationService.updateApplicationStatus(id, status);
        
        if (application.isPresent()) {
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, SUCCESS_STATUS_UPDATED);
        } else {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, ERROR_UPDATE_FAILED);
        }
        
        return REDIRECT_APPLICATIONS;
    }
    
    /**
     * Change password page.
     */
    @GetMapping("/user/change-password")
    public String changePassword(Model model) {
        return CHANGE_PASSWORD_VIEW;
    }
}
