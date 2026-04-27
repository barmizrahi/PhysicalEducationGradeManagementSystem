package com.pe.grademanagement.util;

import com.pe.grademanagement.entity.CalculationType;
import com.pe.grademanagement.entity.UnitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GradeCalculator component.
 * Tests both RATIO and PENALTY calculation methods with various scenarios.
 */
class GradeCalculatorTest {
    
    private GradeCalculator gradeCalculator;
    
    @BeforeEach
    void setUp() {
        gradeCalculator = new GradeCalculator();
    }
    
    // RATIO Calculation Tests
    
    @Test
    void testRatioGrade_ExactMatch() {
        // Test: rawResult equals maxValue should return 100
        BigDecimal result = gradeCalculator.calculateRatioGrade(
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(20)
        );
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(result));
    }
    
    @Test
    void testRatioGrade_HalfValue() {
        // Test: rawResult is half of maxValue should return 50
        BigDecimal result = gradeCalculator.calculateRatioGrade(
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(20)
        );
        assertEquals(0, BigDecimal.valueOf(50.00).compareTo(result));
    }
    
    @Test
    void testRatioGrade_ExceedsMax() {
        // Test: rawResult exceeds maxValue should cap at 100
        BigDecimal result = gradeCalculator.calculateRatioGrade(
                BigDecimal.valueOf(25),
                BigDecimal.valueOf(20)
        );
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(result));
    }
    
    @Test
    void testRatioGrade_ZeroRawResult() {
        // Test: zero rawResult should return 0
        BigDecimal result = gradeCalculator.calculateRatioGrade(
                BigDecimal.ZERO,
                BigDecimal.valueOf(20)
        );
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }
    
    @Test
    void testRatioGrade_DecimalValues() {
        // Test: non-integer values (15.5 / 20 = 77.5%)
        BigDecimal result = gradeCalculator.calculateRatioGrade(
                BigDecimal.valueOf(15.5),
                BigDecimal.valueOf(20)
        );
        assertEquals(0, BigDecimal.valueOf(77.50).compareTo(result));
    }
    
    @Test
    void testRatioGrade_RoundingDown() {
        // Test: rounding to 2 decimal places (10 / 3 = 33.333... → 33.33)
        BigDecimal result = gradeCalculator.calculateRatioGrade(
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(30)
        );
        assertEquals(0, BigDecimal.valueOf(33.33).compareTo(result));
    }
    
    @Test
    void testRatioGrade_RoundingUp() {
        // Test: rounding to 2 decimal places (10 / 3 * 10 = 33.333... → 33.33)
        BigDecimal result = gradeCalculator.calculateRatioGrade(
                BigDecimal.valueOf(20),
                BigDecimal.valueOf(30)
        );
        assertEquals(0, BigDecimal.valueOf(66.67).compareTo(result));
    }
    
    @Test
    void testRatioGrade_NullRawResult() {
        // Test: null rawResult should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            gradeCalculator.calculateRatioGrade(null, BigDecimal.valueOf(20));
        });
    }
    
    @Test
    void testRatioGrade_NullMaxValue() {
        // Test: null maxValue should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            gradeCalculator.calculateRatioGrade(BigDecimal.valueOf(10), null);
        });
    }
    
    @Test
    void testRatioGrade_NegativeRawResult() {
        // Test: negative rawResult should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            gradeCalculator.calculateRatioGrade(BigDecimal.valueOf(-5), BigDecimal.valueOf(20));
        });
    }
    
    @Test
    void testRatioGrade_ZeroMaxValue() {
        // Test: zero maxValue should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            gradeCalculator.calculateRatioGrade(BigDecimal.valueOf(10), BigDecimal.ZERO);
        });
    }
    
    // PENALTY Calculation Tests
    
    @Test
    void testPenaltyGrade_ExactTarget() {
        // Test: rawResult equals targetValue should return 100
        BigDecimal result = gradeCalculator.calculatePenaltyGrade(
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(5)
        );
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(result));
    }
    
    @Test
    void testPenaltyGrade_BetterThanTarget() {
        // Test: rawResult better than target should cap at 100
        BigDecimal result = gradeCalculator.calculatePenaltyGrade(
                BigDecimal.valueOf(8),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(5)
        );
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(result));
    }
    
    @Test
    void testPenaltyGrade_OneUnitOver() {
        // Test: 1 unit over target with penalty 5 = 95
        BigDecimal result = gradeCalculator.calculatePenaltyGrade(
                BigDecimal.valueOf(11),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(5)
        );
        assertEquals(0, BigDecimal.valueOf(95.00).compareTo(result));
    }
    
    @Test
    void testPenaltyGrade_TwoUnitsOver() {
        // Test: 2 units over target with penalty 5 = 90
        BigDecimal result = gradeCalculator.calculatePenaltyGrade(
                BigDecimal.valueOf(12),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(5)
        );
        assertEquals(0, BigDecimal.valueOf(90.00).compareTo(result));
    }
    
    @Test
    void testPenaltyGrade_ExceedsPenalty() {
        // Test: penalty exceeds 100 should cap at 0
        BigDecimal result = gradeCalculator.calculatePenaltyGrade(
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(5)
        );
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }
    
    @Test
    void testPenaltyGrade_DecimalValues() {
        // Test: non-integer values (10.5 - 10) * 5 = 2.5, grade = 97.5
        BigDecimal result = gradeCalculator.calculatePenaltyGrade(
                BigDecimal.valueOf(10.5),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(5)
        );
        assertEquals(0, BigDecimal.valueOf(97.50).compareTo(result));
    }
    
    @Test
    void testPenaltyGrade_DecimalPenalty() {
        // Test: decimal penalty (11 - 10) * 2.5 = 2.5, grade = 97.5
        BigDecimal result = gradeCalculator.calculatePenaltyGrade(
                BigDecimal.valueOf(11),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(2.5)
        );
        assertEquals(0, BigDecimal.valueOf(97.50).compareTo(result));
    }
    
    @Test
    void testPenaltyGrade_Linearity() {
        // Test: doubling deviation doubles penalty
        BigDecimal grade1 = gradeCalculator.calculatePenaltyGrade(
                BigDecimal.valueOf(11),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(5)
        );
        BigDecimal grade2 = gradeCalculator.calculatePenaltyGrade(
                BigDecimal.valueOf(12),
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(5)
        );
        
        // grade1 = 95, grade2 = 90, difference should be 5
        BigDecimal difference = grade1.subtract(grade2);
        assertEquals(0, BigDecimal.valueOf(5.00).compareTo(difference));
    }
    
    @Test
    void testPenaltyGrade_NullRawResult() {
        // Test: null rawResult should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            gradeCalculator.calculatePenaltyGrade(null, BigDecimal.valueOf(10), BigDecimal.valueOf(5));
        });
    }
    
    @Test
    void testPenaltyGrade_NullTargetValue() {
        // Test: null targetValue should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            gradeCalculator.calculatePenaltyGrade(BigDecimal.valueOf(10), null, BigDecimal.valueOf(5));
        });
    }
    
    @Test
    void testPenaltyGrade_NullPenaltyPerUnit() {
        // Test: null penaltyPerUnit should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            gradeCalculator.calculatePenaltyGrade(BigDecimal.valueOf(10), BigDecimal.valueOf(10), null);
        });
    }
    
    @Test
    void testPenaltyGrade_NegativeRawResult() {
        // Test: negative rawResult should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            gradeCalculator.calculatePenaltyGrade(BigDecimal.valueOf(-5), BigDecimal.valueOf(10), BigDecimal.valueOf(5));
        });
    }
    
    // Integration Tests with Test Entity
    
    @Test
    void testCalculateGrade_RatioTest() {
        // Create RATIO test
        com.pe.grademanagement.entity.Test test = new com.pe.grademanagement.entity.Test();
        test.setCalculationType(CalculationType.RATIO);
        test.setUnitType(UnitType.COUNT);
        test.setMaxValue(BigDecimal.valueOf(20));
        
        // Calculate grade
        BigDecimal result = gradeCalculator.calculateGrade(BigDecimal.valueOf(15), test);
        assertEquals(0, BigDecimal.valueOf(75.00).compareTo(result));
    }
    
    @Test
    void testCalculateGrade_PenaltyTest() {
        // Create PENALTY test
        com.pe.grademanagement.entity.Test test = new com.pe.grademanagement.entity.Test();
        test.setCalculationType(CalculationType.PENALTY);
        test.setUnitType(UnitType.TIME);
        test.setTargetValue(BigDecimal.valueOf(10));
        test.setPenaltyPerUnit(BigDecimal.valueOf(5));
        
        // Calculate grade
        BigDecimal result = gradeCalculator.calculateGrade(BigDecimal.valueOf(11), test);
        assertEquals(0, BigDecimal.valueOf(95.00).compareTo(result));
    }
    
    @Test
    void testCalculateGrade_NullRawResult() {
        // Create test
        com.pe.grademanagement.entity.Test test = new com.pe.grademanagement.entity.Test();
        test.setCalculationType(CalculationType.RATIO);
        test.setMaxValue(BigDecimal.valueOf(20));
        
        // Calculate grade with null rawResult should return 0
        BigDecimal result = gradeCalculator.calculateGrade(null, test);
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }
    
    @Test
    void testCalculateGrade_ZeroRawResult() {
        // Create test
        com.pe.grademanagement.entity.Test test = new com.pe.grademanagement.entity.Test();
        test.setCalculationType(CalculationType.RATIO);
        test.setMaxValue(BigDecimal.valueOf(20));
        
        // Calculate grade with zero rawResult should return 0
        BigDecimal result = gradeCalculator.calculateGrade(BigDecimal.ZERO, test);
        assertEquals(0, BigDecimal.ZERO.compareTo(result));
    }
    
    @Test
    void testCalculateGrade_NullTest() {
        // Test: null test should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            gradeCalculator.calculateGrade(BigDecimal.valueOf(10), null);
        });
    }
}
