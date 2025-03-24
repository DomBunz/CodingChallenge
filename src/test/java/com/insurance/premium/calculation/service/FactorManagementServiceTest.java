package com.insurance.premium.calculation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.insurance.premium.calculation.domain.MileageFactor;
import com.insurance.premium.calculation.domain.RegionFactor;
import com.insurance.premium.calculation.domain.VehicleType;
import com.insurance.premium.calculation.dto.MileageFactorRequest;
import com.insurance.premium.calculation.dto.RegionFactorRequest;
import com.insurance.premium.calculation.dto.VehicleTypeRequest;
import com.insurance.premium.calculation.repository.MileageFactorRepository;
import com.insurance.premium.calculation.repository.RegionFactorRepository;
import com.insurance.premium.calculation.repository.VehicleTypeRepository;

@ExtendWith(MockitoExtension.class)
class FactorManagementServiceTest {

    @Mock
    private RegionFactorRepository regionFactorRepository;
    
    @Mock
    private VehicleTypeRepository vehicleTypeRepository;
    
    @Mock
    private MileageFactorRepository mileageFactorRepository;
    
    @InjectMocks
    private FactorManagementService factorManagementService;
    
    // Region Factor Tests
    
    @Test
    void getAllRegionFactors_ShouldReturnAllRegionFactors() {
        // Arrange
        RegionFactor factor1 = new RegionFactor();
        factor1.setId(1L);
        factor1.setFederalState("Berlin");
        factor1.setFactor(new BigDecimal("1.2"));
        
        RegionFactor factor2 = new RegionFactor();
        factor2.setId(2L);
        factor2.setFederalState("Bayern");
        factor2.setFactor(new BigDecimal("1.1"));
        
        List<RegionFactor> expectedFactors = Arrays.asList(factor1, factor2);
        
        when(regionFactorRepository.findAll()).thenReturn(expectedFactors);
        
        // Act
        List<RegionFactor> result = factorManagementService.getAllRegionFactors();
        
        // Assert
        assertEquals(expectedFactors.size(), result.size());
        assertEquals(expectedFactors, result);
        verify(regionFactorRepository, times(1)).findAll();
    }
    
