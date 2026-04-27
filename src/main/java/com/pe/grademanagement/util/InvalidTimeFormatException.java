package com.pe.grademanagement.util;

/**
 * Exception thrown when a time string cannot be parsed into mm:ss format.
 * 
 * This exception is used by TimeConverter to indicate invalid time input.
 */
public class InvalidTimeFormatException extends RuntimeException {
    
    public InvalidTimeFormatException(String message) {
        super(message);
    }
    
    public InvalidTimeFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
