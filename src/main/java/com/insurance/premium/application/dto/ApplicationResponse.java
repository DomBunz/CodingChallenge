package com.insurance.premium.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.insurance.premium.application.domain.Application;
import com.insurance.premium.application.domain.Application.Status;

public record ApplicationResponse(
    Long id,
    String postalCode,
    String vehicleType,
    Integer annualMileage,
    BigDecimal basePremium,
    BigDecimal mileageFactor,
    BigDecimal vehicleFactor,
    BigDecimal regionFactor,
    BigDecimal calculatedPremium,
    LocalDateTime createdAt,
    Status status
) {
    public static ApplicationResponse fromEntity(Application application) {
        return new ApplicationResponse(
            application.getId(),
            application.getPostalCode(),
            application.getVehicleType(),
            application.getAnnualMileage(),
            application.getBasePremium(),
            application.getMileageFactor(),
            application.getVehicleFactor(),
            application.getRegionFactor(),
            application.getCalculatedPremium(),
            application.getCreatedAt(),
            application.getStatus()
        );
    }
}
