package com.pe.grademanagement.util;

/**
 * Exception thrown when an Excel file has an invalid format.
 * Used by ExcelImporter to signal parsing errors.
 */
public class InvalidExcelFormatException extends Exception {
    
    /**
     * Constructs a new exception with the specified detail message.
     * 
     * @param message The detail message
     */
    public InvalidExcelFormatException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new exception with the specified detail message and cause.
     * 
     * @param message The detail message
     * @param cause The cause of the exception
     */
    public InvalidExcelFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
