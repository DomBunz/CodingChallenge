package com.insurance.premium.calculation.dto;

import java.util.Objects;

/**
 * Data Transfer Object for premium calculation requests
 */
public record PremiumCalculationRequest(
    String postalCode,
    String vehicleType,
    int annualMileage
) {
    /**
     * Returns a validation error message if the request is invalid
     * 
     * @return error message or null if valid
     */
    public String getValidationError() {
        String s = "";
        if (postalCode == null || postalCode.isBlank()) {
            s = "Postal code is required. ";
        }
        if (vehicleType == null || vehicleType.isBlank()) {
            s += "Vehicle type is required. ";
        }
        if (annualMileage <= 0) {
            s += "Annual mileage must be a positive number.";
        }
        return s.isBlank() ? null : s.trim();
    }
}
