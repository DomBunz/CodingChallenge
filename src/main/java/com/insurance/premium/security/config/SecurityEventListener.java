package com.insurance.premium.security.config;

import com.insurance.premium.security.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Listener for Spring Security events to log authentication and authorization events.
 */
@Component
public class SecurityEventListener {

    private static final Logger logger = LoggerFactory.getLogger(SecurityEventListener.class);
    private final AuditLogService auditLogService;

    public SecurityEventListener(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Handles successful authentication events.
     *
     * @param event the authentication success event
     */
    @EventListener
    public void handleAuthenticationSuccessEvent(AuthenticationSuccessEvent event) {
        logSecurityEvent("Authentication Success", event.getAuthentication(), 200);
    }

    /**
     * Handles authentication failure due to bad credentials.
     *
     * @param event the authentication failure event
     */
    @EventListener
    public void handleAuthenticationFailureBadCredentialsEvent(AuthenticationFailureBadCredentialsEvent event) {
        logSecurityEvent("Authentication Failure - Bad Credentials", event.getAuthentication(), 401);
    }

    /**
     * Handles authentication failure due to user being disabled.
     *
     * @param event the authentication failure event
     */
    @EventListener
    public void handleAuthenticationFailureDisabledEvent(AuthenticationFailureDisabledEvent event) {
        logSecurityEvent("Authentication Failure - Account Disabled", event.getAuthentication(), 403);
    }

    /**
     * Handles authentication failure due to user being locked.
     *
     * @param event the authentication failure event
     */
    @EventListener
    public void handleAuthenticationFailureLockedEvent(AuthenticationFailureLockedEvent event) {
        logSecurityEvent("Authentication Failure - Account Locked", event.getAuthentication(), 403);
    }

    /**
     * Handles authentication failure due to account being expired.
     *
     * @param event the authentication failure event
     */
    @EventListener
    public void handleAuthenticationFailureExpiredEvent(AuthenticationFailureExpiredEvent event) {
        logSecurityEvent("Authentication Failure - Account Expired", event.getAuthentication(), 403);
    }

    /**
     * Handles authentication failure due to credentials being expired.
     *
     * @param event the authentication failure event
     */
    @EventListener
    public void handleAuthenticationFailureCredentialsExpiredEvent(AuthenticationFailureCredentialsExpiredEvent event) {
        logSecurityEvent("Authentication Failure - Credentials Expired", event.getAuthentication(), 403);
    }

    /**
     * Logs security events to the audit log.
     *
     * @param eventType the type of security event
     * @param authentication the authentication object
     * @param statusCode the HTTP status code
     */
    private void logSecurityEvent(String eventType, Authentication authentication, int statusCode) {
        try {
            // Default request details
            String ipAddress = "unknown";
            String userAgent = "unknown";
            String requestUri = "/";
            
            // Extract IP if available from authentication details
            if (authentication != null && authentication.getDetails() instanceof WebAuthenticationDetails details) {
                ipAddress = details.getRemoteAddress();
            }
            
            // Try to get current request details
            Map<String, String> requestDetails = getRequestDetails();
            if (!requestDetails.isEmpty()) {
                ipAddress = requestDetails.getOrDefault("ipAddress", ipAddress);
                userAgent = requestDetails.getOrDefault("userAgent", userAgent);
                requestUri = requestDetails.getOrDefault("requestUri", requestUri);
            }
            
            // Create audit log entry
            auditLogService.createAuditLog(
                "SecurityFilter",
                "SECURITY",
                requestUri,
                "{\"event\": \"" + eventType + "\"}",
                statusCode,
                ipAddress,
                userAgent,
                0L
            );
        } catch (Exception e) {
            logger.error("Failed to log security event", e);
        }
    }
    
    /**
     * Gets details from the current request context.
     * 
     * @return a map containing request details (ipAddress, userAgent, requestUri)
     */
    private Map<String, String> getRequestDetails() {
        Map<String, String> details = new HashMap<>();
        
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                details.put("ipAddress", request.getRemoteAddr());
                details.put("userAgent", request.getHeader("User-Agent"));
                details.put("requestUri", request.getRequestURI());
            }
        } catch (Exception e) {
            logger.debug("Could not get request details", e);
        }
        
        return details;
    }
}
