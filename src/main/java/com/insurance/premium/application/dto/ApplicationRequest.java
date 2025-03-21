package com.insurance.premium.application.dto;

public record ApplicationRequest(
    String postalCode,
    String vehicleType,
    Integer annualMileage
) {
    /**
     * Returns a validation error message if the request is invalid
     * 
     * @return error message or null if valid
     */
    public String getValidationError() {
        String s = "";
        if (postalCode == null || postalCode.isBlank()) {
            s += "Postal code is required. ";
        }
        if (vehicleType == null || vehicleType.isBlank()) {
            s += "Vehicle type is required. ";
        }
        if (annualMileage == null || annualMileage < 0) {
            s += "Annual mileage must be a positive number.";
        }
        return s.isBlank() ? null : s.trim();
    }
}
