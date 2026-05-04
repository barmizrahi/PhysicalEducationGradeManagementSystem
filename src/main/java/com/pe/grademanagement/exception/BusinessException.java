package com.pe.grademanagement.exception;

/**
 * Base exception for business logic errors.
 * 
 * Used when business rules are violated (e.g., duplicate student detection conflicts,
 * test assignment to non-existent classes, invalid grade calculations).
 * 
 * Requirements:
 * - Error Handling: Handle business logic errors with descriptive messages
 */
public class BusinessException extends RuntimeException {
    
    private final String errorCode;
    
    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
    }
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
    }
    
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
