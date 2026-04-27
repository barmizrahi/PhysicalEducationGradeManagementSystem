package com.pe.grademanagement.entity;

/**
 * Enumeration for test calculation types.
 * Defines how grades are calculated from raw test results.
 */
public enum CalculationType {
    /**
     * RATIO calculation: grade = (rawResult / maxValue) * 100
     * Used when grading is based on percentage of maximum value.
     * Example: 15 repetitions out of 20 max = 75%
     */
    RATIO,
    
    /**
     * PENALTY calculation: grade = 100 - ((rawResult - targetValue) * penaltyPerUnit)
     * Used when grading starts at 100 and deducts points for deviation from target.
     * Example: Target 10 minutes, actual 12 minutes, penalty 5 per minute = 90
     */
    PENALTY
}
