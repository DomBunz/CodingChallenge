package com.insurance.premium.security.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insurance.premium.security.domain.User;
import com.insurance.premium.security.dto.UserDto;
import com.insurance.premium.security.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "User Management", description = "Operations for managing users")
@SecurityRequirement(name = "basicAuth")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Get all users",
        description = "Returns a list of all users in the system",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of users retrieved successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires admin role",
                content = @Content
            )
        }
    )
    public ResponseEntity<List<UserDto>> getAllUsers() {
        logger.debug("REST request to get all Users");
        
        List<UserDto> users = userService.findAll().stream()
                .map(userService::toDto)
                .toList();
        
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Get user by ID",
        description = "Returns a user by ID",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User retrieved successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires admin role",
                content = @Content
            )
        }
    )
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id) {
        logger.debug("REST request to get User : {}", id);
        
        return userService.findById(id)
                .map(userService::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Create a new user",
        description = "Creates a new user with the provided details",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "User created successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input data",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires admin role",
                content = @Content
            )
        }
    )
    public ResponseEntity<UserDto> createUser(
            @Parameter(description = "User details", required = true)
            @Valid @RequestBody UserDto userDto) {
        logger.debug("REST request to create User : {}", userDto.username());
        
        try {
            // Default to ROLE_CUSTOMER if no role specified
            String roleName = userDto.roles().isEmpty() ? "ROLE_CUSTOMER" : userDto.roles().iterator().next();
            User user = userService.createUser(userDto, roleName);
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.toDto(user));
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Update a user",
        description = "Updates an existing user with the provided details",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "User updated successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input data",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires admin role",
                content = @Content
            )
        }
    )
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated user details", required = true)
            @Valid @RequestBody UserDto userDto) {
        logger.debug("REST request to update User : {}", id);
        
        try {
            User user = userService.updateUser(id, userDto);
            return ResponseEntity.ok(userService.toDto(user));
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update user: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Delete a user",
        description = "Deletes a user by ID",
        responses = {
            @ApiResponse(
                responseCode = "204",
                description = "User deleted successfully",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = @Content
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires admin role",
                content = @Content
            )
        }
    )
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable Long id) {
        logger.debug("REST request to delete User : {}", id);
        
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to delete user: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
