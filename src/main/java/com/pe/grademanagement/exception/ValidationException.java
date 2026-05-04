package com.pe.grademanagement.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exception thrown when input validation fails.
 * 
 * Supports both simple validation errors and field-specific validation errors.
 * 
 * Requirements:
 * - 14.1, 14.2, 14.3, 14.4: Handle input validation errors
 * - Error Handling: Return descriptive validation error messages
 */
public class ValidationException extends RuntimeException {
    
    private final Map<String, String> fieldErrors;
    
    public ValidationException(String message) {
        super(message);
        this.fieldErrors = new HashMap<>();
    }
    
    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors != null ? fieldErrors : new HashMap<>();
    }
    
    public ValidationException(String field, String fieldMessage) {
        super("Validation failed");
        this.fieldErrors = new HashMap<>();
        this.fieldErrors.put(field, fieldMessage);
    }
    
    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
    
    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
    
    /**
     * Builder for creating ValidationException with multiple field errors.
     */
    public static class Builder {
        private final String message;
        private final Map<String, String> fieldErrors = new HashMap<>();
        
        public Builder(String message) {
            this.message = message;
        }
        
        public Builder addFieldError(String field, String fieldMessage) {
            this.fieldErrors.put(field, fieldMessage);
            return this;
        }
        
        public ValidationException build() {
            return new ValidationException(message, fieldErrors);
        }
        
        public boolean hasErrors() {
            return !fieldErrors.isEmpty();
        }
    }
}
