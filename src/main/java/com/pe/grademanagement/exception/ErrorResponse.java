package com.pe.grademanagement.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Standardized error response format for all API errors.
 * 
 * Provides consistent error structure across the application:
 * - Error code for programmatic handling
 * - User-friendly message
 * - Optional field-level validation details
 * - Timestamp for debugging
 * 
 * Requirements:
 * - Error Handling: Return consistent error JSON format
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private final ErrorDetails error;
    
    public ErrorResponse(String code, String message) {
        this.error = new ErrorDetails(code, message, null, Instant.now());
    }
    
    public ErrorResponse(String code, String message, List<FieldError> details) {
        this.error = new ErrorDetails(code, message, details, Instant.now());
    }
    
    public ErrorDetails getError() {
        return error;
    }
    
    /**
     * Inner class containing error details.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {
        private final String code;
        private final String message;
        private final List<FieldError> details;
        private final Instant timestamp;
        
        public ErrorDetails(String code, String message, List<FieldError> details, Instant timestamp) {
            this.code = code;
            this.message = message;
            this.details = details;
            this.timestamp = timestamp;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getMessage() {
            return message;
        }
        
        public List<FieldError> getDetails() {
            return details;
        }
        
        public Instant getTimestamp() {
            return timestamp;
        }
    }
    
    /**
     * Field-level error details for validation errors.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FieldError {
        private final String field;
        private final String message;
        
        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }
        
        public String getField() {
            return field;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    /**
     * Builder for creating ErrorResponse with field errors.
     */
    public static class Builder {
        private final String code;
        private final String message;
        private final List<FieldError> details = new ArrayList<>();
        
        public Builder(String code, String message) {
            this.code = code;
            this.message = message;
        }
        
        public Builder addFieldError(String field, String message) {
            this.details.add(new FieldError(field, message));
            return this;
        }
        
        public ErrorResponse build() {
            return new ErrorResponse(code, message, details.isEmpty() ? null : details);
        }
    }
}
