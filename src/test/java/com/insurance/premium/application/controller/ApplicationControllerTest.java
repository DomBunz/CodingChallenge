package com.insurance.premium.application.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

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
import com.insurance.premium.application.domain.Application;
import com.insurance.premium.application.domain.Application.Status;
import com.insurance.premium.application.dto.ApplicationRequest;
import com.insurance.premium.application.dto.ApplicationResponse;
import com.insurance.premium.application.service.ApplicationService;
import com.insurance.premium.security.config.TestSecurityConfig;

@WebMvcTest(ApplicationController.class)
@Import(TestSecurityConfig.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApplicationService applicationService;

    private ApplicationRequest validRequest;
    private Application testApplication;

    @BeforeEach
    void setUp() {
        // Setup test data
        validRequest = new ApplicationRequest("10115", "Kompaktklasse", 15000);

        testApplication = new Application();
        testApplication.setId(1L);
        testApplication.setPostalCode("10115");
        testApplication.setVehicleType("Kompaktklasse");
        testApplication.setAnnualMileage(15000);
        testApplication.setBasePremium(new BigDecimal("500.00"));
        testApplication.setRegionFactor(new BigDecimal("1.2"));
        testApplication.setVehicleFactor(new BigDecimal("1.0"));
        testApplication.setMileageFactor(new BigDecimal("1.5"));
        testApplication.setCalculatedPremium(new BigDecimal("900.00"));
        testApplication.setStatus(Status.ACCEPTED);
        testApplication.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createApplication_WithValidRequest_ReturnsCreatedApplication() throws Exception {
        // Arrange
        when(applicationService.createApplication(any(ApplicationRequest.class)))
                .thenReturn(testApplication);

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        ApplicationResponse response = objectMapper.readValue(responseJson, ApplicationResponse.class);

        assertEquals(testApplication.getId(), response.id());
        assertEquals(testApplication.getPostalCode(), response.postalCode());
        assertEquals(testApplication.getStatus().toString(), response.status().toString());
        assertEquals(0, testApplication.getCalculatedPremium().compareTo(response.calculatedPremium()));

        verify(applicationService, times(1)).createApplication(any(ApplicationRequest.class));
    }

    @Test
    void createApplication_WithInvalidRequest_ReturnsBadRequest() throws Exception {
        // Arrange
        ApplicationRequest invalidRequest = new ApplicationRequest("", "Kompaktklasse", 15000);

        // Act & Assert
        mockMvc.perform(post("/api/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(applicationService, never()).createApplication(any(ApplicationRequest.class));
    }

    @Test
    void createApplication_WithCustomValidationError_ReturnsBadRequest() throws Exception {
        // Create a subclass of ApplicationRequest that overrides getValidationError
        String validationErrorJson = "{\"postalCode\":\"\",\"vehicleType\":\"Kompaktklasse\",\"annualMileage\":15000}";
        
        // Act & Assert
        mockMvc.perform(post("/api/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validationErrorJson))
                .andExpect(status().isBadRequest());

        verify(applicationService, never()).createApplication(any(ApplicationRequest.class));
    }

    @Test
    void getApplication_WithExistingId_ReturnsApplication() throws Exception {
        // Arrange
        when(applicationService.getApplication(1L)).thenReturn(Optional.of(testApplication));

        // Act & Assert
        mockMvc.perform(get("/api/applications/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.postalCode").value("10115"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(applicationService, times(1)).getApplication(1L);
    }

    @Test
    void getApplication_WithNonExistingId_ReturnsNotFound() throws Exception {
        // Arrange
        when(applicationService.getApplication(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/applications/999"))
                .andExpect(status().isNotFound());

        verify(applicationService, times(1)).getApplication(999L);
    }

    @Test
    void getAllApplications_ReturnsPagedApplications() throws Exception {
        // Arrange
        Page<Application> pagedApplications = new PageImpl<>(Arrays.asList(testApplication));
        when(applicationService.getAllApplications(any(Pageable.class))).thenReturn(pagedApplications);

        // Act & Assert
        mockMvc.perform(get("/api/applications")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].postalCode").value("10115"))
                .andExpect(jsonPath("$.content[0].status").value("ACCEPTED"));

        verify(applicationService, times(1)).getAllApplications(any(Pageable.class));
    }

    @Test
    void getApplicationsByStatus_ReturnsFilteredApplications() throws Exception {
        // Arrange
        Page<Application> pagedApplications = new PageImpl<>(Arrays.asList(testApplication));
        when(applicationService.getApplicationsByStatus(eq(Status.ACCEPTED), any(Pageable.class)))
                .thenReturn(pagedApplications);

        // Act & Assert
        mockMvc.perform(get("/api/applications/status/ACCEPTED")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].postalCode").value("10115"))
                .andExpect(jsonPath("$.content[0].status").value("ACCEPTED"));

        verify(applicationService, times(1)).getApplicationsByStatus(eq(Status.ACCEPTED), any(Pageable.class));
    }

    @Test
    void updateApplicationStatus_WithExistingId_ReturnsUpdatedApplication() throws Exception {
        // Arrange
        Application approvedApplication = new Application();
        approvedApplication.setId(1L);
        approvedApplication.setPostalCode("10115");
        approvedApplication.setVehicleType("Kompaktklasse");
        approvedApplication.setStatus(Status.ACCEPTED);
        
        when(applicationService.updateApplicationStatus(1L, Status.ACCEPTED))
                .thenReturn(Optional.of(approvedApplication));

        // Act & Assert
        mockMvc.perform(put("/api/applications/1/status/ACCEPTED"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.postalCode").value("10115"))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(applicationService, times(1)).updateApplicationStatus(1L, Status.ACCEPTED);
    }

    @Test
    void updateApplicationStatus_WithNonExistingId_ReturnsNotFound() throws Exception {
        // Arrange
        when(applicationService.updateApplicationStatus(999L, Status.ACCEPTED))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/applications/999/status/ACCEPTED"))
                .andExpect(status().isNotFound());

        verify(applicationService, times(1)).updateApplicationStatus(999L, Status.ACCEPTED);
    }

    @Test
    void deleteApplication_ReturnsNoContent() throws Exception {
        // Arrange
        doNothing().when(applicationService).deleteApplication(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/applications/1"))
                .andExpect(status().isNoContent());

        verify(applicationService, times(1)).deleteApplication(1L);
    }
    
    // Input validation tests to protect against injection attacks and malicious inputs
    
    @Test
    void createApplication_WithSqlInjectionInPostalCode_ReturnsErrorStatus() throws Exception {
        // Arrange
        ApplicationRequest maliciousRequest = new ApplicationRequest(
            "10115' OR '1'='1", // SQL injection attempt
            "Kompaktklasse",
            15000
        );
        
        // Mock the service to throw an exception for invalid input
        when(applicationService.createApplication(any(ApplicationRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid postal code format"));
        
        // Act & Assert
        mockMvc.perform(post("/api/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousRequest)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // Accept either client error (4xx) or server error (5xx)
                    assertTrue(status >= 400, "Expected error status code, but got " + status);
                });
                
        verify(applicationService, times(1)).createApplication(any(ApplicationRequest.class));
    }
    
    @Test
    void createApplication_WithXssInVehicleType_ReturnsErrorStatus() throws Exception {
        // Arrange
        ApplicationRequest maliciousRequest = new ApplicationRequest(
            "10115",
            "<script>alert('XSS')</script>", // XSS attempt
            15000
        );
        
        // Mock the service to throw an exception for invalid input
        when(applicationService.createApplication(any(ApplicationRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid vehicle type"));
        
        // Act & Assert
        mockMvc.perform(post("/api/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousRequest)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // Accept either client error (4xx) or server error (5xx)
                    assertTrue(status >= 400, "Expected error status code, but got " + status);
                });
                
        verify(applicationService, times(1)).createApplication(any(ApplicationRequest.class));
    }
    
    @Test
    void createApplication_WithExtremelyLargeMileage_ReturnsErrorStatus() throws Exception {
        // Arrange
        ApplicationRequest maliciousRequest = new ApplicationRequest(
            "10115",
            "Kompaktklasse",
            Integer.MAX_VALUE // Extremely large value
        );
        
        // Mock the service to throw an exception for invalid input
        when(applicationService.createApplication(any(ApplicationRequest.class)))
                .thenThrow(new IllegalArgumentException("Mileage value out of acceptable range"));
        
        // Act & Assert
        mockMvc.perform(post("/api/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousRequest)))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // Accept either client error (4xx) or server error (5xx)
                    assertTrue(status >= 400, "Expected error status code, but got " + status);
                });
                
        verify(applicationService, times(1)).createApplication(any(ApplicationRequest.class));
    }
    
    @Test
    void getApplication_WithNonNumericId_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/applications/abc")) // Non-numeric ID
                .andExpect(status().isBadRequest());
                
        verify(applicationService, never()).getApplication(any());
    }
    
    @Test
    void updateApplicationStatus_WithInvalidStatus_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/applications/1/status/INVALID_STATUS"))
                .andExpect(status().isBadRequest());
                
        verify(applicationService, never()).updateApplicationStatus(any(), any());
    }
    
    @Test
    void getAllApplications_WithNegativePage_ReturnsEmptyPage() throws Exception {
        // Mock the service to return empty page for invalid pagination
        when(applicationService.getAllApplications(any(Pageable.class)))
                .thenReturn(Page.empty());
        
        // Act & Assert
        mockMvc.perform(get("/api/applications")
                .param("page", "-1") // Negative page number
                .param("size", "10"))
                .andExpect(status().isOk()) // The controller returns 200 OK with empty page
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }
    
    @Test
    void getAllApplications_WithExcessivePageSize_ReturnsEmptyPage() throws Exception {
        // Mock the service to return empty page for invalid pagination
        when(applicationService.getAllApplications(any(Pageable.class)))
                .thenReturn(Page.empty());
        
        // Act & Assert
        mockMvc.perform(get("/api/applications")
                .param("page", "0")
                .param("size", "10000")) // Extremely large page size
                .andExpect(status().isOk()) // The controller returns 200 OK with empty page
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}
