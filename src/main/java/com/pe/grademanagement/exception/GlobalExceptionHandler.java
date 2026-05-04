package com.pe.grademanagement.exception;

import com.pe.grademanagement.util.InvalidExcelFormatException;
import com.pe.grademanagement.util.InvalidTimeFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for the application.
 * 
 * Provides centralized exception handling across all controllers using @ControllerAdvice.
 * Returns consistent error JSON format for all exceptions.
 * Logs errors with appropriate context for debugging.
 * 
 * Requirements:
 * - Error Handling: Create @ControllerAdvice for exception handling
 * - Error Handling: Return consistent error JSON format
 * - Error Handling: Log errors with context
 * - Error Handling: Handle validation errors, business logic errors, system errors
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle validation errors from @Valid annotations.
     * Returns 400 Bad Request with field-level error details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        logger.warn("Validation error on request {}: {}", 
                request.getDescription(false), ex.getMessage());
        
        ErrorResponse.Builder builder = new ErrorResponse.Builder(
                "VALIDATION_ERROR",
                "Input validation failed"
        );
        
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            builder.addFieldError(error.getField(), error.getDefaultMessage());
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(builder.build());
    }
    
    /**
     * Handle custom validation exceptions.
     * Returns 400 Bad Request with validation error details.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, WebRequest request) {
        
        logger.warn("Validation error on request {}: {}", 
                request.getDescription(false), ex.getMessage());
        
        if (ex.hasFieldErrors()) {
            ErrorResponse.Builder builder = new ErrorResponse.Builder(
                    "VALIDATION_ERROR",
                    ex.getMessage()
            );
            
            ex.getFieldErrors().forEach(builder::addFieldError);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(builder.build());
        } else {
            ErrorResponse response = new ErrorResponse("VALIDATION_ERROR", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    /**
     * Handle invalid time format exceptions.
     * Returns 400 Bad Request with descriptive error message.
     */
    @ExceptionHandler(InvalidTimeFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTimeFormatException(
            InvalidTimeFormatException ex, WebRequest request) {
        
        logger.warn("Invalid time format on request {}: {}", 
                request.getDescription(false), ex.getMessage());
        
        ErrorResponse response = new ErrorResponse("INVALID_TIME_FORMAT", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handle invalid Excel format exceptions.
     * Returns 400 Bad Request with descriptive error message.
     */
    @ExceptionHandler(InvalidExcelFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidExcelFormatException(
            InvalidExcelFormatException ex, WebRequest request) {
        
        logger.warn("Invalid Excel format on request {}: {}", 
                request.getDescription(false), ex.getMessage());
        
        ErrorResponse response = new ErrorResponse("INVALID_EXCEL_FORMAT", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handle illegal argument exceptions (typically from business logic validation).
     * Returns 400 Bad Request with error message.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        logger.warn("Illegal argument on request {}: {}", 
                request.getDescription(false), ex.getMessage());
        
        ErrorResponse response = new ErrorResponse("INVALID_ARGUMENT", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handle business logic exceptions.
     * Returns 422 Unprocessable Entity with business error details.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {
        
        logger.warn("Business logic error on request {}: {}", 
                request.getDescription(false), ex.getMessage());
        
        ErrorResponse response = new ErrorResponse(ex.getErrorCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }
    
    /**
     * Handle resource not found exceptions.
     * Returns 404 Not Found with resource details.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        logger.warn("Resource not found on request {}: {}", 
                request.getDescription(false), ex.getMessage());
        
        ErrorResponse response = new ErrorResponse("RESOURCE_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Handle authentication exceptions (bad credentials, etc.).
     * Returns 401 Unauthorized with generic error message.
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            Exception ex, WebRequest request) {
        
        logger.warn("Authentication failed on request {}: {}", 
                request.getDescription(false), ex.getMessage());
        
        // Don't expose detailed authentication failure reasons for security
        ErrorResponse response = new ErrorResponse(
                "AUTHENTICATION_FAILED",
                "Authentication failed. Please check your credentials."
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Handle authorization exceptions (access denied).
     * Returns 403 Forbidden with error message.
     */
    @ExceptionHandler({AccessDeniedException.class, UnauthorizedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            Exception ex, WebRequest request) {
        
        logger.warn("Access denied on request {}: {}", 
                request.getDescription(false), ex.getMessage());
        
        ErrorResponse response = new ErrorResponse(
                "ACCESS_DENIED",
                "You do not have permission to access this resource"
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    /**
     * Handle file upload size exceeded exceptions.
     * Returns 413 Payload Too Large with error message.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, WebRequest request) {
        
        logger.warn("File upload size exceeded on request {}: {}", 
                request.getDescription(false), ex.getMessage());
        
        ErrorResponse response = new ErrorResponse(
                "FILE_TOO_LARGE",
                "The uploaded file exceeds the maximum allowed size"
        );
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }
    
    /**
     * Handle all other uncaught exceptions.
     * Returns 500 Internal Server Error with generic message.
     * Logs full stack trace for debugging.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        
        // Log full stack trace for system errors
        logger.error("Unexpected error on request {}: {}", 
                request.getDescription(false), ex.getMessage(), ex);
        
        // Don't expose internal error details to users
        ErrorResponse response = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
