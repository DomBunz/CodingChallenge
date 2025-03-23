package com.insurance.premium.application.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import com.insurance.premium.application.domain.Application;
import com.insurance.premium.application.domain.Application.Status;

@DataJpaTest
@ActiveProfiles("test")
class ApplicationRepositoryTest {

    @Autowired
    private ApplicationRepository applicationRepository;
    
    private LocalDateTime now;
    
    @BeforeEach
    void setUp() {
        // Clear the repository before each test
        applicationRepository.deleteAll();
        now = LocalDateTime.now();
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
        assertThat(newApplications).hasSize(2);
        assertThat(acceptedApplications).hasSize(1);
        assertThat(rejectedApplications).hasSize(1);
        
        assertThat(newApplications).extracting(Application::getPostalCode)
            .containsExactlyInAnyOrder("10115", "10117");
        assertThat(acceptedApplications).extracting(Application::getPostalCode)
            .containsExactly("20095");
        assertThat(rejectedApplications).extracting(Application::getPostalCode)
            .containsExactly("30159");
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
        assertThat(firstPage.getContent()).hasSize(10);
        assertThat(secondPage.getContent()).hasSize(10);
        assertThat(thirdPage.getContent()).hasSize(5);
        
        assertThat(firstPage.getTotalElements()).isEqualTo(25);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
        
        assertThat(firstPage.getNumber()).isEqualTo(0);
        assertThat(secondPage.getNumber()).isEqualTo(1);
        assertThat(thirdPage.getNumber()).isEqualTo(2);
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
        assertThat(foundApplications).hasSize(2);
        assertThat(foundApplications).extracting(Application::getVehicleType)
            .containsExactlyInAnyOrder("Kompaktklasse", "Van");
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
        assertThat(foundApplications).hasSize(2);
        assertThat(foundApplications).extracting(Application::getPostalCode)
            .containsExactlyInAnyOrder("10115", "20095");
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
        assertThat(recentApplications).hasSize(2);
        assertThat(recentApplications).extracting(Application::getPostalCode)
            .containsExactlyInAnyOrder("10115", "20095");
    }
    
    @Test
    void save_ShouldPersistApplication() {
        // Arrange
        Application application = createApplication("10115", "Kompaktklasse", Status.NEW);
        
        // Act
        Application savedApplication = applicationRepository.save(application);
        
        // Assert
        assertThat(savedApplication.getId()).isNotNull();
        
        // Verify it can be retrieved
        Application retrievedApplication = applicationRepository.findById(savedApplication.getId()).orElse(null);
        assertThat(retrievedApplication).isNotNull();
        assertThat(retrievedApplication.getPostalCode()).isEqualTo("10115");
        assertThat(retrievedApplication.getVehicleType()).isEqualTo("Kompaktklasse");
        assertThat(retrievedApplication.getStatus()).isEqualTo(Status.NEW);
    }
    
    @Test
    void delete_ShouldRemoveApplication() {
        // Arrange
        Application application = createApplication("10115", "Kompaktklasse", Status.NEW);
        Application savedApplication = applicationRepository.save(application);
        
        // Act
        applicationRepository.delete(savedApplication);
        
        // Assert
        assertThat(applicationRepository.findById(savedApplication.getId())).isEmpty();
    }
    
    // Helper method to create test applications
    private Application createApplication(String postalCode, String vehicleType, Status status) {
        return createApplicationWithDate(postalCode, vehicleType, status, now);
    }
    
    private Application createApplicationWithDate(String postalCode, String vehicleType, Status status, LocalDateTime createdAt) {
        return new Application(
            15000, // annualMileage
            vehicleType,
            postalCode,
            new BigDecimal("500.00"), // basePremium
            new BigDecimal("1.0"), // mileageFactor
            new BigDecimal("1.0"), // vehicleFactor
            new BigDecimal("1.0"), // regionFactor
            new BigDecimal("500.00"), // calculatedPremium
            createdAt,
            status
        );
    }
}
