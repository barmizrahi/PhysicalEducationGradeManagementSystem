package com.pe.grademanagement.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of Excel file validation.
 * Contains validation status and any error messages.
 */
public class ValidationResult {
    
    private boolean valid;
    private List<String> errors;
    
    /**
     * Default constructor creates a valid result with no errors.
     */
    public ValidationResult() {
        this.valid = true;
        this.errors = new ArrayList<>();
    }
    
    /**
     * Constructor with validation status.
     * 
     * @param valid Whether the validation passed
     */
    public ValidationResult(boolean valid) {
        this.valid = valid;
        this.errors = new ArrayList<>();
    }
    
    /**
     * Adds an error message and marks the result as invalid.
     * 
     * @param error Error message to add
     */
    public void addError(String error) {
        this.valid = false;
        this.errors.add(error);
    }
    
    /**
     * Checks if the validation passed.
     * 
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * Sets the validation status.
     * 
     * @param valid Validation status
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    /**
     * Gets all error messages.
     * 
     * @return List of error messages
     */
    public List<String> getErrors() {
        return errors;
    }
    
    /**
     * Sets the error messages.
     * 
     * @param errors List of error messages
     */
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    
    /**
     * Gets a formatted error message containing all errors.
     * 
     * @return Formatted error message
     */
    public String getErrorMessage() {
        if (valid || errors.isEmpty()) {
            return "";
        }
        return String.join("; ", errors);
    }
    
    @Override
    public String toString() {
        return "ValidationResult{" +
                "valid=" + valid +
                ", errors=" + errors +
                '}';
    }
}
