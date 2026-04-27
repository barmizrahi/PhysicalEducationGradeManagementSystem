package com.pe.grademanagement.util;

import com.pe.grademanagement.entity.UnitType;

import java.math.BigDecimal;

/**
 * Utility class for validating user input for raw test results.
 * 
 * Validates input based on unit type (TIME or COUNT) and ensures:
 * - TIME tests use valid mm:ss format
 * - COUNT tests use valid numeric format
 * - No negative values are accepted
 * - Non-numeric input is rejected for numeric fields
 * 
 * Requirements: 14.1, 14.2, 14.3, 14.4, 14.5
 */
public class InputValidator {
    
    private final TimeConverter timeConverter;
    
    /**
     * Default constructor that creates a new TimeConverter instance.
     */
    public InputValidator() {
        this.timeConverter = new TimeConverter();
    }
    
    /**
     * Constructor with dependency injection for testing.
     * 
     * @param timeConverter TimeConverter instance to use
     */
    public InputValidator(TimeConverter timeConverter) {
        this.timeConverter = timeConverter;
    }
    
    /**
     * Validate raw result input based on unit type.
     * 
     * For TIME tests: validates mm:ss format using TimeConverter
     * For COUNT tests: validates numeric format
     * Rejects negative values for both types
     * 
     * @param input User input string
     * @param unitType Unit type of the test (TIME or COUNT)
     * @return ValidationResult with validation status and error messages
     */
    public ValidationResult validateRawResult(String input, UnitType unitType) {
        ValidationResult result = new ValidationResult();
        
        // Check for null or empty input
        if (input == null || input.trim().isEmpty()) {
            result.addError("Input cannot be empty");
            return result;
        }
        
        String trimmedInput = input.trim();
        
        // Validate based on unit type
        if (unitType == UnitType.TIME) {
            validateTimeInput(trimmedInput, result);
        } else if (unitType == UnitType.COUNT) {
            validateCountInput(trimmedInput, result);
        } else {
            result.addError("Invalid unit type");
        }
        
        return result;
    }
    
    /**
     * Validate TIME format input (mm:ss).
     * 
     * @param input User input string
     * @param result ValidationResult to populate with errors
     */
    private void validateTimeInput(String input, ValidationResult result) {
        try {
            BigDecimal decimalMinutes = timeConverter.convertToDecimalMinutes(input);
            
            // Check for negative values
            if (decimalMinutes.compareTo(BigDecimal.ZERO) < 0) {
                result.addError("Time value cannot be negative");
            }
        } catch (InvalidTimeFormatException e) {
            result.addError(e.getMessage());
        }
    }
    
    /**
     * Validate COUNT format input (numeric).
     * 
     * @param input User input string
     * @param result ValidationResult to populate with errors
     */
    private void validateCountInput(String input, ValidationResult result) {
        try {
            BigDecimal value = new BigDecimal(input);
            
            // Check for negative values
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                result.addError("Count value cannot be negative");
            }
        } catch (NumberFormatException e) {
            result.addError("Invalid numeric format. Expected a number (e.g., 15 or 15.5)");
        }
    }
}
