package com.insurance.premium.calculation.domain;

import com.insurance.premium.common.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "mileage_factors", 
    uniqueConstraints = @UniqueConstraint(
        name = "uk_mileage_factors_range", 
        columnNames = {"min_mileage", "max_mileage"}
    )
)
public class MileageFactor extends BaseEntity {

    @NotNull
    @Column(name = "min_mileage", nullable = false)
    private Integer minMileage;

    @Column(name = "max_mileage")
    private Integer maxMileage;

    @NotNull
    @Column(name = "factor", nullable = false)
    private BigDecimal factor;

    public Integer getMinMileage() {
        return minMileage;
    }

    public void setMinMileage(Integer minMileage) {
        this.minMileage = minMileage;
    }

    public Integer getMaxMileage() {
        return maxMileage;
    }

    public void setMaxMileage(Integer maxMileage) {
        this.maxMileage = maxMileage;
    }

    public BigDecimal getFactor() {
        return factor;
    }

    public void setFactor(BigDecimal factor) {
        this.factor = factor;
    }
    
    public boolean isInRange(Integer mileage) {
        return mileage >= minMileage && (maxMileage == null || mileage <= maxMileage);
    }
    
    @Override
    public String toString() {
        return "MileageFactor{" +
               "id:" + getId() + "," +
               "minMileage:" + minMileage + "," +
               "maxMileage:" + maxMileage + "," +
               "factor:" + factor +
               '}';
    }
}
