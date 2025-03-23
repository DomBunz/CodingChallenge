package com.insurance.premium.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.insurance.premium.application.domain.Application;
import com.insurance.premium.application.domain.Application.Status;
import com.insurance.premium.application.dto.ApplicationRequest;
import com.insurance.premium.application.repository.ApplicationRepository;
import com.insurance.premium.calculation.dto.PremiumCalculationRequest;
import com.insurance.premium.calculation.dto.PremiumCalculationResult;
import com.insurance.premium.calculation.service.PremiumCalculationService;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;
    
    @Mock
    private PremiumCalculationService calculationService;
    
    @InjectMocks
    private ApplicationService applicationService;
    
    @Test
    void createApplication_ShouldCreateAndSaveApplication() {
        // Arrange
        String postalCode = "10115";
        String vehicleType = "Kompaktklasse";
        int annualMileage = 15000;
        
        ApplicationRequest request = new ApplicationRequest(postalCode, vehicleType, annualMileage);
        
        BigDecimal basePremium = new BigDecimal("500.00");
        BigDecimal mileageFactor = new BigDecimal("1.5");
        BigDecimal vehicleFactor = new BigDecimal("1.0");
        BigDecimal regionFactor = new BigDecimal("1.2");
        BigDecimal calculatedPremium = new BigDecimal("900.00");
        
        PremiumCalculationResult calculationResult = new PremiumCalculationResult(
            postalCode, vehicleType, annualMileage,
            basePremium, mileageFactor, vehicleFactor, regionFactor, calculatedPremium
        );
        
        when(calculationService.calculatePremium(any(PremiumCalculationRequest.class)))
            .thenReturn(calculationResult);
        
        Application savedApplication = new Application();
        savedApplication.setId(1L);
        savedApplication.setPostalCode(postalCode);
        savedApplication.setVehicleType(vehicleType);
        savedApplication.setAnnualMileage(annualMileage);
        savedApplication.setBasePremium(basePremium);
        savedApplication.setMileageFactor(mileageFactor);
        savedApplication.setVehicleFactor(vehicleFactor);
        savedApplication.setRegionFactor(regionFactor);
        savedApplication.setCalculatedPremium(calculatedPremium);
        savedApplication.setCreatedAt(LocalDateTime.now());
        savedApplication.setStatus(Status.NEW);
        
        when(applicationRepository.save(any(Application.class))).thenReturn(savedApplication);
        
        // Act
        Application result = applicationService.createApplication(request);
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(postalCode, result.getPostalCode());
        assertEquals(vehicleType, result.getVehicleType());
        assertEquals(annualMileage, result.getAnnualMileage());
        assertEquals(calculatedPremium, result.getCalculatedPremium());
        assertEquals(Status.NEW, result.getStatus());
        
        // Verify calculation service was called with correct parameters
        ArgumentCaptor<PremiumCalculationRequest> requestCaptor = ArgumentCaptor.forClass(PremiumCalculationRequest.class);
        verify(calculationService).calculatePremium(requestCaptor.capture());
        
        PremiumCalculationRequest capturedRequest = requestCaptor.getValue();
        assertEquals(postalCode, capturedRequest.postalCode());
        assertEquals(vehicleType, capturedRequest.vehicleType());
        assertEquals(annualMileage, capturedRequest.annualMileage());
        
        // Verify application was saved
        verify(applicationRepository).save(any(Application.class));
    }
    
    @Test
    void getApplication_ShouldReturnApplication_WhenExists() {
        // Arrange
        Long id = 1L;
        Application application = new Application();
        application.setId(id);
        
        when(applicationRepository.findById(id)).thenReturn(Optional.of(application));
        
        // Act
        Optional<Application> result = applicationService.getApplication(id);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        
        // Verify repository was called
        verify(applicationRepository).findById(id);
    }
    
    @Test
    void getApplication_ShouldReturnEmpty_WhenNotExists() {
        // Arrange
        Long id = 1L;
        
        when(applicationRepository.findById(id)).thenReturn(Optional.empty());
        
        // Act
        Optional<Application> result = applicationService.getApplication(id);
        
        // Assert
        assertTrue(result.isEmpty());
        
        // Verify repository was called
        verify(applicationRepository).findById(id);
    }
    
    @Test
    void updateApplicationStatus_ShouldUpdateStatus_WhenApplicationExists() {
        // Arrange
        Long id = 1L;
        Status newStatus = Status.ACCEPTED;
        
        Application application = new Application();
        application.setId(id);
        application.setStatus(Status.NEW);
        
        Application updatedApplication = new Application();
        updatedApplication.setId(id);
        updatedApplication.setStatus(newStatus);
        
        when(applicationRepository.findById(id)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class))).thenReturn(updatedApplication);
        
        // Act
        Optional<Application> result = applicationService.updateApplicationStatus(id, newStatus);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(newStatus, result.get().getStatus());
        
        // Verify repository methods were called
        verify(applicationRepository).findById(id);
        verify(applicationRepository).save(any(Application.class));
    }
    
    @Test
    void updateApplicationStatus_ShouldReturnEmpty_WhenApplicationNotExists() {
        // Arrange
        Long id = 1L;
        Status newStatus = Status.ACCEPTED;
        
        when(applicationRepository.findById(id)).thenReturn(Optional.empty());
        
        // Act
        Optional<Application> result = applicationService.updateApplicationStatus(id, newStatus);
        
        // Assert
        assertTrue(result.isEmpty());
        
        // Verify repository was called but save was not
        verify(applicationRepository).findById(id);
        verify(applicationRepository, never()).save(any(Application.class));
    }
}
