package com.pe.grademanagement.entity;

/**
 * Enumeration for test unit types.
 * Defines the measurement unit for test results.
 */
public enum UnitType {
    /**
     * TIME unit: measured in decimal minutes.
     * Example: 10.5 minutes (10 minutes 30 seconds)
     * Used for timed tests like running, swimming, etc.
     */
    TIME,
    
    /**
     * COUNT unit: measured in repetitions or counts.
     * Example: 15 repetitions, 20 jumps
     * Used for counting tests like push-ups, sit-ups, etc.
     */
    COUNT
}
