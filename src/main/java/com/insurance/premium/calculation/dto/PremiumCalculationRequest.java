package com.insurance.premium.calculation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for premium calculation requests
 */
public record PremiumCalculationRequest(
    @NotBlank(message = "Postal code is required")
    String postalCode,
    
    @NotBlank(message = "Vehicle type is required")
    String vehicleType,
    
    @Min(value = 1, message = "Annual mileage must be a positive number")
    int annualMileage
) {
}
