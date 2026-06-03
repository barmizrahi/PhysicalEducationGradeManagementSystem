package com.pe.grademanagement.util;

import com.pe.grademanagement.entity.CalculationType;
import com.pe.grademanagement.entity.Test;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * GradeCalculator component for calculating grades from raw test results.
 * Supports two calculation methods: RATIO and PENALTY.
 * 
 * Requirements:
 * - 4.1: RATIO calculation: grade = (rawResult / maxValue) * 100
 * - 4.2: Support non-integer raw results
 * - 4.3: Cap RATIO grades at 100
 * - 4.4: Return 0 for zero raw results
 * - 4.5: Round to 2 decimal places
 * - 5.1: PENALTY calculation: grade = 100 - ((rawResult - targetValue) / penaltyUnit) * penaltyPerUnit
 * - 5.2: Support non-integer raw results
 * - 5.3: Cap PENALTY grades at 100 (no bonus)
 * - 5.4: Cap PENALTY grades at 0 (no negative)
 * - 5.5: Linear calculation for all deviations
 * - 5.6: Round to 2 decimal places
 */
@Component
public class GradeCalculator {
    
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    /**
     * Calculate grade based on test configuration.
     * Dispatches to appropriate calculation method based on test type.
     * 
     * @param rawResult Raw test result (decimal), can be null
     * @param test Test configuration containing calculation parameters
     * @return Calculated grade (0-100, rounded to 2 decimal places)
     * @throws IllegalArgumentException if test configuration is invalid
     */
    public BigDecimal calculateGrade(BigDecimal rawResult, Test test) {
        if (test == null) {
            throw new IllegalArgumentException("Test configuration cannot be null");
        }
        
        // Handle null raw result - return 0 (Requirement 8.1, 8.4)
        if (rawResult == null) {
            return ZERO.setScale(SCALE, ROUNDING_MODE);
        }
        
        // Handle zero raw result - return 0 (Requirement 4.4)
        if (rawResult.compareTo(ZERO) == 0) {
            return ZERO.setScale(SCALE, ROUNDING_MODE);
        }
        
        // Dispatch to appropriate calculation method
        if (test.getCalculationType() == CalculationType.RATIO) {
            return calculateRatioGrade(rawResult, test.getMaxValue());
        } else if (test.getCalculationType() == CalculationType.PENALTY) {
            return calculatePenaltyGrade(rawResult, test.getTargetValue(), test.getPenaltyPerUnit(), test.getPenaltyUnit());
        } else {
            throw new IllegalArgumentException("Unknown calculation type: " + test.getCalculationType());
        }
    }
    
