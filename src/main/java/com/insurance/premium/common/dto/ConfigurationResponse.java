package com.insurance.premium.common.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for system configuration
 */
public record ConfigurationResponse(
    Long id,
    String key,
    String value,
    String description,
    LocalDateTime lastModified
) {}
