package com.insurance.premium.application.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import com.insurance.premium.application.domain.Application;
import com.insurance.premium.application.domain.Application.Status;
import com.insurance.premium.security.domain.User;
import com.insurance.premium.security.repository.UserRepository;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ApplicationRepositoryTest {

    @Autowired
    private ApplicationRepository applicationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private LocalDateTime now;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        applicationRepository.deleteAll();
        now = LocalDateTime.now();
        
        // Create a test user for applications
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEnabled(true);
        testUser.setAccountNonExpired(true);
        testUser.setAccountNonLocked(true);
        testUser.setCredentialsNonExpired(true);
        testUser = userRepository.save(testUser);
    }
    
    @Test
    void findByStatus_ShouldReturnApplicationsWithMatchingStatus() {
        // Arrange
        Application app1 = createApplication("10115", "Kompaktklasse", Status.NEW);
        Application app2 = createApplication("10117", "Kompaktklasse", Status.NEW);
        Application app3 = createApplication("20095", "Van", Status.ACCEPTED);
        Application app4 = createApplication("30159", "Elektroauto", Status.REJECTED);
        
        applicationRepository.saveAll(List.of(app1, app2, app3, app4));
        
        // Act
        List<Application> newApplications = applicationRepository.findByStatus(Status.NEW);
        List<Application> acceptedApplications = applicationRepository.findByStatus(Status.ACCEPTED);
        List<Application> rejectedApplications = applicationRepository.findByStatus(Status.REJECTED);
        
        // Assert
        assertEquals(2, newApplications.size());
        assertEquals(1, acceptedApplications.size());
        assertEquals(1, rejectedApplications.size());
        
        assertTrue(newApplications.stream().map(Application::getPostalCode).anyMatch(pc -> pc.equals("10115")));
        assertTrue(newApplications.stream().map(Application::getPostalCode).anyMatch(pc -> pc.equals("10117")));
        assertEquals("20095", acceptedApplications.get(0).getPostalCode());
        assertEquals("30159", rejectedApplications.get(0).getPostalCode());
    }
    
    @Test
    void findByStatus_WithPagination_ShouldReturnPagedResults() {
        // Arrange
        // Create 25 applications with NEW status
        for (int i = 0; i < 25; i++) {
            Application app = createApplication(
                String.format("%05d", 10000 + i), 
                "Kompaktklasse", 
                Status.NEW
            );
            applicationRepository.save(app);
        }
        
        // Act - Get first page (10 items)
        Pageable firstPageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Application> firstPage = applicationRepository.findByStatus(Status.NEW, firstPageable);
        
        // Act - Get second page (10 items)
        Pageable secondPageable = PageRequest.of(1, 10, Sort.by("createdAt").descending());
        Page<Application> secondPage = applicationRepository.findByStatus(Status.NEW, secondPageable);
        
        // Act - Get third page (5 items)
        Pageable thirdPageable = PageRequest.of(2, 10, Sort.by("createdAt").descending());
        Page<Application> thirdPage = applicationRepository.findByStatus(Status.NEW, thirdPageable);
        
        // Assert
        assertEquals(10, firstPage.getContent().size());
        assertEquals(10, secondPage.getContent().size());
        assertEquals(5, thirdPage.getContent().size());
        
        assertEquals(25, firstPage.getTotalElements());
        assertEquals(3, firstPage.getTotalPages());
        
        assertEquals(0, firstPage.getNumber());
        assertEquals(1, secondPage.getNumber());
        assertEquals(2, thirdPage.getNumber());
    }
    
    @Test
    void findByPostalCode_ShouldReturnApplicationsWithMatchingPostalCode() {
        // Arrange
        Application app1 = createApplication("10115", "Kompaktklasse", Status.NEW);
        Application app2 = createApplication("10115", "Van", Status.ACCEPTED);
        Application app3 = createApplication("20095", "Kompaktklasse", Status.NEW);
        
        applicationRepository.saveAll(List.of(app1, app2, app3));
        
        // Act
        List<Application> foundApplications = applicationRepository.findByPostalCode("10115");
        
        // Assert
        assertEquals(2, foundApplications.size());
        
        // Check that both vehicle types are present
        boolean hasKompaktklasse = false;
        boolean hasVan = false;
        for (Application app : foundApplications) {
            if ("Kompaktklasse".equals(app.getVehicleType())) {
                hasKompaktklasse = true;
            } else if ("Van".equals(app.getVehicleType())) {
                hasVan = true;
            }
        }
        assertTrue(hasKompaktklasse);
        assertTrue(hasVan);
    }
    
    @Test
    void findByVehicleType_ShouldReturnApplicationsWithMatchingVehicleType() {
        // Arrange
        Application app1 = createApplication("10115", "Kompaktklasse", Status.NEW);
        Application app2 = createApplication("20095", "Kompaktklasse", Status.ACCEPTED);
        Application app3 = createApplication("30159", "Van", Status.REJECTED);
        
        applicationRepository.saveAll(List.of(app1, app2, app3));
        
        // Act
        List<Application> foundApplications = applicationRepository.findByVehicleType("Kompaktklasse");
        
        // Assert
        assertEquals(2, foundApplications.size());
        
        // Check that both postal codes are present
        boolean has10115 = false;
        boolean has20095 = false;
        for (Application app : foundApplications) {
            if ("10115".equals(app.getPostalCode())) {
                has10115 = true;
            } else if ("20095".equals(app.getPostalCode())) {
                has20095 = true;
            }
        }
        assertTrue(has10115);
        assertTrue(has20095);
    }
    
    @Test
    void findByCreatedAtAfter_ShouldReturnApplicationsCreatedAfterGivenDate() {
        // Arrange
        LocalDateTime yesterday = now.minusDays(1);
        LocalDateTime twoDaysAgo = now.minusDays(2);
        LocalDateTime threeDaysAgo = now.minusDays(3);
        
        Application app1 = createApplicationWithDate("10115", "Kompaktklasse", Status.NEW, now);
        Application app2 = createApplicationWithDate("20095", "Sportwage", Status.ACCEPTED, yesterday);
        Application app3 = createApplicationWithDate("30159", "Van", Status.REJECTED, twoDaysAgo);
        Application app4 = createApplicationWithDate("40210", "Kompaktklasse", Status.NEW, threeDaysAgo);
        
        applicationRepository.saveAll(List.of(app1, app2, app3, app4));
        
        // Act
        List<Application> recentApplications = applicationRepository.findByCreatedAtAfter(yesterday.minusHours(1));
        
        // Assert
        assertEquals(2, recentApplications.size());
        
        // Check that both recent postal codes are present
        boolean has10115 = false;
        boolean has20095 = false;
        for (Application app : recentApplications) {
            if ("10115".equals(app.getPostalCode())) {
                has10115 = true;
            } else if ("20095".equals(app.getPostalCode())) {
                has20095 = true;
            }
        }
        assertTrue(has10115);
        assertTrue(has20095);
    }
    
    @Test
    void save_ShouldPersistApplication() {
        // Arrange
        Application application = createApplication("10115", "Kompaktklasse", Status.NEW);
        
        // Act
        Application savedApplication = applicationRepository.save(application);
        
        // Assert
        assertNotNull(savedApplication.getId());
        
        // Verify it can be retrieved
        Application retrievedApplication = applicationRepository.findById(savedApplication.getId()).orElse(null);
        assertNotNull(retrievedApplication);
        assertEquals("10115", retrievedApplication.getPostalCode());
        assertEquals("Kompaktklasse", retrievedApplication.getVehicleType());
        assertEquals(Status.NEW, retrievedApplication.getStatus());
    }
    
    @Test
    void delete_ShouldRemoveApplication() {
        // Arrange
        Application application = createApplication("10115", "Kompaktklasse", Status.NEW);
        Application savedApplication = applicationRepository.save(application);
        
        // Act
        applicationRepository.delete(savedApplication);
        
        // Assert
        assertTrue(applicationRepository.findById(savedApplication.getId()).isEmpty());
    }
    
    // Helper method to create test applications
    private Application createApplication(String postalCode, String vehicleType, Status status) {
        return createApplicationWithDate(postalCode, vehicleType, status, now);
    }
    
    private Application createApplicationWithDate(String postalCode, String vehicleType, Status status, LocalDateTime createdAt) {
        return new Application(
            10000, // annualMileage
            vehicleType,
            postalCode,
            new BigDecimal("500.00"), // basePremium
            new BigDecimal("1.0"), // mileageFactor
            new BigDecimal("1.0"), // vehicleFactor
            new BigDecimal("1.0"), // regionFactor
            new BigDecimal("500.00"), // calculatedPremium
            createdAt,
            status,
            testUser // Add the createdBy parameter
        );
    }
}
