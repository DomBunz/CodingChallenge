package com.insurance.premium.calculation.domain;

import com.insurance.premium.common.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "vehicle_types")
public class VehicleType extends BaseEntity {

    @NotBlank
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @NotNull
    @Column(name = "factor", nullable = false)
    private BigDecimal factor;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getFactor() {
        return factor;
    }

    public void setFactor(BigDecimal factor) {
        this.factor = factor;
    }

    @Override
    public String toString() {
        return "VehicleType{" +
               "id:" + getId() + "," +
               "name:'" + name + "'," +
               "factor:" + factor +
               '}';
    }
}
