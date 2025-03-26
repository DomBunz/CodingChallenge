package com.insurance.premium.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ApplicationRequest(
    @NotBlank(message = "Postal code is required")
    String postalCode,
    
    @NotBlank(message = "Vehicle type is required")
    String vehicleType,
    
    @NotNull(message = "Annual mileage is required")
    @Min(value = 0, message = "Annual mileage must be a positive number")
    Integer annualMileage
) {
}
