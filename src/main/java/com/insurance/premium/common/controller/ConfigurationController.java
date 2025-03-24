package com.insurance.premium.common.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.premium.common.domain.SystemConfiguration;
import com.insurance.premium.common.dto.ConfigurationRequest;
import com.insurance.premium.common.dto.ConfigurationResponse;
import com.insurance.premium.common.service.ConfigurationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/configurations")
@Tag(name = "System Administration", description = "API for managing system configurations (admin only)")
public class ConfigurationController {

    private final ConfigurationService configurationService;

    public ConfigurationController(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping
    @Operation(summary = "Get all configurations", description = "Returns a list of all system configurations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of configurations retrieved successfully", 
                content = @Content(schema = @Schema(implementation = ConfigurationResponse.class)))
    })
    public ResponseEntity<List<ConfigurationResponse>> getAllConfigurations() {
        List<ConfigurationResponse> configurations = configurationService.getAllConfigurations().stream()
                .map(this::mapToResponse)
                .toList();
        return ResponseEntity.ok(configurations);
    }

    @GetMapping("/{key}")
    @Operation(summary = "Get configuration by key", description = "Returns a specific configuration by its key")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully", 
                content = @Content(schema = @Schema(implementation = ConfigurationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid configuration key"),
        @ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    public ResponseEntity<ConfigurationResponse> getConfigurationByKey(
            @Parameter(description = "Configuration key", required = true) @PathVariable String key) {
        if (!configurationService.isKeyAllowed(key)) {
            return ResponseEntity.badRequest().build();
        }
        
        return configurationService.getConfigurationByKey(key)
                .map(config -> ResponseEntity.ok(mapToResponse(config)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create configuration", description = "Creates a new system configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Configuration created successfully", 
                content = @Content(schema = @Schema(implementation = ConfigurationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid configuration data or key not allowed")
    })
    public ResponseEntity<ConfigurationResponse> createConfiguration(
            @Parameter(description = "Configuration data", required = true) 
            @Valid @RequestBody ConfigurationRequest request) {
        try {
            SystemConfiguration config = configurationService.updateConfiguration(
                    request.key(), request.value(), request.description());
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(config));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{key}")
    @Operation(summary = "Update configuration", description = "Updates an existing system configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration updated successfully", 
                content = @Content(schema = @Schema(implementation = ConfigurationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid configuration data, key mismatch, or key not allowed")
    })
    public ResponseEntity<ConfigurationResponse> updateConfiguration(
            @Parameter(description = "Configuration key", required = true) @PathVariable String key, 
            @Parameter(description = "Updated configuration data", required = true) 
            @Valid @RequestBody ConfigurationRequest request) {
        
        // Ensure the key in the path matches the key in the request
        if (!key.equals(request.key())) {
            return ResponseEntity.badRequest().build();
        }
        
        // Check if the key is allowed
        if (!configurationService.isKeyAllowed(key)) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            SystemConfiguration config = configurationService.updateConfiguration(
                    request.key(), request.value(), request.description());
            return ResponseEntity.ok(mapToResponse(config));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private ConfigurationResponse mapToResponse(SystemConfiguration config) {
        return new ConfigurationResponse(
                config.getId(),
                config.getKey(),
                config.getValue(),
                config.getDescription(),
                config.getLastModified()
        );
    }
}
