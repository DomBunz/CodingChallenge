package com.insurance.premium.application.domain;

import com.insurance.premium.common.domain.BaseEntity;
import com.insurance.premium.security.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@SuppressWarnings("java:S2160") // equals and hashCode are in BaseEntity
public class Application extends BaseEntity {

    public Application() {} // default constructor for JPA
    
    @SuppressWarnings("java:S107") // number of parameters
    public Application(Integer annualMileage, String vehicleType, String postalCode, BigDecimal basePremium,
            BigDecimal mileageFactor, BigDecimal vehicleFactor, BigDecimal regionFactor, BigDecimal calculatedPremium,
            LocalDateTime createdAt, Status status, User createdBy) {
        this.annualMileage = annualMileage;
        this.vehicleType = vehicleType;
        this.postalCode = postalCode;
        this.basePremium = basePremium;
        this.mileageFactor = mileageFactor;
        this.vehicleFactor = vehicleFactor;
        this.regionFactor = regionFactor;
        this.calculatedPremium = calculatedPremium;
        this.createdAt = createdAt;
        this.status = status;
        this.createdBy = createdBy;
    }

    public enum Status {
        NEW,
        ACCEPTED,
        REJECTED
    }

    @NotNull
    @Column(name = "annual_mileage", nullable = false)
    private Integer annualMileage;

    @NotNull
    @Column(name = "vehicle_type", nullable = false)
    private String vehicleType;

    @NotNull
    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @NotNull
    @Column(name = "base_premium", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePremium;

    @NotNull
    @Column(name = "mileage_factor", nullable = false, precision = 10, scale = 2)
    private BigDecimal mileageFactor;

    @NotNull
    @Column(name = "vehicle_factor", nullable = false, precision = 10, scale = 2)
    private BigDecimal vehicleFactor;

    @NotNull
    @Column(name = "region_factor", nullable = false, precision = 10, scale = 2)
    private BigDecimal regionFactor;

    @NotNull
    @Column(name = "calculated_premium", nullable = false, precision = 10, scale = 2)
    private BigDecimal calculatedPremium;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    public Integer getAnnualMileage() {
        return annualMileage;
    }

    public void setAnnualMileage(Integer annualMileage) {
        this.annualMileage = annualMileage;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public BigDecimal getBasePremium() {
        return basePremium;
    }

    public void setBasePremium(BigDecimal basePremium) {
        this.basePremium = basePremium;
    }

    public BigDecimal getMileageFactor() {
        return mileageFactor;
    }

    public void setMileageFactor(BigDecimal mileageFactor) {
        this.mileageFactor = mileageFactor;
    }

    public BigDecimal getVehicleFactor() {
        return vehicleFactor;
    }

    public void setVehicleFactor(BigDecimal vehicleFactor) {
        this.vehicleFactor = vehicleFactor;
    }

    public BigDecimal getRegionFactor() {
        return regionFactor;
    }

    public void setRegionFactor(BigDecimal regionFactor) {
        this.regionFactor = regionFactor;
    }

    public BigDecimal getCalculatedPremium() {
        return calculatedPremium;
    }

    public void setCalculatedPremium(BigDecimal calculatedPremium) {
        this.calculatedPremium = calculatedPremium;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return "Application{" +
               "id:" + getId() + "," +
               "annualMileage:" + annualMileage + "," +
               "vehicleType:'" + vehicleType + "'," +
               "postalCode:'" + postalCode + "'," +
               "basePremium:" + basePremium + "," +
               "mileageFactor:" + mileageFactor + "," +
               "vehicleFactor:" + vehicleFactor + "," +
               "regionFactor:" + regionFactor + "," +
               "calculatedPremium:" + calculatedPremium + "," +
               "createdAt:" + createdAt + "," +
               "status:" + status + "," +
               "createdBy:" + createdBy +
               '}';
    }
}
