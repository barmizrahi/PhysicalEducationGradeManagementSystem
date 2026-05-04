package com.pe.grademanagement.exception;

/**
 * Exception thrown when a user attempts to access a resource they don't have permission for.
 * 
 * Used for authorization failures (e.g., teacher trying to access another teacher's classes).
 * 
 * Requirements:
 * - 13.4: Prevent teachers from accessing data for classes not assigned to them
 * - Error Handling: Handle authorization errors with descriptive messages
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
