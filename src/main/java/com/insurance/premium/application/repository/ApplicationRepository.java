package com.insurance.premium.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.insurance.premium.application.domain.Application;
import com.insurance.premium.application.domain.Application.Status;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    /**
     * Find applications by status with pagination
     * 
     * @param status The application status
     * @param pageable Pagination information
     * @return Page of applications with the given status
     */
    Page<Application> findByStatus(Status status, Pageable pageable);
    
    /**
     * Find applications by status
     * 
     * @param status The application status
     * @return List of applications with the given status
     */
    List<Application> findByStatus(Status status);
    
    /**
     * Find applications by postal code
     * 
     * @param postalCode The postal code
     * @return List of applications with the given postal code
     */
    List<Application> findByPostalCode(String postalCode);
    
    /**
     * Find applications by vehicle type
     * 
     * @param vehicleType The vehicle type
     * @return List of applications with the given vehicle type
     */
    List<Application> findByVehicleType(String vehicleType);
    
    /**
     * Find applications created after a specific date
     * 
     * @param date The date threshold
     * @return List of applications created after the given date
     */
    List<Application> findByCreatedAtAfter(LocalDateTime date);
}
