package com.insurance.premium.calculation.domain;

import com.insurance.premium.common.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "regions", 
    uniqueConstraints = @UniqueConstraint(
        name = "uk_regions_multiple", 
        columnNames = {"federal_state", "country", "area", "city", "postal_code", "district"}
    )
)
public class Region extends BaseEntity {

    public Region(String federalState, String country, String area, String city, String postalCode, String district, RegionFactor regionFactor) {
        this.federalState = federalState;
        this.country = country;
        this.area = area;
        this.city = city;
        this.postalCode = postalCode;
        this.district = district;
        this.regionFactor = regionFactor;
    }

    @NotBlank
    @Column(name = "federal_state", nullable = false)
    private String federalState;

    @NotBlank
    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "area")
    private String area;

    @NotBlank
    @Column(name = "city", nullable = false)
    private String city;

    @NotBlank
    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "district")
    private String district;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "region_factor_id", nullable = false)
    private RegionFactor regionFactor;

    public String getFederalState() {
        return federalState;
    }

    public void setFederalState(String federalState) {
        this.federalState = federalState;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public RegionFactor getRegionFactor() {
        return regionFactor;
    }

    public void setRegionFactor(RegionFactor regionFactor) {
        this.regionFactor = regionFactor;
    }

    @Override
    public String toString() {
        return "Region{" +
               "id:" + getId() + "," +
               "federalState:'" + federalState + "'," +
               "country:'" + country + "'," +
               "area:'" + area + "'," +
               "city:'" + city + "'," +
               "postalCode:'" + postalCode + "'," +
               "district:'" + district + "'," +
               "regionFactor:" + (regionFactor != null ? regionFactor.getId() : null) +
               '}';
    }
}
