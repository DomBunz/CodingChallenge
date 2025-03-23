package com.insurance.premium.calculation.dto;

import java.math.BigDecimal;

import com.insurance.premium.calculation.domain.MileageFactor;
import com.insurance.premium.calculation.domain.RegionFactor;
import com.insurance.premium.calculation.domain.VehicleType;

/**
 * Data Transfer Object for factor responses
 */
public record FactorResponse(
    String name,
    BigDecimal factor,
    String description
) {
    public static FactorResponse fromRegionFactor(RegionFactor regionFactor) {
        return new FactorResponse(
            regionFactor.getFederalState(),
            regionFactor.getFactor(),
            "Region factor for " + regionFactor.getFederalState()
        );
    }
    
    public static FactorResponse fromVehicleType(VehicleType vehicleType) {
        return new FactorResponse(
            vehicleType.getName(),
            vehicleType.getFactor(),
            "Vehicle type factor for " + vehicleType.getName()
        );
    }
    
    public static FactorResponse fromMileageFactor(MileageFactor mileageFactor) {
        String range;
        if (mileageFactor.getMaxMileage() == null) {
            range = mileageFactor.getMinMileage() + "+ km";
        } else {
            range = mileageFactor.getMinMileage() + "-" + mileageFactor.getMaxMileage() + " km";
        }
        
        return new FactorResponse(
            range,
            mileageFactor.getFactor(),
            "Mileage factor for " + range + " per year"
        );
    }
}
