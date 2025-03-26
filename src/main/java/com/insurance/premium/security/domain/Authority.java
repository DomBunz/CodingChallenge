package com.insurance.premium.security.domain;

import com.insurance.premium.common.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Authority entity representing a specific permission in the system.
 */
@Entity
@Table(name = "authorities")
@SuppressWarnings("java:S2160") // equals and hashCode are in BaseEntity
public class Authority extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    public Authority() {} // Default constructor for JPA

    public Authority(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
