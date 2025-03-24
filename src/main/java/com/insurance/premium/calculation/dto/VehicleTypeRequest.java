package com.insurance.premium.calculation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating or updating a vehicle type
 */
public class VehicleTypeRequest extends FactorRequest {
    
    @NotBlank(message = "Vehicle type name cannot be blank")
    private String name;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
