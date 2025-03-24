package com.insurance.premium.calculation.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating or updating a region factor
 */
public class RegionFactorRequest extends FactorRequest {
    
    @NotBlank(message = "Federal state cannot be blank")
    private String federalState;
    
    public String getFederalState() {
        return federalState;
    }
    
    public void setFederalState(String federalState) {
        this.federalState = federalState;
    }
}
