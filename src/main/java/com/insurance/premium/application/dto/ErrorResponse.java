package com.insurance.premium.application.dto;

/**
 * Standard error response for API errors
 */
public record ErrorResponse(
    String message,
    String code
) {
    public static ErrorResponse of(String message) {
        return new ErrorResponse(message, "ERROR");
    }
    
    public static ErrorResponse validation(String message) {
        return new ErrorResponse(message, "VALIDATION_ERROR");
    }
    
    public static ErrorResponse serverError(String message) {
        return new ErrorResponse(message, "SERVER_ERROR");
    }
}
