package com.insurance.premium.calculation.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Base class for factor requests with common validation
 */
public abstract class FactorRequest {
    
    @NotNull(message = "Factor cannot be null")
    @DecimalMin(value = "0.01", message = "Factor must be greater than 0")
    private BigDecimal factor;
    
    public BigDecimal getFactor() {
        return factor;
    }
    
    public void setFactor(BigDecimal factor) {
        this.factor = factor;
    }
}
