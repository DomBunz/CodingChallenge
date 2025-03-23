package com.insurance.premium.common.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating or updating a system configuration
 */
public record ConfigurationRequest(
    @NotBlank String key,
    @NotBlank String value,
    String description
) {}
