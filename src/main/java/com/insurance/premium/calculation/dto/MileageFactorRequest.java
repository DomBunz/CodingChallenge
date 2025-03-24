package com.insurance.premium.calculation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating or updating a mileage factor.
 * Overlaps in ranges or invalid ranges are not validated.
 */
public class MileageFactorRequest extends FactorRequest {
    
    @NotNull(message = "Minimum mileage cannot be null")
    @Min(value = 0, message = "Minimum mileage must be at least 0")
    private Integer minMileage;
    
    @Min(value = 1, message = "Maximum mileage must be at least 1")
    private Integer maxMileage; // Can be null for unlimited upper bound
    
    public Integer getMinMileage() {
        return minMileage;
    }
    
    public void setMinMileage(Integer minMileage) {
        this.minMileage = minMileage;
    }
    
    public Integer getMaxMileage() {
        return maxMileage;
    }
    
    public void setMaxMileage(Integer maxMileage) {
        this.maxMileage = maxMileage;
    }
}
