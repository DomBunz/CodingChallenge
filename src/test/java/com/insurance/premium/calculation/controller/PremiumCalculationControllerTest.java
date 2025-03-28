package com.insurance.premium.calculation.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.premium.calculation.dto.FactorResponse;
import com.insurance.premium.calculation.dto.PostcodeResponse;
import com.insurance.premium.calculation.dto.PremiumCalculationRequest;
import com.insurance.premium.calculation.dto.PremiumCalculationResult;
import com.insurance.premium.calculation.service.PremiumCalculationService;
import com.insurance.premium.security.config.TestSecurityConfig;

@WebMvcTest(PremiumCalculationController.class)
@Import(TestSecurityConfig.class)
class PremiumCalculationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PremiumCalculationService calculationService;

    private PremiumCalculationRequest validRequest;
    private PremiumCalculationResult calculationResult;
    private List<FactorResponse> regionFactors;
    private List<FactorResponse> vehicleFactors;
    private List<FactorResponse> mileageFactors;
    private List<PostcodeResponse> postcodes;
    private Page<PostcodeResponse> pagedPostcodes;

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
        
        // Setup factor responses
        regionFactors = Arrays.asList(
            new FactorResponse("Berlin", new BigDecimal("1.2"), "Region factor for Berlin")
        );
        
        vehicleFactors = Arrays.asList(
            new FactorResponse("Kompaktklasse", new BigDecimal("1.0"), "Vehicle type factor for Kompaktklasse")
        );
        
        mileageFactors = Arrays.asList(
            new FactorResponse("10000-20000 km", new BigDecimal("1.5"), "Mileage factor for 10000-20000 km per year")
        );
        
        // Setup postcode responses
        postcodes = Arrays.asList(
            new PostcodeResponse("10115", "Berlin", "Germany", "Berlin", "Berlin", "Mitte")
        );
        
        pagedPostcodes = new PageImpl<>(postcodes);
        
        // Setup default mock responses
        when(calculationService.getAllRegionFactors()).thenReturn(regionFactors);
        when(calculationService.getAllVehicleFactors()).thenReturn(vehicleFactors);
        when(calculationService.getAllMileageFactors()).thenReturn(mileageFactors);
        when(calculationService.getAllPostcodes(any(Pageable.class))).thenReturn(pagedPostcodes);
        when(calculationService.getPostcodesByPrefix(anyString())).thenReturn(postcodes);
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
        // Act & Assert
        mockMvc.perform(get("/api/premium/postcodes")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].postalCode").value("10115"))
                .andExpect(jsonPath("$.content[0].city").value("Berlin"))
                .andExpect(jsonPath("$.content[0].federalState").value("Berlin"));

        verify(calculationService, times(1)).getAllPostcodes(any(Pageable.class));
    }

    @Test
    void getPostcodesByPrefix_ReturnsMatchingPostcodes() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/premium/postcodes/search/101"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].postalCode").value("10115"))
                .andExpect(jsonPath("$[0].city").value("Berlin"))
                .andExpect(jsonPath("$[0].federalState").value("Berlin"));

        verify(calculationService, times(1)).getPostcodesByPrefix("101");
    }

    @Test
    void getRegionFactors_ReturnsAllRegionFactors() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/premium/factors/region"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Berlin"))
                .andExpect(jsonPath("$[0].factor").value("1.2"));

        verify(calculationService, times(1)).getAllRegionFactors();
    }

    @Test
    void getVehicleTypeFactors_ReturnsAllVehicleTypeFactors() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/premium/factors/vehicle"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Kompaktklasse"))
                .andExpect(jsonPath("$[0].factor").value("1.0"));

        verify(calculationService, times(1)).getAllVehicleFactors();
    }

    @Test
    void getMileageFactors_ReturnsAllMileageFactors() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/premium/factors/mileage"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("10000-20000 km"))
                .andExpect(jsonPath("$[0].factor").value("1.5"));

        verify(calculationService, times(1)).getAllMileageFactors();
    }

    @Test
    void getAllFactors_ReturnsAllFactors() throws Exception {
        // Arrange
        Map<String, List<FactorResponse>> allFactors = new HashMap<>();
        allFactors.put("regionFactors", regionFactors);
        allFactors.put("vehicleTypeFactors", vehicleFactors);
        allFactors.put("mileageFactors", mileageFactors);
        
        when(calculationService.getAllFactors()).thenReturn(allFactors);

        // Act & Assert
        mockMvc.perform(get("/api/premium/factors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.regionFactors[0].name").value("Berlin"))
                .andExpect(jsonPath("$.vehicleTypeFactors[0].name").value("Kompaktklasse"))
                .andExpect(jsonPath("$.mileageFactors[0].name").value("10000-20000 km"));

        verify(calculationService, times(1)).getAllFactors();
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
        
        when(calculationService.getPostcodesByPrefix(maliciousPrefix))
            .thenReturn(List.of()); // Assuming the service safely handles this
            
        // Act & Assert
        mockMvc.perform(get("/api/premium/postcodes/search/" + maliciousPrefix))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
                
        verify(calculationService, times(1)).getPostcodesByPrefix(maliciousPrefix);
    }
    
    @Test
    void getPostcodes_WithNegativePage_ReturnsEmptyPage() throws Exception {
        // Arrange
        when(calculationService.getAllPostcodes(any(Pageable.class)))
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
        // Arrange
        when(calculationService.getAllPostcodes(any(Pageable.class)))
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
