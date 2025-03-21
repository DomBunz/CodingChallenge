package com.insurance.premium.calculation.dto;

import java.math.BigDecimal;

/**
 * Data Transfer Object for premium calculation results
 */
public record PremiumCalculationResult(
    String postalCode,
    String vehicleType,
    int annualMileage,
    BigDecimal basePremium,
    BigDecimal mileageFactor,
    BigDecimal vehicleTypeFactor,
    BigDecimal regionFactor,
    BigDecimal premium
) {}
