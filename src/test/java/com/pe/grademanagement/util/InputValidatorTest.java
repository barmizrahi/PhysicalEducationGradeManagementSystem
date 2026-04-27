package com.pe.grademanagement.util;

import com.pe.grademanagement.entity.UnitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InputValidator class.
 * 
 * Tests validation of raw test results for both TIME and COUNT unit types.
 * Validates: Requirements 14.1, 14.2, 14.3, 14.4, 14.5
 */
class InputValidatorTest {
    
    private InputValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new InputValidator();
    }
    
    // TIME validation tests
    
    @Test
    @DisplayName("Valid TIME format (mm:ss) should pass validation")
    void testValidTimeFormat() {
        ValidationResult result = validator.validateRawResult("10:30", UnitType.TIME);
        assertTrue(result.isValid(), "Valid time format should pass validation");
        assertTrue(result.getErrors().isEmpty(), "Valid input should have no errors");
    }
    
    @Test
    @DisplayName("Valid TIME format with zero seconds should pass validation")
    void testValidTimeFormatZeroSeconds() {
        ValidationResult result = validator.validateRawResult("5:00", UnitType.TIME);
        assertTrue(result.isValid(), "Valid time format with zero seconds should pass");
    }
    
    @Test
    @DisplayName("Valid TIME format with single digit minutes should pass validation")
    void testValidTimeFormatSingleDigitMinutes() {
        ValidationResult result = validator.validateRawResult("0:45", UnitType.TIME);
        assertTrue(result.isValid(), "Valid time format with single digit minutes should pass");
    }
    
    @Test
    @DisplayName("Invalid TIME format (missing colon) should fail validation")
    void testInvalidTimeFormatMissingColon() {
        ValidationResult result = validator.validateRawResult("1030", UnitType.TIME);
        assertFalse(result.isValid(), "Time format without colon should fail");
        assertFalse(result.getErrors().isEmpty(), "Should have error message");
        assertTrue(result.getErrorMessage().contains("Invalid time format"), 
            "Error message should mention invalid format");
    }
    
    @Test
    @DisplayName("Invalid TIME format (seconds >= 60) should fail validation")
    void testInvalidTimeFormatSecondsOver60() {
        ValidationResult result = validator.validateRawResult("10:60", UnitType.TIME);
        assertFalse(result.isValid(), "Time with seconds >= 60 should fail");
        assertTrue(result.getErrorMessage().contains("seconds"), 
            "Error message should mention seconds issue");
    }
    
    @Test
    @DisplayName("Invalid TIME format (non-numeric) should fail validation")
    void testInvalidTimeFormatNonNumeric() {
        ValidationResult result = validator.validateRawResult("abc:def", UnitType.TIME);
        assertFalse(result.isValid(), "Non-numeric time should fail");
        assertFalse(result.getErrors().isEmpty(), "Should have error message");
    }
    
    @Test
    @DisplayName("Invalid TIME format (wrong format) should fail validation")
    void testInvalidTimeFormatWrongFormat() {
        ValidationResult result = validator.validateRawResult("10:5", UnitType.TIME);
        assertFalse(result.isValid(), "Time with single digit seconds should fail");
        assertTrue(result.getErrorMessage().contains("Invalid time format"), 
            "Error message should mention invalid format");
    }
    
    // COUNT validation tests
    
    @Test
    @DisplayName("Valid COUNT format (integer) should pass validation")
    void testValidCountFormatInteger() {
        ValidationResult result = validator.validateRawResult("15", UnitType.COUNT);
        assertTrue(result.isValid(), "Valid integer count should pass validation");
        assertTrue(result.getErrors().isEmpty(), "Valid input should have no errors");
    }
    
    @Test
    @DisplayName("Valid COUNT format (decimal) should pass validation")
    void testValidCountFormatDecimal() {
        ValidationResult result = validator.validateRawResult("15.5", UnitType.COUNT);
        assertTrue(result.isValid(), "Valid decimal count should pass validation");
        assertTrue(result.getErrors().isEmpty(), "Valid input should have no errors");
    }
    
    @Test
    @DisplayName("Valid COUNT format (zero) should pass validation")
    void testValidCountFormatZero() {
        ValidationResult result = validator.validateRawResult("0", UnitType.COUNT);
        assertTrue(result.isValid(), "Zero count should pass validation");
    }
    
    @Test
    @DisplayName("Invalid COUNT format (non-numeric) should fail validation")
    void testInvalidCountFormatNonNumeric() {
        ValidationResult result = validator.validateRawResult("abc", UnitType.COUNT);
        assertFalse(result.isValid(), "Non-numeric count should fail");
        assertTrue(result.getErrorMessage().contains("Invalid numeric format"), 
            "Error message should mention invalid numeric format");
    }
    
    @Test
    @DisplayName("Invalid COUNT format (mixed alphanumeric) should fail validation")
    void testInvalidCountFormatMixedAlphanumeric() {
        ValidationResult result = validator.validateRawResult("15abc", UnitType.COUNT);
        assertFalse(result.isValid(), "Mixed alphanumeric count should fail");
        assertTrue(result.getErrorMessage().contains("Invalid numeric format"), 
            "Error message should mention invalid numeric format");
    }
    
    // Negative value tests
    
    @Test
    @DisplayName("Negative COUNT value should fail validation")
    void testNegativeCountValue() {
        ValidationResult result = validator.validateRawResult("-5", UnitType.COUNT);
        assertFalse(result.isValid(), "Negative count should fail validation");
        assertTrue(result.getErrorMessage().contains("cannot be negative"), 
            "Error message should mention negative value");
    }
    
    @Test
    @DisplayName("Negative decimal COUNT value should fail validation")
    void testNegativeDecimalCountValue() {
        ValidationResult result = validator.validateRawResult("-5.5", UnitType.COUNT);
        assertFalse(result.isValid(), "Negative decimal count should fail validation");
        assertTrue(result.getErrorMessage().contains("cannot be negative"), 
            "Error message should mention negative value");
    }
    
    // Empty/null input tests
    
    @Test
    @DisplayName("Null input should fail validation")
    void testNullInput() {
        ValidationResult result = validator.validateRawResult(null, UnitType.COUNT);
        assertFalse(result.isValid(), "Null input should fail validation");
        assertTrue(result.getErrorMessage().contains("cannot be empty"), 
            "Error message should mention empty input");
    }
    
    @Test
    @DisplayName("Empty string input should fail validation")
    void testEmptyStringInput() {
        ValidationResult result = validator.validateRawResult("", UnitType.COUNT);
        assertFalse(result.isValid(), "Empty string should fail validation");
        assertTrue(result.getErrorMessage().contains("cannot be empty"), 
            "Error message should mention empty input");
    }
    
    @Test
    @DisplayName("Whitespace-only input should fail validation")
    void testWhitespaceOnlyInput() {
        ValidationResult result = validator.validateRawResult("   ", UnitType.COUNT);
        assertFalse(result.isValid(), "Whitespace-only input should fail validation");
        assertTrue(result.getErrorMessage().contains("cannot be empty"), 
            "Error message should mention empty input");
    }
    
    // Whitespace handling tests
    
    @Test
    @DisplayName("Valid COUNT with leading/trailing whitespace should pass validation")
    void testValidCountWithWhitespace() {
        ValidationResult result = validator.validateRawResult("  15  ", UnitType.COUNT);
        assertTrue(result.isValid(), "Valid count with whitespace should pass after trimming");
    }
    
    @Test
    @DisplayName("Valid TIME with leading/trailing whitespace should pass validation")
    void testValidTimeWithWhitespace() {
        ValidationResult result = validator.validateRawResult("  10:30  ", UnitType.TIME);
        assertTrue(result.isValid(), "Valid time with whitespace should pass after trimming");
    }
    
    // Edge cases
    
    @Test
    @DisplayName("Very large COUNT value should pass validation")
    void testVeryLargeCountValue() {
        ValidationResult result = validator.validateRawResult("999999", UnitType.COUNT);
        assertTrue(result.isValid(), "Very large count should pass validation");
    }
    
    @Test
    @DisplayName("Very large TIME value should pass validation")
    void testVeryLargeTimeValue() {
        ValidationResult result = validator.validateRawResult("999:59", UnitType.TIME);
        assertTrue(result.isValid(), "Very large time should pass validation");
    }
    
    @Test
    @DisplayName("COUNT with many decimal places should pass validation")
    void testCountWithManyDecimals() {
        ValidationResult result = validator.validateRawResult("15.123456", UnitType.COUNT);
        assertTrue(result.isValid(), "Count with many decimals should pass validation");
    }
    
    // Descriptive error message tests
    
    @Test
    @DisplayName("Error messages should be descriptive for TIME format errors")
    void testDescriptiveErrorMessageForTimeFormat() {
        ValidationResult result = validator.validateRawResult("invalid", UnitType.TIME);
        assertFalse(result.isValid());
        String errorMessage = result.getErrorMessage();
        assertFalse(errorMessage.isEmpty(), "Should have error message");
        assertTrue(errorMessage.contains("mm:ss") || errorMessage.contains("format"), 
            "Error message should describe expected format");
    }
    
    @Test
    @DisplayName("Error messages should be descriptive for COUNT format errors")
    void testDescriptiveErrorMessageForCountFormat() {
        ValidationResult result = validator.validateRawResult("not-a-number", UnitType.COUNT);
        assertFalse(result.isValid());
        String errorMessage = result.getErrorMessage();
        assertFalse(errorMessage.isEmpty(), "Should have error message");
        assertTrue(errorMessage.contains("numeric") || errorMessage.contains("number"), 
            "Error message should describe expected format");
    }
    
    @Test
    @DisplayName("Multiple validation errors should be captured")
    void testMultipleValidationErrors() {
        // This test verifies that ValidationResult can hold multiple errors
        ValidationResult result = new ValidationResult();
        result.addError("Error 1");
        result.addError("Error 2");
        
        assertFalse(result.isValid());
        assertEquals(2, result.getErrors().size());
        assertTrue(result.getErrorMessage().contains("Error 1"));
        assertTrue(result.getErrorMessage().contains("Error 2"));
    }
}
