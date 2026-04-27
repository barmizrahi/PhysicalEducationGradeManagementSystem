package com.pe.grademanagement.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for converting time values between mm:ss format and decimal minutes.
 * 
 * The system stores all TIME values as decimal numbers internally (e.g., 10.5 minutes),
 * but accepts user input in mm:ss format (e.g., "10:30") for ease of use.
 * 
 * Requirements: 15.1, 15.2, 15.3
 */
public class TimeConverter {
    
    private static final Pattern TIME_PATTERN = Pattern.compile("^(\\d+):(\\d{2})$");
    
    /**
     * Convert mm:ss format to decimal minutes.
     * 
     * @param timeString Time in mm:ss format (e.g., "10:30")
     * @return Decimal minutes (e.g., 10.5)
     * @throws InvalidTimeFormatException if format is invalid
     */
    public BigDecimal convertToDecimalMinutes(String timeString) {
        if (timeString == null || timeString.trim().isEmpty()) {
            throw new InvalidTimeFormatException("Time string cannot be null or empty");
        }
        
        String trimmed = timeString.trim();
        Matcher matcher = TIME_PATTERN.matcher(trimmed);
        
        if (!matcher.matches()) {
            throw new InvalidTimeFormatException(
                "Invalid time format: '" + timeString + "'. Expected format: mm:ss (e.g., 10:30)"
            );
        }
        
        try {
            int minutes = Integer.parseInt(matcher.group(1));
            int seconds = Integer.parseInt(matcher.group(2));
            
            if (seconds >= 60) {
                throw new InvalidTimeFormatException(
                    "Invalid seconds value: " + seconds + ". Seconds must be between 00 and 59"
                );
            }
            
            if (minutes < 0 || seconds < 0) {
                throw new InvalidTimeFormatException(
                    "Time values cannot be negative"
                );
            }
            
            // Convert to decimal minutes: minutes + (seconds / 60)
            BigDecimal decimalMinutes = BigDecimal.valueOf(minutes)
                .add(BigDecimal.valueOf(seconds).divide(BigDecimal.valueOf(60), 10, RoundingMode.HALF_UP));
            
            // Round to 2 decimal places for storage
            return decimalMinutes.setScale(2, RoundingMode.HALF_UP);
            
        } catch (NumberFormatException e) {
            throw new InvalidTimeFormatException(
                "Invalid numeric values in time string: '" + timeString + "'", e
            );
        }
    }
    
    /**
     * Convert decimal minutes to mm:ss format.
     * 
     * @param decimalMinutes Decimal minutes (e.g., 10.5)
     * @return Time in mm:ss format (e.g., "10:30")
     * @throws IllegalArgumentException if decimalMinutes is null or negative
     */
    public String convertToTimeFormat(BigDecimal decimalMinutes) {
        if (decimalMinutes == null) {
            throw new IllegalArgumentException("Decimal minutes cannot be null");
        }
        
        if (decimalMinutes.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Decimal minutes cannot be negative");
        }
        
        // Extract whole minutes
        int minutes = decimalMinutes.intValue();
        
        // Extract fractional part and convert to seconds
        BigDecimal fractionalMinutes = decimalMinutes.subtract(BigDecimal.valueOf(minutes));
        int seconds = fractionalMinutes.multiply(BigDecimal.valueOf(60))
            .setScale(0, RoundingMode.HALF_UP)
            .intValue();
        
        // Handle edge case where rounding seconds gives 60
        if (seconds >= 60) {
            minutes += 1;
            seconds = 0;
        }
        
        // Format as mm:ss with zero-padded seconds
        return String.format("%d:%02d", minutes, seconds);
    }
}
