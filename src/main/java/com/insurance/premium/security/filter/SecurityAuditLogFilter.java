package com.insurance.premium.security.filter;

import com.insurance.premium.security.service.AuditLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to log all HTTP requests, including those rejected by Spring Security.
 * This filter is ordered to run after Spring Security but before controller execution.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class SecurityAuditLogFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityAuditLogFilter.class);
    private final AuditLogService auditLogService;

    public SecurityAuditLogFilter(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, 
                                   @NonNull HttpServletResponse response, 
                                   @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        // Record the start time
        long startTime = System.currentTimeMillis();
        
        // Get the request URI
        String requestUri = request.getRequestURI();
        
        // Skip logging for certain paths (like static resources)
        if (shouldSkipLogging(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Get the HTTP method
        String httpMethod = request.getMethod();
        
        // Get client information
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        
        try {
            // Continue with the filter chain
            filterChain.doFilter(request, response);
        } finally {
            // Only log if the response status indicates an error (4xx or 5xx)
            int status = response.getStatus();
            if (status >= 400) {
                long executionTime = System.currentTimeMillis() - startTime;
                
                // Create audit log for failed requests
                try {
                    auditLogService.createAuditLog(
                        "SecurityFilter",
                        httpMethod,
                        requestUri,
                        "{}",  // We don't have access to the request body at this point
                        status,
                        ipAddress,
                        userAgent,
                        executionTime
                    );
                } catch (Exception e) {
                    LOG.error("Failed to create audit log for failed request", e);
                }
            }
        }
    }
    
    /**
     * Determines if logging should be skipped for the given URI.
     * 
     * @param uri the request URI
     * @return true if logging should be skipped, false otherwise
     */
    private boolean shouldSkipLogging(String uri) {
        // Skip logging for static resources
        return uri.startsWith("/static/") || 
               uri.startsWith("/css/") || 
               uri.startsWith("/js/") || 
               uri.startsWith("/images/") || 
               uri.startsWith("/favicon.ico");
    }
}