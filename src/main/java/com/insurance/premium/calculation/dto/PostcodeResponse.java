package com.insurance.premium.calculation.dto;

import com.insurance.premium.calculation.domain.Region;

/**
 * Data Transfer Object for postal code responses
 */
public record PostcodeResponse(
    String postalCode,
    String federalState,
    String country,
    String area,
    String city,
    String district
) {
    public static PostcodeResponse fromRegion(Region region) {
        return new PostcodeResponse(
            region.getPostalCode(),
            region.getFederalState(),
            region.getCountry(),
            region.getArea(),
            region.getCity(),
            region.getDistrict()
        );
    }
}
