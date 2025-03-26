package com.insurance.premium.security.controller;

import com.insurance.premium.security.domain.AuditLog;
import com.insurance.premium.security.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for managing audit logs.
 */
@RestController
@RequestMapping("/api/admin/audit-logs")
@Tag(name = "Audit Logs", description = "Operations for managing audit logs")
@SecurityRequirement(name = "basicAuth")
public class AuditLogController {
    
    private final AuditLogService auditLogService;
    
    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }
    
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Get all audit logs",
        description = "Returns a list of all audit logs in the system",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of audit logs retrieved successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuditLog.class))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires admin role",
                content = @Content
            )
        }
    )
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        List<AuditLog> auditLogs = auditLogService.findAll();
        return ResponseEntity.ok(auditLogs);
    }
    
    @GetMapping("/user/{username}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Get audit logs by username",
        description = "Returns a list of audit logs for a specific user",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of audit logs retrieved successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuditLog.class))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires admin role",
                content = @Content
            )
        }
    )
    public ResponseEntity<List<AuditLog>> getAuditLogsByUsername(
            @Parameter(description = "Username", required = true)
            @PathVariable String username) {
        List<AuditLog> auditLogs = auditLogService.findByUsername(username);
        return ResponseEntity.ok(auditLogs);
    }
    
    @GetMapping("/controller/{controller}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Get audit logs by controller",
        description = "Returns a list of audit logs for a specific controller",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of audit logs retrieved successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuditLog.class))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires admin role",
                content = @Content
            )
        }
    )
    public ResponseEntity<List<AuditLog>> getAuditLogsByController(
            @Parameter(description = "Controller name", required = true)
            @PathVariable String controller) {
        List<AuditLog> auditLogs = auditLogService.findByController(controller);
        return ResponseEntity.ok(auditLogs);
    }
    
    @GetMapping("/endpoint/{endpoint}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Get audit logs by endpoint",
        description = "Returns a list of audit logs for a specific endpoint",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of audit logs retrieved successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuditLog.class))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires admin role",
                content = @Content
            )
        }
    )
    public ResponseEntity<List<AuditLog>> getAuditLogsByEndpoint(
            @Parameter(description = "Endpoint path", required = true)
            @PathVariable String endpoint) {
        List<AuditLog> auditLogs = auditLogService.findByEndpoint(endpoint);
        return ResponseEntity.ok(auditLogs);
    }
    
    @GetMapping("/time-range")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Get audit logs by time range",
        description = "Returns a list of audit logs created within a specific time range",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "List of audit logs retrieved successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuditLog.class))
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - requires admin role",
                content = @Content
            )
        }
    )
    public ResponseEntity<List<AuditLog>> getAuditLogsByTimeRange(
            @Parameter(description = "Start time (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "End time (ISO format)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<AuditLog> auditLogs = auditLogService.findByTimeRange(startTime, endTime);
        return ResponseEntity.ok(auditLogs);
    }
}
