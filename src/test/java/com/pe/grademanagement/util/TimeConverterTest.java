package com.pe.grademanagement.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for TimeConverter utility class.
 * 
 * These tests verify specific examples and edge cases for time format conversion.
 */
@DisplayName("TimeConverter Unit Tests")
class TimeConverterTest {
    
    private TimeConverter timeConverter;
    
    @BeforeEach
    void setUp() {
        timeConverter = new TimeConverter();
    }
    
    // Tests for convertToDecimalMinutes
    
    @Test
    @DisplayName("Should convert 10:30 to 10.5 decimal minutes")
    void shouldConvertTenThirtyToDecimal() {
        BigDecimal result = timeConverter.convertToDecimalMinutes("10:30");
        assertThat(result).isEqualByComparingTo("10.5");
    }
    
    @Test
    @DisplayName("Should convert 0:00 to 0.0 decimal minutes")
    void shouldConvertZeroToDecimal() {
        BigDecimal result = timeConverter.convertToDecimalMinutes("0:00");
        assertThat(result).isEqualByComparingTo("0.0");
    }
    
    @Test
    @DisplayName("Should convert 5:15 to 5.25 decimal minutes")
    void shouldConvertFiveFifteenToDecimal() {
        BigDecimal result = timeConverter.convertToDecimalMinutes("5:15");
        assertThat(result).isEqualByComparingTo("5.25");
    }
    
    @Test
    @DisplayName("Should convert 1:00 to 1.0 decimal minutes")
    void shouldConvertOneMinuteToDecimal() {
        BigDecimal result = timeConverter.convertToDecimalMinutes("1:00");
        assertThat(result).isEqualByComparingTo("1.0");
    }
    
    @Test
    @DisplayName("Should convert 0:30 to 0.5 decimal minutes")
    void shouldConvertThirtySecondsToDecimal() {
        BigDecimal result = timeConverter.convertToDecimalMinutes("0:30");
        assertThat(result).isEqualByComparingTo("0.5");
    }
    
    @Test
    @DisplayName("Should convert 15:45 to 15.75 decimal minutes")
    void shouldConvertFifteenFortyFiveToDecimal() {
        BigDecimal result = timeConverter.convertToDecimalMinutes("15:45");
        assertThat(result).isEqualByComparingTo("15.75");
    }
    
    @Test
    @DisplayName("Should handle large minute values")
    void shouldHandleLargeMinuteValues() {
        BigDecimal result = timeConverter.convertToDecimalMinutes("120:30");
        assertThat(result).isEqualByComparingTo("120.5");
    }
    
    @Test
    @DisplayName("Should trim whitespace from input")
    void shouldTrimWhitespace() {
        BigDecimal result = timeConverter.convertToDecimalMinutes("  10:30  ");
        assertThat(result).isEqualByComparingTo("10.5");
    }
    
    @Test
    @DisplayName("Should throw exception for null input")
    void shouldThrowExceptionForNullInput() {
        assertThatThrownBy(() -> timeConverter.convertToDecimalMinutes(null))
            .isInstanceOf(InvalidTimeFormatException.class)
            .hasMessageContaining("cannot be null or empty");
    }
    
    @Test
    @DisplayName("Should throw exception for empty input")
    void shouldThrowExceptionForEmptyInput() {
        assertThatThrownBy(() -> timeConverter.convertToDecimalMinutes(""))
            .isInstanceOf(InvalidTimeFormatException.class)
            .hasMessageContaining("cannot be null or empty");
    }
    
    @Test
    @DisplayName("Should throw exception for invalid format - no colon")
    void shouldThrowExceptionForNoColon() {
        assertThatThrownBy(() -> timeConverter.convertToDecimalMinutes("1030"))
            .isInstanceOf(InvalidTimeFormatException.class)
            .hasMessageContaining("Invalid time format");
    }
    
    @Test
    @DisplayName("Should throw exception for invalid format - single digit seconds")
    void shouldThrowExceptionForSingleDigitSeconds() {
        assertThatThrownBy(() -> timeConverter.convertToDecimalMinutes("10:3"))
            .isInstanceOf(InvalidTimeFormatException.class)
            .hasMessageContaining("Invalid time format");
    }
    
    @Test
    @DisplayName("Should throw exception for seconds >= 60")
    void shouldThrowExceptionForSecondsGreaterThanSixty() {
        assertThatThrownBy(() -> timeConverter.convertToDecimalMinutes("10:60"))
            .isInstanceOf(InvalidTimeFormatException.class)
            .hasMessageContaining("Seconds must be between 00 and 59");
    }
    
