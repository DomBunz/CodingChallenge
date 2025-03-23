package com.insurance.premium.calculation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.insurance.premium.calculation.domain.MileageFactor;
import com.insurance.premium.calculation.domain.Region;
import com.insurance.premium.calculation.domain.RegionFactor;
import com.insurance.premium.calculation.domain.VehicleType;
import com.insurance.premium.calculation.dto.PremiumCalculationRequest;
import com.insurance.premium.calculation.dto.PremiumCalculationResult;
import com.insurance.premium.calculation.repository.MileageFactorRepository;
import com.insurance.premium.calculation.repository.RegionRepository;
import com.insurance.premium.calculation.repository.VehicleTypeRepository;
import com.insurance.premium.common.service.ConfigurationService;

@ExtendWith(MockitoExtension.class)
class PremiumCalculationServiceTest {

    @Mock
    private RegionRepository regionRepository;
    
    @Mock
    private VehicleTypeRepository vehicleTypeRepository;
    
    @Mock
    private MileageFactorRepository mileageFactorRepository;
    
    @Mock
    private ConfigurationService configService;
    
    @InjectMocks
    private PremiumCalculationService calculationService;
    
    private static final String POSTAL_CODE = "10115";
    private static final String VEHICLE_TYPE = "Kompaktklasse";
    private static final int ANNUAL_MILEAGE = 15000;
    private static final BigDecimal REGION_FACTOR_VALUE = new BigDecimal("1.2");
    private static final BigDecimal VEHICLE_FACTOR_VALUE = new BigDecimal("1.0");
    private static final BigDecimal MILEAGE_FACTOR_VALUE = new BigDecimal("1.5");
    private static final BigDecimal BASE_PREMIUM = new BigDecimal("500.00");
    
    @Test
    void calculatePremium_WithValidInputs_ReturnsCorrectPremium() {
        // Arrange
        PremiumCalculationRequest request = new PremiumCalculationRequest(POSTAL_CODE, VEHICLE_TYPE, ANNUAL_MILEAGE);
        
        RegionFactor regionFactor = new RegionFactor();
        regionFactor.setFederalState("Berlin");
        regionFactor.setFactor(REGION_FACTOR_VALUE);
        
        Region region = new Region("Berlin", "Germany", "Berlin", "Berlin", POSTAL_CODE, "Mitte", regionFactor);
        
        VehicleType vehicleType = new VehicleType();
        vehicleType.setName(VEHICLE_TYPE);
        vehicleType.setFactor(VEHICLE_FACTOR_VALUE);
        
        MileageFactor mileageFactor = new MileageFactor();
        mileageFactor.setMinMileage(10001);
        mileageFactor.setMaxMileage(20000);
        mileageFactor.setFactor(MILEAGE_FACTOR_VALUE);
        
        when(configService.getBasePremium()).thenReturn(BASE_PREMIUM);
        when(regionRepository.findByPostalCode(POSTAL_CODE)).thenReturn(List.of(region));
        when(vehicleTypeRepository.findByName(VEHICLE_TYPE)).thenReturn(Optional.of(vehicleType));
        when(mileageFactorRepository.findByMileage(ANNUAL_MILEAGE)).thenReturn(Optional.of(mileageFactor));
        
        // Expected premium calculation: 500 * 1.2 * 1.0 * 1.5 = 900.00
        BigDecimal expectedPremium = BASE_PREMIUM
                .multiply(REGION_FACTOR_VALUE)
                .multiply(VEHICLE_FACTOR_VALUE)
                .multiply(MILEAGE_FACTOR_VALUE)
                .setScale(2, RoundingMode.HALF_UP);
        
        // Act
        PremiumCalculationResult result = calculationService.calculatePremium(request);
        
        // Assert
        assertNotNull(result);
        assertEquals(expectedPremium, result.premium());
        assertEquals(REGION_FACTOR_VALUE, result.regionFactor());
        assertEquals(VEHICLE_FACTOR_VALUE, result.vehicleTypeFactor());
        assertEquals(MILEAGE_FACTOR_VALUE, result.mileageFactor());
        assertEquals(POSTAL_CODE, result.postalCode());
        assertEquals(VEHICLE_TYPE, result.vehicleType());
        assertEquals(ANNUAL_MILEAGE, result.annualMileage());
    }
    
    @Test
    void calculatePremium_WithInvalidRequest_ThrowsException() {
        // Arrange
        PremiumCalculationRequest invalidRequest = new PremiumCalculationRequest(null, VEHICLE_TYPE, ANNUAL_MILEAGE);
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            calculationService.calculatePremium(invalidRequest);
        });
        
        assertTrue(exception.getMessage().contains("Invalid request"));
    }
    
    @Test
    void calculatePremium_WithUnknownVehicleType_ThrowsException() {
        // Arrange
        PremiumCalculationRequest request = new PremiumCalculationRequest(POSTAL_CODE, "Unknown", ANNUAL_MILEAGE);
        
        RegionFactor regionFactor = new RegionFactor();
        regionFactor.setFederalState("Berlin");
        regionFactor.setFactor(REGION_FACTOR_VALUE);
        
        Region region = new Region("Berlin", "Germany", "Berlin", "Berlin", POSTAL_CODE, "Mitte", regionFactor);
        
        when(regionRepository.findByPostalCode(POSTAL_CODE)).thenReturn(List.of(region));
        when(vehicleTypeRepository.findByName("Unknown")).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            calculationService.calculatePremium(request);
        });
        
        assertTrue(exception.getMessage().contains("Unknown vehicle type"));
    }
    
    @Test
    void calculatePremium_WithNoMileageFactor_ThrowsException() {
        // Arrange
        PremiumCalculationRequest request = new PremiumCalculationRequest(POSTAL_CODE, VEHICLE_TYPE, ANNUAL_MILEAGE);
        
        RegionFactor regionFactor = new RegionFactor();
        regionFactor.setFederalState("Berlin");
        regionFactor.setFactor(REGION_FACTOR_VALUE);
        
        Region region = new Region("Berlin", "Germany", "Berlin", "Berlin", POSTAL_CODE, "Mitte", regionFactor);
        
        VehicleType vehicleType = new VehicleType();
        vehicleType.setName(VEHICLE_TYPE);
        vehicleType.setFactor(VEHICLE_FACTOR_VALUE);
        
        when(configService.getBasePremium()).thenReturn(BASE_PREMIUM);
        when(regionRepository.findByPostalCode(POSTAL_CODE)).thenReturn(List.of(region));
        when(vehicleTypeRepository.findByName(VEHICLE_TYPE)).thenReturn(Optional.of(vehicleType));
        when(mileageFactorRepository.findByMileage(ANNUAL_MILEAGE)).thenReturn(Optional.empty());
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            calculationService.calculatePremium(request);
        });
        
        assertTrue(exception.getMessage().contains("No mileage factor found"));
    }
}
