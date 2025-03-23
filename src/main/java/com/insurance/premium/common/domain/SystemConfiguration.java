package com.insurance.premium.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_configurations")
@SuppressWarnings("java:S2160") // equals and hashCode are in BaseEntity
public class SystemConfiguration extends BaseEntity {

    @NotBlank
    @Column(name = "config_key", nullable = false, unique = true)
    private String key;

    @NotBlank
    @Column(name = "config_value", nullable = false)
    private String value;

    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "last_modified", nullable = false)
    private LocalDateTime lastModified;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return "SystemConfiguration{" +
               "id:" + getId() + "," +
               "key:'" + key + "'," +
               "value:'" + value + "'," +
               "description:'" + description + "'," +
               "lastModified:" + lastModified +
               '}';
    }
}
