package com.insurance.premium.calculation.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.premium.calculation.domain.MileageFactor;
import com.insurance.premium.calculation.domain.RegionFactor;
import com.insurance.premium.calculation.domain.VehicleType;
import com.insurance.premium.calculation.dto.MileageFactorRequest;
import com.insurance.premium.calculation.dto.RegionFactorRequest;
import com.insurance.premium.calculation.dto.VehicleTypeRequest;
import com.insurance.premium.calculation.service.FactorManagementService;
import com.insurance.premium.security.config.TestSecurityConfig;

@WebMvcTest(FactorManagementController.class)
@Import(TestSecurityConfig.class)
class FactorManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FactorManagementService factorManagementService;

    private RegionFactor testRegionFactor;
    private RegionFactorRequest regionFactorRequest;
    
    private VehicleType testVehicleType;
    private VehicleTypeRequest vehicleTypeRequest;
    
    private MileageFactor testMileageFactor;
    private MileageFactorRequest mileageFactorRequest;

    @BeforeEach
    void setUp() {
        // Setup test region factor
        testRegionFactor = new RegionFactor();
        testRegionFactor.setId(1L);
        testRegionFactor.setFederalState("Berlin");
        testRegionFactor.setFactor(new BigDecimal("1.2"));
        
        regionFactorRequest = new RegionFactorRequest();
        regionFactorRequest.setFederalState("Berlin");
        regionFactorRequest.setFactor(new BigDecimal("1.2"));
        
        // Setup test vehicle type
        testVehicleType = new VehicleType();
        testVehicleType.setId(1L);
        testVehicleType.setName("Kompaktklasse");
        testVehicleType.setFactor(new BigDecimal("1.0"));
        
        vehicleTypeRequest = new VehicleTypeRequest();
        vehicleTypeRequest.setName("Kompaktklasse");
        vehicleTypeRequest.setFactor(new BigDecimal("1.0"));
        
        // Setup test mileage factor
        testMileageFactor = new MileageFactor();
        testMileageFactor.setId(1L);
        testMileageFactor.setMinMileage(0);
        testMileageFactor.setMaxMileage(10000);
        testMileageFactor.setFactor(new BigDecimal("0.8"));
        
        mileageFactorRequest = new MileageFactorRequest();
        mileageFactorRequest.setMinMileage(0);
        mileageFactorRequest.setMaxMileage(10000);
        mileageFactorRequest.setFactor(new BigDecimal("0.8"));
    }

    // Region Factor Tests
    
    @Test
    void getAllRegionFactors_ShouldReturnAllRegionFactors() throws Exception {
        // Arrange
        List<RegionFactor> regionFactors = Arrays.asList(testRegionFactor);
        when(factorManagementService.getAllRegionFactors()).thenReturn(regionFactors);
        
        // Act & Assert
        mockMvc.perform(get("/api/admin/premium/management/regions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(testRegionFactor.getId()))
                .andExpect(jsonPath("$[0].federalState").value(testRegionFactor.getFederalState()))
                .andExpect(jsonPath("$[0].factor").value(testRegionFactor.getFactor().doubleValue()));
        
        verify(factorManagementService, times(1)).getAllRegionFactors();
    }
    
    @Test
    void getRegionFactorById_WithExistingId_ShouldReturnRegionFactor() throws Exception {
        // Arrange
        Long id = 1L;
        when(factorManagementService.getRegionFactorById(id)).thenReturn(Optional.of(testRegionFactor));
        
        // Act & Assert
        mockMvc.perform(get("/api/admin/premium/management/regions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testRegionFactor.getId()))
                .andExpect(jsonPath("$.federalState").value(testRegionFactor.getFederalState()))
                .andExpect(jsonPath("$.factor").value(testRegionFactor.getFactor().doubleValue()));
        
        verify(factorManagementService, times(1)).getRegionFactorById(id);
    }
    
    @Test
    void getRegionFactorById_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long id = 999L;
        when(factorManagementService.getRegionFactorById(id)).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(get("/api/admin/premium/management/regions/{id}", id))
                .andExpect(status().isNotFound());
        
        verify(factorManagementService, times(1)).getRegionFactorById(id);
    }
    
    @Test
    void createRegionFactor_WithValidRequest_ShouldCreateAndReturnRegionFactor() throws Exception {
        // Arrange
        when(factorManagementService.createRegionFactor(any(RegionFactorRequest.class))).thenReturn(testRegionFactor);
        
        // Act & Assert
        mockMvc.perform(post("/api/admin/premium/management/regions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regionFactorRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testRegionFactor.getId()))
                .andExpect(jsonPath("$.federalState").value(testRegionFactor.getFederalState()))
                .andExpect(jsonPath("$.factor").value(testRegionFactor.getFactor().doubleValue()));
        
        verify(factorManagementService, times(1)).createRegionFactor(any(RegionFactorRequest.class));
    }
    
    @Test
    void createRegionFactor_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange
        RegionFactorRequest invalidRequest = new RegionFactorRequest();
        // Missing required fields
        
        // Act & Assert
        mockMvc.perform(post("/api/admin/premium/management/regions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(factorManagementService, never()).createRegionFactor(any(RegionFactorRequest.class));
    }
    
    @Test
    void updateRegionFactor_WithExistingIdAndValidRequest_ShouldUpdateAndReturnRegionFactor() throws Exception {
        // Arrange
        Long id = 1L;
        when(factorManagementService.updateRegionFactor(eq(id), any(RegionFactorRequest.class))).thenReturn(testRegionFactor);
        
        // Act & Assert
        mockMvc.perform(put("/api/admin/premium/management/regions/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regionFactorRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testRegionFactor.getId()))
                .andExpect(jsonPath("$.federalState").value(testRegionFactor.getFederalState()))
                .andExpect(jsonPath("$.factor").value(testRegionFactor.getFactor().doubleValue()));
        
        verify(factorManagementService, times(1)).updateRegionFactor(eq(id), any(RegionFactorRequest.class));
    }
    
    @Test
    void updateRegionFactor_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long id = 999L;
        when(factorManagementService.updateRegionFactor(eq(id), any(RegionFactorRequest.class)))
                .thenThrow(new IllegalArgumentException("Region factor not found with ID: " + id));
        
        // Act & Assert
        mockMvc.perform(put("/api/admin/premium/management/regions/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regionFactorRequest)))
                .andExpect(status().isNotFound());
        
        verify(factorManagementService, times(1)).updateRegionFactor(eq(id), any(RegionFactorRequest.class));
    }
    
    @Test
    void deleteRegionFactor_WithExistingId_ShouldReturnNoContent() throws Exception {
        // Arrange
        Long id = 1L;
        doNothing().when(factorManagementService).deleteRegionFactor(id);
        
        // Act & Assert
        mockMvc.perform(delete("/api/admin/premium/management/regions/{id}", id))
                .andExpect(status().isNoContent());
        
        verify(factorManagementService, times(1)).deleteRegionFactor(id);
    }
    
    @Test
    void deleteRegionFactor_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long id = 999L;
        doThrow(new IllegalArgumentException("Region factor not found with ID: " + id))
                .when(factorManagementService).deleteRegionFactor(id);
        
        // Act & Assert
        mockMvc.perform(delete("/api/admin/premium/management/regions/{id}", id))
                .andExpect(status().isNotFound());
        
        verify(factorManagementService, times(1)).deleteRegionFactor(id);
    }
    
    // Vehicle Type Tests
    
    @Test
    void getAllVehicleTypes_ShouldReturnAllVehicleTypes() throws Exception {
        // Arrange
        List<VehicleType> vehicleTypes = Arrays.asList(testVehicleType);
        when(factorManagementService.getAllVehicleTypes()).thenReturn(vehicleTypes);
        
        // Act & Assert
        mockMvc.perform(get("/api/admin/premium/management/vehicles"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(testVehicleType.getId()))
                .andExpect(jsonPath("$[0].name").value(testVehicleType.getName()))
                .andExpect(jsonPath("$[0].factor").value(testVehicleType.getFactor().doubleValue()));
        
        verify(factorManagementService, times(1)).getAllVehicleTypes();
    }
    
    @Test
    void getVehicleTypeById_WithExistingId_ShouldReturnVehicleType() throws Exception {
        // Arrange
        Long id = 1L;
        when(factorManagementService.getVehicleTypeById(id)).thenReturn(Optional.of(testVehicleType));
        
        // Act & Assert
        mockMvc.perform(get("/api/admin/premium/management/vehicles/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testVehicleType.getId()))
                .andExpect(jsonPath("$.name").value(testVehicleType.getName()))
                .andExpect(jsonPath("$.factor").value(testVehicleType.getFactor().doubleValue()));
        
        verify(factorManagementService, times(1)).getVehicleTypeById(id);
    }
    
    @Test
    void createVehicleType_WithValidRequest_ShouldCreateAndReturnVehicleType() throws Exception {
        // Arrange
        when(factorManagementService.createVehicleType(any(VehicleTypeRequest.class))).thenReturn(testVehicleType);
        
        // Act & Assert
        mockMvc.perform(post("/api/admin/premium/management/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehicleTypeRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testVehicleType.getId()))
                .andExpect(jsonPath("$.name").value(testVehicleType.getName()))
                .andExpect(jsonPath("$.factor").value(testVehicleType.getFactor().doubleValue()));
        
        verify(factorManagementService, times(1)).createVehicleType(any(VehicleTypeRequest.class));
    }
    
    @Test
    void deleteVehicleType_WithExistingId_ShouldReturnNoContent() throws Exception {
        // Arrange
        Long id = 1L;
        doNothing().when(factorManagementService).deleteVehicleType(id);
        
        // Act & Assert
        mockMvc.perform(delete("/api/admin/premium/management/vehicles/{id}", id))
                .andExpect(status().isNoContent());
        
        verify(factorManagementService, times(1)).deleteVehicleType(id);
    }
    
    // Mileage Factor Tests
    
    @Test
    void getAllMileageFactors_ShouldReturnAllMileageFactors() throws Exception {
        // Arrange
        List<MileageFactor> mileageFactors = Arrays.asList(testMileageFactor);
        when(factorManagementService.getAllMileageFactors()).thenReturn(mileageFactors);
        
        // Act & Assert
        mockMvc.perform(get("/api/admin/premium/management/mileages"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(testMileageFactor.getId()))
                .andExpect(jsonPath("$[0].minMileage").value(testMileageFactor.getMinMileage()))
                .andExpect(jsonPath("$[0].maxMileage").value(testMileageFactor.getMaxMileage()))
                .andExpect(jsonPath("$[0].factor").value(testMileageFactor.getFactor().doubleValue()));
        
        verify(factorManagementService, times(1)).getAllMileageFactors();
    }
    
    @Test
    void getMileageFactorById_WithExistingId_ShouldReturnMileageFactor() throws Exception {
        // Arrange
        Long id = 1L;
        when(factorManagementService.getMileageFactorById(id)).thenReturn(Optional.of(testMileageFactor));
        
        // Act & Assert
        mockMvc.perform(get("/api/admin/premium/management/mileages/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testMileageFactor.getId()))
                .andExpect(jsonPath("$.minMileage").value(testMileageFactor.getMinMileage()))
                .andExpect(jsonPath("$.maxMileage").value(testMileageFactor.getMaxMileage()))
                .andExpect(jsonPath("$.factor").value(testMileageFactor.getFactor().doubleValue()));
        
        verify(factorManagementService, times(1)).getMileageFactorById(id);
    }
    
    @Test
    void createMileageFactor_WithValidRequest_ShouldCreateAndReturnMileageFactor() throws Exception {
        // Arrange
        when(factorManagementService.createMileageFactor(any(MileageFactorRequest.class))).thenReturn(testMileageFactor);
        
        // Act & Assert
        mockMvc.perform(post("/api/admin/premium/management/mileages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mileageFactorRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testMileageFactor.getId()))
                .andExpect(jsonPath("$.minMileage").value(testMileageFactor.getMinMileage()))
                .andExpect(jsonPath("$.maxMileage").value(testMileageFactor.getMaxMileage()))
                .andExpect(jsonPath("$.factor").value(testMileageFactor.getFactor().doubleValue()));
        
        verify(factorManagementService, times(1)).createMileageFactor(any(MileageFactorRequest.class));
    }
    
    @Test
    void createMileageFactor_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange
        MileageFactorRequest invalidRequest = new MileageFactorRequest();
        // Missing required fields
        
        // Act & Assert
        mockMvc.perform(post("/api/admin/premium/management/mileages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        
        verify(factorManagementService, never()).createMileageFactor(any(MileageFactorRequest.class));
    }
    
    @Test
    void updateMileageFactor_WithExistingIdAndValidRequest_ShouldUpdateAndReturnMileageFactor() throws Exception {
        // Arrange
        Long id = 1L;
        when(factorManagementService.updateMileageFactor(eq(id), any(MileageFactorRequest.class))).thenReturn(testMileageFactor);
        
        // Act & Assert
        mockMvc.perform(put("/api/admin/premium/management/mileages/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mileageFactorRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testMileageFactor.getId()))
                .andExpect(jsonPath("$.minMileage").value(testMileageFactor.getMinMileage()))
                .andExpect(jsonPath("$.maxMileage").value(testMileageFactor.getMaxMileage()))
                .andExpect(jsonPath("$.factor").value(testMileageFactor.getFactor().doubleValue()));
        
        verify(factorManagementService, times(1)).updateMileageFactor(eq(id), any(MileageFactorRequest.class));
    }
    
    @Test
    void deleteMileageFactor_WithExistingId_ShouldReturnNoContent() throws Exception {
        // Arrange
        Long id = 1L;
        doNothing().when(factorManagementService).deleteMileageFactor(id);
        
        // Act & Assert
        mockMvc.perform(delete("/api/admin/premium/management/mileages/{id}", id))
                .andExpect(status().isNoContent());
        
        verify(factorManagementService, times(1)).deleteMileageFactor(id);
    }
}
