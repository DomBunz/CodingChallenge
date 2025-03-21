package com.insurance.premium.calculation.domain;

import com.insurance.premium.common.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "region_factors")
public class RegionFactor extends BaseEntity {

    @NotBlank
    @Column(name = "federal_state", nullable = false, unique = true)
    private String federalState;

    @NotNull
    @Column(name = "factor", nullable = false)
    private BigDecimal factor;

    public String getFederalState() {
        return federalState;
    }

    public void setFederalState(String federalState) {
        this.federalState = federalState;
    }

    public BigDecimal getFactor() {
        return factor;
    }

    public void setFactor(BigDecimal factor) {
        this.factor = factor;
    }

    @Override
    public String toString() {
        return "RegionFactor{" +
               "id:" + getId() + "," +
               "federalState:'" + federalState + "'," +
               "factor:" + factor +
               '}';
    }
}