    @Test
    void getRegionFactorById_WithExistingId_ShouldReturnRegionFactor() {
        // Arrange
        Long id = 1L;
        RegionFactor expectedFactor = new RegionFactor();
        expectedFactor.setId(id);
        expectedFactor.setFederalState("Berlin");
        expectedFactor.setFactor(new BigDecimal("1.2"));
        
        when(regionFactorRepository.findById(id)).thenReturn(Optional.of(expectedFactor));
        
        // Act
        Optional<RegionFactor> result = factorManagementService.getRegionFactorById(id);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedFactor, result.get());
        verify(regionFactorRepository, times(1)).findById(id);
    }
    
    @Test
    void getRegionFactorById_WithNonExistingId_ShouldReturnEmptyOptional() {
        // Arrange
        Long id = 999L;
        when(regionFactorRepository.findById(id)).thenReturn(Optional.empty());
        
        // Act
        Optional<RegionFactor> result = factorManagementService.getRegionFactorById(id);
        
        // Assert
        assertFalse(result.isPresent());
        verify(regionFactorRepository, times(1)).findById(id);
    }
    
    @Test
    void createRegionFactor_WithValidRequest_ShouldCreateAndReturnRegionFactor() {
        // Arrange
        RegionFactorRequest request = new RegionFactorRequest();
        request.setFederalState("Berlin");
        request.setFactor(new BigDecimal("1.2"));
        
        RegionFactor savedFactor = new RegionFactor();
        savedFactor.setId(1L);
        savedFactor.setFederalState(request.getFederalState());
        savedFactor.setFactor(request.getFactor());
        
        when(regionFactorRepository.save(any(RegionFactor.class))).thenReturn(savedFactor);
        
        // Act
        RegionFactor result = factorManagementService.createRegionFactor(request);
        
        // Assert
        assertNotNull(result);
        assertEquals(savedFactor.getId(), result.getId());
        assertEquals(request.getFederalState(), result.getFederalState());
        assertEquals(request.getFactor(), result.getFactor());
        verify(regionFactorRepository, times(1)).save(any(RegionFactor.class));
    }
    
    @Test
    void updateRegionFactor_WithExistingIdAndValidRequest_ShouldUpdateAndReturnRegionFactor() {
        // Arrange
        Long id = 1L;
        RegionFactorRequest request = new RegionFactorRequest();
        request.setFederalState("Bayern");
        request.setFactor(new BigDecimal("1.1"));
        
        RegionFactor existingFactor = new RegionFactor();
        existingFactor.setId(id);
        existingFactor.setFederalState("Berlin");
        existingFactor.setFactor(new BigDecimal("1.2"));
        
        RegionFactor updatedFactor = new RegionFactor();
        updatedFactor.setId(id);
        updatedFactor.setFederalState(request.getFederalState());
        updatedFactor.setFactor(request.getFactor());
        
        when(regionFactorRepository.findById(id)).thenReturn(Optional.of(existingFactor));
        when(regionFactorRepository.save(any(RegionFactor.class))).thenReturn(updatedFactor);
        
        // Act
        RegionFactor result = factorManagementService.updateRegionFactor(id, request);
        
        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(request.getFederalState(), result.getFederalState());
        assertEquals(request.getFactor(), result.getFactor());
        verify(regionFactorRepository, times(1)).findById(id);
        verify(regionFactorRepository, times(1)).save(any(RegionFactor.class));
    }
    
    @Test
    void updateRegionFactor_WithNonExistingId_ShouldThrowException() {
        // Arrange
        Long id = 999L;
        RegionFactorRequest request = new RegionFactorRequest();
        request.setFederalState("Bayern");
        request.setFactor(new BigDecimal("1.1"));
        
        when(regionFactorRepository.findById(id)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            factorManagementService.updateRegionFactor(id, request);
        });
        
        assertEquals("Region factor not found with ID: " + id, exception.getMessage());
        verify(regionFactorRepository, times(1)).findById(id);
        verify(regionFactorRepository, never()).save(any(RegionFactor.class));
    }
    
    @Test
    void deleteRegionFactor_WithExistingId_ShouldDeleteRegionFactor() {
        // Arrange
        Long id = 1L;
        when(regionFactorRepository.existsById(id)).thenReturn(true);
        
        // Act
        factorManagementService.deleteRegionFactor(id);
        
        // Assert
        verify(regionFactorRepository, times(1)).existsById(id);
        verify(regionFactorRepository, times(1)).deleteById(id);
    }
    
    @Test
    void deleteRegionFactor_WithNonExistingId_ShouldThrowException() {
        // Arrange
        Long id = 999L;
        when(regionFactorRepository.existsById(id)).thenReturn(false);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            factorManagementService.deleteRegionFactor(id);
        });
        
        assertEquals("Region factor not found with ID: " + id, exception.getMessage());
        verify(regionFactorRepository, times(1)).existsById(id);
        verify(regionFactorRepository, never()).deleteById(id);
    }
    
    // Vehicle Type Tests
    
    @Test
    void getAllVehicleTypes_ShouldReturnAllVehicleTypes() {
        // Arrange
        VehicleType type1 = new VehicleType();
        type1.setId(1L);
        type1.setName("Kompaktklasse");
        type1.setFactor(new BigDecimal("1.0"));
        
        VehicleType type2 = new VehicleType();
        type2.setId(2L);
        type2.setName("SUV");
        type2.setFactor(new BigDecimal("1.3"));
        
        List<VehicleType> expectedTypes = Arrays.asList(type1, type2);
        
        when(vehicleTypeRepository.findAll()).thenReturn(expectedTypes);
        
        // Act
        List<VehicleType> result = factorManagementService.getAllVehicleTypes();
        
        // Assert
        assertEquals(expectedTypes.size(), result.size());
        assertEquals(expectedTypes, result);
        verify(vehicleTypeRepository, times(1)).findAll();
    }
    
    @Test
    void getVehicleTypeById_WithExistingId_ShouldReturnVehicleType() {
        // Arrange
        Long id = 1L;
        VehicleType expectedType = new VehicleType();
        expectedType.setId(id);
        expectedType.setName("Kompaktklasse");
        expectedType.setFactor(new BigDecimal("1.0"));
        
        when(vehicleTypeRepository.findById(id)).thenReturn(Optional.of(expectedType));
        
        // Act
        Optional<VehicleType> result = factorManagementService.getVehicleTypeById(id);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedType, result.get());
        verify(vehicleTypeRepository, times(1)).findById(id);
    }
    
    @Test
    void createVehicleType_WithValidRequest_ShouldCreateAndReturnVehicleType() {
        // Arrange
        VehicleTypeRequest request = new VehicleTypeRequest();
        request.setName("Kompaktklasse");
        request.setFactor(new BigDecimal("1.0"));
        
        VehicleType savedType = new VehicleType();
        savedType.setId(1L);
        savedType.setName(request.getName());
        savedType.setFactor(request.getFactor());
        
        when(vehicleTypeRepository.save(any(VehicleType.class))).thenReturn(savedType);
        
        // Act
        VehicleType result = factorManagementService.createVehicleType(request);
        
        // Assert
        assertNotNull(result);
        assertEquals(savedType.getId(), result.getId());
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getFactor(), result.getFactor());
        verify(vehicleTypeRepository, times(1)).save(any(VehicleType.class));
    }
    
    @Test
    void updateVehicleType_WithExistingIdAndValidRequest_ShouldUpdateAndReturnVehicleType() {
        // Arrange
        Long id = 1L;
        VehicleTypeRequest request = new VehicleTypeRequest();
        request.setName("SUV");
        request.setFactor(new BigDecimal("1.3"));
        
        VehicleType existingType = new VehicleType();
        existingType.setId(id);
        existingType.setName("Kompaktklasse");
        existingType.setFactor(new BigDecimal("1.0"));
        
        VehicleType updatedType = new VehicleType();
        updatedType.setId(id);
        updatedType.setName(request.getName());
        updatedType.setFactor(request.getFactor());
        
        when(vehicleTypeRepository.findById(id)).thenReturn(Optional.of(existingType));
        when(vehicleTypeRepository.save(any(VehicleType.class))).thenReturn(updatedType);
        
        // Act
        VehicleType result = factorManagementService.updateVehicleType(id, request);
        
        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(request.getName(), result.getName());
        assertEquals(request.getFactor(), result.getFactor());
        verify(vehicleTypeRepository, times(1)).findById(id);
        verify(vehicleTypeRepository, times(1)).save(any(VehicleType.class));
    }
    
    @Test
    void deleteVehicleType_WithExistingId_ShouldDeleteVehicleType() {
        // Arrange
        Long id = 1L;
        when(vehicleTypeRepository.existsById(id)).thenReturn(true);
        
        // Act
        factorManagementService.deleteVehicleType(id);
        
        // Assert
        verify(vehicleTypeRepository, times(1)).existsById(id);
        verify(vehicleTypeRepository, times(1)).deleteById(id);
    }
    
    // Mileage Factor Tests
    
    @Test
    void getAllMileageFactors_ShouldReturnAllMileageFactorsSortedByMinMileage() {
        // Arrange
        MileageFactor factor1 = new MileageFactor();
        factor1.setId(1L);
        factor1.setMinMileage(0);
        factor1.setMaxMileage(10000);
        factor1.setFactor(new BigDecimal("0.8"));
        
        MileageFactor factor2 = new MileageFactor();
        factor2.setId(2L);
        factor2.setMinMileage(10001);
        factor2.setMaxMileage(20000);
        factor2.setFactor(new BigDecimal("1.0"));
        
        List<MileageFactor> expectedFactors = Arrays.asList(factor1, factor2);
        
        when(mileageFactorRepository.findAllByOrderByMinMileageAsc()).thenReturn(expectedFactors);
        
        // Act
        List<MileageFactor> result = factorManagementService.getAllMileageFactors();
        
        // Assert
        assertEquals(expectedFactors.size(), result.size());
        assertEquals(expectedFactors, result);
        verify(mileageFactorRepository, times(1)).findAllByOrderByMinMileageAsc();
    }
    
    @Test
    void getMileageFactorById_WithExistingId_ShouldReturnMileageFactor() {
        // Arrange
        Long id = 1L;
        MileageFactor expectedFactor = new MileageFactor();
        expectedFactor.setId(id);
        expectedFactor.setMinMileage(0);
        expectedFactor.setMaxMileage(10000);
        expectedFactor.setFactor(new BigDecimal("0.8"));
        
        when(mileageFactorRepository.findById(id)).thenReturn(Optional.of(expectedFactor));
        
        // Act
        Optional<MileageFactor> result = factorManagementService.getMileageFactorById(id);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedFactor, result.get());
        verify(mileageFactorRepository, times(1)).findById(id);
    }
    
    @Test
    void createMileageFactor_WithValidRequest_ShouldCreateAndReturnMileageFactor() {
        // Arrange
        MileageFactorRequest request = new MileageFactorRequest();
        request.setMinMileage(0);
        request.setMaxMileage(10000);
        request.setFactor(new BigDecimal("0.8"));
        
        MileageFactor savedFactor = new MileageFactor();
        savedFactor.setId(1L);
        savedFactor.setMinMileage(request.getMinMileage());
        savedFactor.setMaxMileage(request.getMaxMileage());
        savedFactor.setFactor(request.getFactor());
        
        when(mileageFactorRepository.save(any(MileageFactor.class))).thenReturn(savedFactor);
        
        // Act
        MileageFactor result = factorManagementService.createMileageFactor(request);
        
        // Assert
        assertNotNull(result);
        assertEquals(savedFactor.getId(), result.getId());
        assertEquals(request.getMinMileage(), result.getMinMileage());
        assertEquals(request.getMaxMileage(), result.getMaxMileage());
        assertEquals(request.getFactor(), result.getFactor());
        verify(mileageFactorRepository, times(1)).save(any(MileageFactor.class));
    }
    
    @Test
    void createMileageFactor_WithInvalidRequest_ShouldThrowException() {
        // Arrange
        MileageFactorRequest request = new MileageFactorRequest();
        request.setMinMileage(10000);
        request.setMaxMileage(5000); // Invalid: min > max
        request.setFactor(new BigDecimal("0.8"));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            factorManagementService.createMileageFactor(request);
        });
        
        assertEquals("Minimum mileage must be less than maximum mileage", exception.getMessage());
        verify(mileageFactorRepository, never()).save(any(MileageFactor.class));
    }
    
    @Test
    void updateMileageFactor_WithExistingIdAndValidRequest_ShouldUpdateAndReturnMileageFactor() {
        // Arrange
        Long id = 1L;
        MileageFactorRequest request = new MileageFactorRequest();
        request.setMinMileage(5000);
        request.setMaxMileage(15000);
        request.setFactor(new BigDecimal("1.0"));
        
        MileageFactor existingFactor = new MileageFactor();
        existingFactor.setId(id);
        existingFactor.setMinMileage(0);
        existingFactor.setMaxMileage(10000);
        existingFactor.setFactor(new BigDecimal("0.8"));
        
        MileageFactor updatedFactor = new MileageFactor();
        updatedFactor.setId(id);
        updatedFactor.setMinMileage(request.getMinMileage());
        updatedFactor.setMaxMileage(request.getMaxMileage());
        updatedFactor.setFactor(request.getFactor());
        
        when(mileageFactorRepository.findById(id)).thenReturn(Optional.of(existingFactor));
        when(mileageFactorRepository.save(any(MileageFactor.class))).thenReturn(updatedFactor);
        
        // Act
        MileageFactor result = factorManagementService.updateMileageFactor(id, request);
        
        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(request.getMinMileage(), result.getMinMileage());
        assertEquals(request.getMaxMileage(), result.getMaxMileage());
        assertEquals(request.getFactor(), result.getFactor());
        verify(mileageFactorRepository, times(1)).findById(id);
        verify(mileageFactorRepository, times(1)).save(any(MileageFactor.class));
    }
    
    @Test
    void deleteMileageFactor_WithExistingId_ShouldDeleteMileageFactor() {
        // Arrange
        Long id = 1L;
        when(mileageFactorRepository.existsById(id)).thenReturn(true);
        
        // Act
        factorManagementService.deleteMileageFactor(id);
        
        // Assert
        verify(mileageFactorRepository, times(1)).existsById(id);
        verify(mileageFactorRepository, times(1)).deleteById(id);
    }
}
