package com.insurance.premium.security.repository;

import com.insurance.premium.security.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for audit log operations.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * Find audit logs by username.
     * 
     * @param username the username to search for
     * @return list of audit logs for the specified user
     */
    List<AuditLog> findByUsername(String username);
    
    /**
     * Find audit logs by controller name.
     * 
     * @param controller the controller name to search for
     * @return list of audit logs for the specified controller
     */
    List<AuditLog> findByController(String controller);
    
    /**
     * Find audit logs by endpoint.
     * 
     * @param endpoint the endpoint to search for
     * @return list of audit logs for the specified endpoint
     */
    List<AuditLog> findByEndpoint(String endpoint);
    
    /**
     * Find audit logs created within a time range.
     * 
     * @param startTime the start time
     * @param endTime the end time
     * @return list of audit logs created within the specified time range
     */
    List<AuditLog> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);
}