    @Test
    @DisplayName("Should throw exception for non-numeric values")
    void shouldThrowExceptionForNonNumericValues() {
        assertThatThrownBy(() -> timeConverter.convertToDecimalMinutes("abc:de"))
            .isInstanceOf(InvalidTimeFormatException.class)
            .hasMessageContaining("Invalid time format");
    }
    
    // Tests for convertToTimeFormat
    
    @Test
    @DisplayName("Should convert 10.5 decimal minutes to 10:30")
    void shouldConvertDecimalToTenThirty() {
        String result = timeConverter.convertToTimeFormat(new BigDecimal("10.5"));
        assertThat(result).isEqualTo("10:30");
    }
    
    @Test
    @DisplayName("Should convert 0.0 decimal minutes to 0:00")
    void shouldConvertZeroDecimalToTime() {
        String result = timeConverter.convertToTimeFormat(BigDecimal.ZERO);
        assertThat(result).isEqualTo("0:00");
    }
    
    @Test
    @DisplayName("Should convert 5.25 decimal minutes to 5:15")
    void shouldConvertFivePointTwoFiveToTime() {
        String result = timeConverter.convertToTimeFormat(new BigDecimal("5.25"));
        assertThat(result).isEqualTo("5:15");
    }
    
    @Test
    @DisplayName("Should convert 1.0 decimal minutes to 1:00")
    void shouldConvertOneDecimalToTime() {
        String result = timeConverter.convertToTimeFormat(BigDecimal.ONE);
        assertThat(result).isEqualTo("1:00");
    }
    
    @Test
    @DisplayName("Should convert 0.5 decimal minutes to 0:30")
    void shouldConvertHalfMinuteToTime() {
        String result = timeConverter.convertToTimeFormat(new BigDecimal("0.5"));
        assertThat(result).isEqualTo("0:30");
    }
    
    @Test
    @DisplayName("Should convert 15.75 decimal minutes to 15:45")
    void shouldConvertFifteenPointSevenFiveToTime() {
        String result = timeConverter.convertToTimeFormat(new BigDecimal("15.75"));
        assertThat(result).isEqualTo("15:45");
    }
    
    @Test
    @DisplayName("Should handle large decimal values")
    void shouldHandleLargeDecimalValues() {
        String result = timeConverter.convertToTimeFormat(new BigDecimal("120.5"));
        assertThat(result).isEqualTo("120:30");
    }
    
    @Test
    @DisplayName("Should round seconds properly - 10.83 to 10:50")
    void shouldRoundSecondsUp() {
        String result = timeConverter.convertToTimeFormat(new BigDecimal("10.83"));
        assertThat(result).isEqualTo("10:50");
    }
    
    @Test
    @DisplayName("Should handle rounding that produces 60 seconds")
    void shouldHandleRoundingToSixtySeconds() {
        // 10.9917 minutes = 10 minutes + 59.5 seconds, should round to 11:00
        String result = timeConverter.convertToTimeFormat(new BigDecimal("10.9917"));
        assertThat(result).isEqualTo("11:00");
    }
    
    @Test
    @DisplayName("Should throw exception for null decimal input")
    void shouldThrowExceptionForNullDecimal() {
        assertThatThrownBy(() -> timeConverter.convertToTimeFormat(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be null");
    }
    
    @Test
    @DisplayName("Should throw exception for negative decimal input")
    void shouldThrowExceptionForNegativeDecimal() {
        assertThatThrownBy(() -> timeConverter.convertToTimeFormat(new BigDecimal("-5.5")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cannot be negative");
    }
    
    // Round-trip tests
    
    @Test
    @DisplayName("Should maintain value through round-trip conversion - 10:30")
    void shouldMaintainValueThroughRoundTrip() {
        String original = "10:30";
        BigDecimal decimal = timeConverter.convertToDecimalMinutes(original);
        String result = timeConverter.convertToTimeFormat(decimal);
        assertThat(result).isEqualTo(original);
    }
    
    @Test
    @DisplayName("Should maintain value through round-trip conversion - 5:15")
    void shouldMaintainValueThroughRoundTripFiveFifteen() {
        String original = "5:15";
        BigDecimal decimal = timeConverter.convertToDecimalMinutes(original);
        String result = timeConverter.convertToTimeFormat(decimal);
        assertThat(result).isEqualTo(original);
    }
}