    /**
     * Calculate grade using RATIO method.
     * Formula: grade = (rawResult / maxValue) * 100
     * 
     * Requirements:
     * - 4.1: Apply ratio formula
     * - 4.2: Support non-integer values
     * - 4.3: Cap at 100
     * - 4.5: Round to 2 decimal places
     * 
     * @param rawResult Raw test result (must be non-null and non-negative)
     * @param maxValue Maximum value for 100% grade (must be positive)
     * @return Calculated grade (0-100, rounded to 2 decimal places)
     * @throws IllegalArgumentException if parameters are invalid
     */
    public BigDecimal calculateRatioGrade(BigDecimal rawResult, BigDecimal maxValue) {
        // Validate inputs
        if (rawResult == null) {
            throw new IllegalArgumentException("Raw result cannot be null");
        }
        if (maxValue == null) {
            throw new IllegalArgumentException("Max value cannot be null for RATIO calculation");
        }
        if (maxValue.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("Max value must be positive");
        }
        if (rawResult.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Raw result cannot be negative");
        }
        
        // Calculate: (rawResult / maxValue) * 100
        BigDecimal ratio = rawResult.divide(maxValue, 4, ROUNDING_MODE);
        BigDecimal grade = ratio.multiply(ONE_HUNDRED);
        
        // Cap at 100 (Requirement 4.3)
        if (grade.compareTo(ONE_HUNDRED) > 0) {
            grade = ONE_HUNDRED;
        }
        
        // Round to 2 decimal places (Requirement 4.5)
        return grade.setScale(SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate grade using PENALTY method.
     * Formula: grade = 100 - ((rawResult - targetValue) / penaltyUnit) * penaltyPerUnit
     *
     * The penaltyUnit defines the size of one deduction interval. For TIME tests it lets
     * the teacher deduct points per arbitrary interval (e.g. penaltyUnit = 0.75 minutes = 45s),
     * so every 45 seconds over target costs penaltyPerUnit points. When penaltyUnit is null
     * or non-positive it defaults to 1, which reduces to the classic
     * grade = 100 - (deviation * penaltyPerUnit) used by COUNT tests.
     *
     * Example: target 10:30 (10.5), result 11:45 (11.75), penaltyPerUnit 5, penaltyUnit 0:45 (0.75)
     *   deviation = 1.25, units = 1.25 / 0.75 = 1.667, grade = 100 - 1.667 * 5 = 91.67
     *
     * Requirements:
     * - 5.1: Apply penalty formula
     * - 5.2: Support non-integer values
     * - 5.3: Cap at 100 (no bonus above 100)
     * - 5.4: Cap at 0 (no negative grades)
     * - 5.5: Linear calculation for all deviations
     * - 5.6: Round to 2 decimal places
     *
     * @param rawResult Raw test result (must be non-null and non-negative)
     * @param targetValue Target value for 100% grade (must be positive)
     * @param penaltyPerUnit Penalty per deduction unit (must be positive)
     * @param penaltyUnit Size of one deduction interval; defaults to 1 if null or non-positive
     * @return Calculated grade (0-100, rounded to 2 decimal places)
     * @throws IllegalArgumentException if parameters are invalid
     */
    public BigDecimal calculatePenaltyGrade(BigDecimal rawResult, BigDecimal targetValue,
                                            BigDecimal penaltyPerUnit, BigDecimal penaltyUnit) {
        // Validate inputs
        if (rawResult == null) {
            throw new IllegalArgumentException("Raw result cannot be null");
        }
        if (targetValue == null) {
            throw new IllegalArgumentException("Target value cannot be null for PENALTY calculation");
        }
        if (penaltyPerUnit == null) {
            throw new IllegalArgumentException("Penalty per unit cannot be null for PENALTY calculation");
        }
        if (targetValue.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("Target value must be positive");
        }
        if (penaltyPerUnit.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("Penalty per unit must be positive");
        }
        if (rawResult.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Raw result cannot be negative");
        }

        // Default penaltyUnit to 1 (classic per-unit penalty) when missing or non-positive
        BigDecimal effectivePenaltyUnit = (penaltyUnit != null && penaltyUnit.compareTo(ZERO) > 0)
                ? penaltyUnit
                : BigDecimal.ONE;

        // Calculate: 100 - ((rawResult - targetValue) / penaltyUnit) * penaltyPerUnit
        BigDecimal deviation = rawResult.subtract(targetValue);
        BigDecimal units = deviation.divide(effectivePenaltyUnit, 4, ROUNDING_MODE);
        BigDecimal penalty = units.multiply(penaltyPerUnit);
        BigDecimal grade = ONE_HUNDRED.subtract(penalty);
        
        // Cap at 100 (no bonus above 100) (Requirement 5.3)
        if (grade.compareTo(ONE_HUNDRED) > 0) {
            grade = ONE_HUNDRED;
        }
        
        // Cap at 0 (no negative grades) (Requirement 5.4)
        if (grade.compareTo(ZERO) < 0) {
            grade = ZERO;
        }
        
        // Round to 2 decimal places (Requirement 5.6)
        return grade.setScale(SCALE, ROUNDING_MODE);
    }
}
