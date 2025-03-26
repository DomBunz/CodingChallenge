package com.insurance.premium.security.service;

import com.insurance.premium.security.domain.AuditLog;
import com.insurance.premium.security.repository.AuditLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for audit logging operations.
 */
@Service
public class AuditLogService {
    
    private final AuditLogRepository auditLogRepository;
    
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }
    
    /**
     * Create a new audit log entry.
     * 
     * @param controller the controller name
     * @param method the HTTP method
     * @param endpoint the endpoint path
     * @param requestData the request data (JSON)
     * @param responseStatus the HTTP response status code
     * @param ipAddress the client IP address
     * @param userAgent the client user agent
     * @param executionTimeMs the execution time in milliseconds
     * @return the created audit log
     */
    @Transactional
    @SuppressWarnings("java:S107")
    public AuditLog createAuditLog(String controller, String method, String endpoint, 
                                  String requestData, Integer responseStatus, 
                                  String ipAddress, String userAgent, Long executionTimeMs) {
        // Get the current authenticated user
        String username = "anonymous";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            username = authentication.getName();
        }
        
        AuditLog auditLog = new AuditLog(
            username, controller, method, endpoint, requestData, 
            responseStatus, ipAddress, userAgent, executionTimeMs
        );
        
        return auditLogRepository.save(auditLog);
    }
    
    /**
     * Get all audit logs.
     * 
     * @return list of all audit logs
     */
    @Transactional(readOnly = true)
    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }
    
    /**
     * Find audit logs by username.
     * 
     * @param username the username to search for
     * @return list of audit logs for the specified user
     */
    @Transactional(readOnly = true)
    public List<AuditLog> findByUsername(String username) {
        return auditLogRepository.findByUsername(username);
    }
    
    /**
     * Find audit logs by controller name.
     * 
     * @param controller the controller name to search for
     * @return list of audit logs for the specified controller
     */
    @Transactional(readOnly = true)
    public List<AuditLog> findByController(String controller) {
        return auditLogRepository.findByController(controller);
    }
    
    /**
     * Find audit logs by endpoint.
     * 
     * @param endpoint the endpoint to search for
     * @return list of audit logs for the specified endpoint
     */
    @Transactional(readOnly = true)
    public List<AuditLog> findByEndpoint(String endpoint) {
        return auditLogRepository.findByEndpoint(endpoint);
    }
    
    /**
     * Find audit logs created within a time range.
     * 
     * @param startTime the start time
     * @param endTime the end time
     * @return list of audit logs created within the specified time range
     */
    @Transactional(readOnly = true)
    public List<AuditLog> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.findByTimestampBetween(startTime, endTime);
    }
}
