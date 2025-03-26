package com.insurance.premium.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

/**
 * Data Transfer Object for User operations.
 */
public record UserDto(
    Long id,
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username,
    
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password,
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email,
    
    String firstName,
    
    String lastName,
    
    boolean enabled,
    
    Set<String> roles
) {
    /**
     * Default constructor with empty roles set
     */
    public UserDto {
        if (roles == null) {
            roles = new HashSet<>();
        }
    }
}
