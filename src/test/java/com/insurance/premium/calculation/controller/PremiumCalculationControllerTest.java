package com.insurance.premium.calculation.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.premium.calculation.domain.MileageFactor;
import com.insurance.premium.calculation.domain.Region;
import com.insurance.premium.calculation.domain.RegionFactor;
import com.insurance.premium.calculation.domain.VehicleType;
import com.insurance.premium.calculation.dto.PremiumCalculationRequest;
import com.insurance.premium.calculation.dto.PremiumCalculationResult;
import com.insurance.premium.calculation.repository.MileageFactorRepository;
import com.insurance.premium.calculation.repository.RegionRepository;
import com.insurance.premium.calculation.repository.VehicleTypeRepository;
import com.insurance.premium.calculation.service.PremiumCalculationService;
import com.insurance.premium.common.service.ConfigurationService;

@WebMvcTest(PremiumCalculationController.class)
class PremiumCalculationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PremiumCalculationService calculationService;

    @MockitoBean
    private RegionRepository regionRepository;

    @MockitoBean
    private VehicleTypeRepository vehicleTypeRepository;

    @MockitoBean
    private MileageFactorRepository mileageFactorRepository;
    
    @MockitoBean
    private ConfigurationService configurationService;

    private PremiumCalculationRequest validRequest;
    private PremiumCalculationResult calculationResult;
    private Region testRegion;
    private VehicleType testVehicleType;
    private MileageFactor testMileageFactor;

    @BeforeEach
    void setUp() {
        // Setup test data
        validRequest = new PremiumCalculationRequest("10115", "Kompaktklasse", 15000);

        calculationResult = new PremiumCalculationResult(
            "10115",
            "Kompaktklasse",
            15000,
            new BigDecimal("500.00"),
            new BigDecimal("1.5"),
            new BigDecimal("1.0"),
            new BigDecimal("1.2"),
            new BigDecimal("900.00")
        );

        // Setup region
        RegionFactor regionFactor = new RegionFactor();
        regionFactor.setId(1L);
        regionFactor.setFederalState("Berlin");
        regionFactor.setFactor(new BigDecimal("1.2"));

        testRegion = new Region();
        testRegion.setId(1L);
        testRegion.setPostalCode("10115");
        testRegion.setCity("Berlin");
        testRegion.setFederalState("Berlin");
        testRegion.setCountry("Germany");
        testRegion.setRegionFactor(regionFactor);

        // Setup vehicle type
        testVehicleType = new VehicleType();
        testVehicleType.setId(1L);
        testVehicleType.setName("Kompaktklasse");
        testVehicleType.setFactor(new BigDecimal("1.0"));

        // Setup mileage factor
        testMileageFactor = new MileageFactor();
        testMileageFactor.setId(1L);
        testMileageFactor.setMinMileage(10000);
        testMileageFactor.setMaxMileage(20000);
        testMileageFactor.setFactor(new BigDecimal("1.5"));
        
        // Setup default mock responses
        when(configurationService.getBasePremium()).thenReturn(new BigDecimal("500.00"));
    }

    @Test
    void calculatePremium_WithValidRequest_ReturnsCalculatedPremium() throws Exception {
        // Arrange
        when(calculationService.calculatePremium(any(PremiumCalculationRequest.class)))
                .thenReturn(calculationResult);

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/premium/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        PremiumCalculationResult response = objectMapper.readValue(responseJson, PremiumCalculationResult.class);

        assertEquals(0, calculationResult.basePremium().compareTo(response.basePremium()));
        assertEquals(0, calculationResult.regionFactor().compareTo(response.regionFactor()));
        assertEquals(0, calculationResult.vehicleTypeFactor().compareTo(response.vehicleTypeFactor()));
        assertEquals(0, calculationResult.mileageFactor().compareTo(response.mileageFactor()));
        assertEquals(0, calculationResult.premium().compareTo(response.premium()));

        verify(calculationService, times(1)).calculatePremium(any(PremiumCalculationRequest.class));
    }

    @Test
    void calculatePremium_WithInvalidRequest_Returns4xxClientError() throws Exception {
        // Arrange
        PremiumCalculationRequest invalidRequest = new PremiumCalculationRequest("", "Kompaktklasse", 15000);
        
        // Mock the service to throw an exception for invalid input
        when(calculationService.calculatePremium(any(PremiumCalculationRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid postal code"));

        // Act & Assert
        mockMvc.perform(post("/api/premium/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void calculatePremium_WhenServiceThrowsException_Returns4xxClientError() throws Exception {
        // Arrange
        when(calculationService.calculatePremium(any(PremiumCalculationRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid input"));

        // Act & Assert
        mockMvc.perform(post("/api/premium/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value("Invalid input"));

        verify(calculationService, times(1)).calculatePremium(any(PremiumCalculationRequest.class));
    }

    @Test
    void getPostcodes_ReturnsPagedPostcodes() throws Exception {
        // Arrange
        List<Region> regions = Arrays.asList(testRegion);
        Page<Region> pagedRegions = new PageImpl<>(regions);
        
        when(regionRepository.findAll(any(Pageable.class))).thenReturn(pagedRegions);

        // Act & Assert
        mockMvc.perform(get("/api/premium/postcodes")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].postalCode").value("10115"))
                .andExpect(jsonPath("$.content[0].city").value("Berlin"))
                .andExpect(jsonPath("$.content[0].federalState").value("Berlin"));

        verify(regionRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void getPostcodesByPrefix_ReturnsMatchingPostcodes() throws Exception {
        // Arrange
        when(regionRepository.findByPostalCodeStartingWith("101")).thenReturn(Arrays.asList(testRegion));

        // Act & Assert
        mockMvc.perform(get("/api/premium/postcodes/search/101"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].postalCode").value("10115"))
                .andExpect(jsonPath("$[0].city").value("Berlin"))
                .andExpect(jsonPath("$[0].federalState").value("Berlin"));

        verify(regionRepository, times(1)).findByPostalCodeStartingWith("101");
    }

    @Test
    void getRegionFactors_ReturnsAllRegionFactors() throws Exception {
        // Arrange
        when(regionRepository.findAll()).thenReturn(Arrays.asList(testRegion));

        // Act & Assert
        mockMvc.perform(get("/api/premium/factors/region"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Berlin"))
                .andExpect(jsonPath("$[0].factor").value("1.2"));

        verify(regionRepository, times(1)).findAll();
    }

    @Test
    void getVehicleTypeFactors_ReturnsAllVehicleTypeFactors() throws Exception {
        // Arrange
        when(vehicleTypeRepository.findAll()).thenReturn(Arrays.asList(testVehicleType));

        // Act & Assert
        mockMvc.perform(get("/api/premium/factors/vehicle"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Kompaktklasse"))
                .andExpect(jsonPath("$[0].factor").value("1.0"));

        verify(vehicleTypeRepository, times(1)).findAll();
    }

    @Test
    void getMileageFactors_ReturnsAllMileageFactors() throws Exception {
        // Arrange
        when(mileageFactorRepository.findAllByOrderByMinMileageAsc()).thenReturn(Arrays.asList(testMileageFactor));

        // Act & Assert
        mockMvc.perform(get("/api/premium/factors/mileage"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("10000-20000 km"))
                .andExpect(jsonPath("$[0].factor").value("1.5"));

        verify(mileageFactorRepository, times(1)).findAllByOrderByMinMileageAsc();
    }

    @Test
    void getAllFactors_ReturnsAllFactors() throws Exception {
        // Arrange
        when(regionRepository.findAll()).thenReturn(Arrays.asList(testRegion));
        when(vehicleTypeRepository.findAll()).thenReturn(Arrays.asList(testVehicleType));
        when(mileageFactorRepository.findAllByOrderByMinMileageAsc()).thenReturn(Arrays.asList(testMileageFactor));
        when(configurationService.getBasePremium()).thenReturn(new BigDecimal("500.00"));

        // Act & Assert
        mockMvc.perform(get("/api/premium/factors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.regionFactors[0].name").value("Berlin"))
                .andExpect(jsonPath("$.vehicleTypeFactors[0].name").value("Kompaktklasse"))
                .andExpect(jsonPath("$.mileageFactors[0].name").value("10000-20000 km"));

        verify(regionRepository, times(1)).findAll();
        verify(vehicleTypeRepository, times(1)).findAll();
        verify(mileageFactorRepository, times(1)).findAllByOrderByMinMileageAsc();
    }
    
    // Input validation tests to protect against injection attacks and malicious inputs
    
    @Test
    void calculatePremium_WithSqlInjectionInPostalCode_Returns4xxClientError() throws Exception {
        // Arrange
        PremiumCalculationRequest maliciousRequest = new PremiumCalculationRequest(
            "10115' OR '1'='1", // SQL injection attempt
            "Kompaktklasse",
            15000
        );
        
        when(calculationService.calculatePremium(any(PremiumCalculationRequest.class)))
            .thenThrow(new IllegalArgumentException("Invalid postal code format"));
            
        // Act & Assert
        mockMvc.perform(post("/api/premium/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousRequest)))
                .andExpect(status().is4xxClientError()); // Accept any 4xx error
    }
    
    @Test
    void calculatePremium_WithXssInVehicleType_Returns4xxClientError() throws Exception {
        // Arrange
        PremiumCalculationRequest maliciousRequest = new PremiumCalculationRequest(
            "10115",
            "<script>alert('XSS')</script>", // XSS attempt
            15000
        );
        
        when(calculationService.calculatePremium(any(PremiumCalculationRequest.class)))
            .thenThrow(new IllegalArgumentException("Invalid vehicle type"));
            
        // Act & Assert
        mockMvc.perform(post("/api/premium/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousRequest)))
                .andExpect(status().is4xxClientError()); // Accept any 4xx error
    }
    
    @Test
    void calculatePremium_WithExtremelyLargeMileage_Returns4xxClientError() throws Exception {
        // Arrange
        PremiumCalculationRequest maliciousRequest = new PremiumCalculationRequest(
            "10115",
            "Kompaktklasse",
            Integer.MAX_VALUE // Extremely large value
        );
        
        when(calculationService.calculatePremium(any(PremiumCalculationRequest.class)))
            .thenThrow(new IllegalArgumentException("Mileage value out of acceptable range"));
            
        // Act & Assert
        mockMvc.perform(post("/api/premium/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousRequest)))
                .andExpect(status().is4xxClientError()); // Accept any 4xx error
    }
    
    @Test
    void getPostcodesByPrefix_WithSqlInjectionInPrefix_ReturnsEmptyList() throws Exception {
        // Arrange
        String maliciousPrefix = "' OR '1'='1"; // SQL injection attempt
        
        when(regionRepository.findByPostalCodeStartingWith(maliciousPrefix))
            .thenReturn(List.of()); // Assuming the repository safely handles this
            
        // Act & Assert
        mockMvc.perform(get("/api/premium/postcodes/search/" + maliciousPrefix))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
                
        verify(regionRepository, times(1)).findByPostalCodeStartingWith(maliciousPrefix);
    }
    
    @Test
    void getPostcodes_WithNegativePage_ReturnsEmptyPage() throws Exception {
        // Mock the repository to return empty page for invalid pagination
        when(regionRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());
        
        // Act & Assert
        mockMvc.perform(get("/api/premium/postcodes")
                .param("page", "-1") // Negative page number
                .param("size", "10"))
                .andExpect(status().isOk()) // The controller returns 200 OK with empty page
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }
    
    @Test
    void getPostcodes_WithExcessivePageSize_ReturnsEmptyPage() throws Exception {
        // Mock the repository to return empty page for invalid pagination
        when(regionRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());
        
        // Act & Assert
        mockMvc.perform(get("/api/premium/postcodes")
                .param("page", "0")
                .param("size", "10000")) // Extremely large page size
                .andExpect(status().isOk()) // The controller returns 200 OK with empty page
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}
