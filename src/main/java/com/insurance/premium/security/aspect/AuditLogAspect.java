package com.insurance.premium.security.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.premium.security.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspect for audit logging of controller methods.
 */
@Aspect
@Component
public class AuditLogAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditLogAspect.class);
    
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    
    public AuditLogAspect(AuditLogService auditLogService, ObjectMapper objectMapper) {
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Intercepts all controller method calls and logs them.
     * 
     * @param joinPoint the join point
     * @return the result of the method execution
     * @throws Throwable if an error occurs
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController) || " +
            "@within(org.springframework.stereotype.Controller)")
    public Object logControllerMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // Get HTTP request and response
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        
        // Get method information
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String controllerName = joinPoint.getTarget().getClass().getSimpleName();
        
        // Determine HTTP method
        String httpMethod = determineHttpMethod(method);
        
        // Get endpoint path
        String endpoint = determineEndpoint(method, request);
        
        // Extract request data
        String requestData = extractRequestData(joinPoint, method);
        
        // Execute the method
        Object result = null;
        Integer responseStatus = 200;
        try {
            result = joinPoint.proceed();
            
            // Try to extract response status if result is ResponseEntity
            if (result != null && result.getClass().getSimpleName().equals("ResponseEntity")) {
                try {
                    responseStatus = (Integer) result.getClass().getMethod("getStatusCodeValue").invoke(result);
                } catch (Exception e) {
                    // Fallback for newer Spring versions
                    try {
                        Object statusCode = result.getClass().getMethod("getStatusCode").invoke(result);
                        responseStatus = (Integer) statusCode.getClass().getMethod("value").invoke(statusCode);
                    } catch (Exception ex) {
                        logger.debug("Could not extract status code from ResponseEntity", ex);
                    }
                }
            }
        } catch (Exception e) {
            responseStatus = 500;
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Get client information
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            
            // Create audit log
            try {
                auditLogService.createAuditLog(
                    controllerName,
                    httpMethod,
                    endpoint,
                    requestData,
                    responseStatus,
                    ipAddress,
                    userAgent,
                    executionTime
                );
            } catch (Exception e) {
                logger.error("Failed to create audit log", e);
            }
        }
        
        return result;
    }
    
    /**
     * Determines the HTTP method from the method annotations.
     * 
     * @param method the method
     * @return the HTTP method
     */
    private String determineHttpMethod(Method method) {
        if (method.isAnnotationPresent(GetMapping.class)) {
            return "GET";
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            return "POST";
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            return "PUT";
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            return "DELETE";
        } else if (method.isAnnotationPresent(PatchMapping.class)) {
            return "PATCH";
        } else if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            if (requestMapping.method().length > 0) {
                return requestMapping.method()[0].name();
            }
        }
        return "UNKNOWN";
    }
    
    /**
     * Determines the endpoint path from the method annotations and request.
     * 
     * @param method the method
     * @param request the HTTP request
     * @return the endpoint path
     */
    private String determineEndpoint(Method method, HttpServletRequest request) {
        return request.getRequestURI();
    }
    
    /**
     * Extracts request data from the join point and method.
     * 
     * @param joinPoint the join point
     * @param method the method
     * @return the request data as JSON string
     */
    private String extractRequestData(ProceedingJoinPoint joinPoint, Method method) {
        try {
            Object[] args = joinPoint.getArgs();
            String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            
            Map<String, Object> requestData = new HashMap<>();
            
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                
                // Skip HttpServletRequest, HttpServletResponse, and similar objects
                if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) {
                    continue;
                }
                
                // Check if parameter has RequestBody annotation
                boolean isRequestBody = Arrays.stream(parameterAnnotations[i])
                    .anyMatch(annotation -> annotation.annotationType().equals(RequestBody.class));
                
                if (isRequestBody) {
                    // For RequestBody, use the whole object
                    return objectMapper.writeValueAsString(arg);
                } else {
                    // For other parameters, add them to the map
                    String paramName = parameterNames[i];
                    requestData.put(paramName, arg);
                }
            }
            
            // If we didn't find a RequestBody, return the map of parameters
            if (!requestData.isEmpty()) {
                return objectMapper.writeValueAsString(requestData);
            }
        } catch (Exception e) {
            logger.debug("Failed to extract request data", e);
        }
        
        return "{}";
    }
}
