package com.pe.grademanagement.service;

import com.pe.grademanagement.entity.CalculationType;
import com.pe.grademanagement.entity.Class;
import com.pe.grademanagement.entity.Test;
import com.pe.grademanagement.entity.TestAssignment;
import com.pe.grademanagement.entity.UnitType;
import com.pe.grademanagement.repository.ClassRepository;
import com.pe.grademanagement.repository.TestAssignmentRepository;
import com.pe.grademanagement.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing test configurations and assignments.
 * 
 * Requirements:
 * - 3.1: Create tests with name, calculation type, and unit type
 * - 3.2: Support RATIO and PENALTY calculation types
 * - 3.3: Support TIME and COUNT unit types
 * - 3.4: Require maxValue for RATIO tests
 * - 3.5: Require targetValue and penaltyPerUnit for PENALTY tests
 * - 3.6: Store test configurations in database
 * - 3.7: Allow editing of existing test configurations
 * - 15: Support test assignment to classes
 */
@Service
public class TestService {
    
    private final TestRepository testRepository;
    private final TestAssignmentRepository testAssignmentRepository;
    private final ClassRepository classRepository;
    
    @Autowired
    public TestService(TestRepository testRepository,
                      TestAssignmentRepository testAssignmentRepository,
                      ClassRepository classRepository) {
        this.testRepository = testRepository;
        this.testAssignmentRepository = testAssignmentRepository;
        this.classRepository = classRepository;
    }
    
    /**
     * Create a new test configuration.
     * Validates that the test configuration is complete based on calculation type.
     * 
     * @param test the test configuration to create
     * @return the created test entity
     * @throws IllegalArgumentException if test configuration is invalid
     */
    @Transactional
    public Test createTest(Test test) {
        validateTestConfiguration(test);
        return testRepository.save(test);
    }
    
    /**
     * Update an existing test configuration.
     * Validates that the updated configuration is complete based on calculation type.
     * 
     * @param testId the ID of the test to update
     * @param updatedTest the updated test configuration
     * @return the updated test entity
     * @throws IllegalArgumentException if test configuration is invalid or test not found
     */
    @Transactional
    public Test updateTest(Long testId, Test updatedTest) {
        Optional<Test> existingTestOpt = testRepository.findById(testId);
        if (existingTestOpt.isEmpty()) {
            throw new IllegalArgumentException("Test not found with ID: " + testId);
        }
        
        Test existingTest = existingTestOpt.get();
        
        // Update fields
        existingTest.setName(updatedTest.getName());
        existingTest.setCalculationType(updatedTest.getCalculationType());
        existingTest.setUnitType(updatedTest.getUnitType());
        existingTest.setMaxValue(updatedTest.getMaxValue());
        existingTest.setTargetValue(updatedTest.getTargetValue());
        existingTest.setPenaltyPerUnit(updatedTest.getPenaltyPerUnit());
        existingTest.setPenaltyUnit(updatedTest.getPenaltyUnit());
        
        // Validate updated configuration
        validateTestConfiguration(existingTest);
        
        return testRepository.save(existingTest);
    }
    
    /**
     * Assign a test to multiple classes.
     * Creates TestAssignment records for each class.
     * Skips classes that already have the test assigned.
     * 
     * @param testId the ID of the test to assign
     * @param classIds the list of class IDs to assign the test to
     * @throws IllegalArgumentException if test not found or any class not found
     */
    @Transactional
    public void assignTestToClasses(Long testId, List<Long> classIds) {
        // Verify test exists
        Optional<Test> testOpt = testRepository.findById(testId);
        if (testOpt.isEmpty()) {
            throw new IllegalArgumentException("Test not found with ID: " + testId);
        }
        Test test = testOpt.get();
        
        // Assign test to each class
        for (Long classId : classIds) {
            // Verify class exists
            Optional<Class> classOpt = classRepository.findById(classId);
            if (classOpt.isEmpty()) {
                throw new IllegalArgumentException("Class not found with ID: " + classId);
            }
            Class classEntity = classOpt.get();
            
            // Check if assignment already exists
            if (!testAssignmentRepository.existsByTestIdAndClassEntityId(testId, classId)) {
                TestAssignment assignment = new TestAssignment(test, classEntity);
                testAssignmentRepository.save(assignment);
            }
        }
    }
    
    /**
     * Get all tests assigned to a specific class.
     * 
     * @param classId the class ID
     * @return list of tests assigned to the class
     */
    @Transactional(readOnly = true)
    public List<Test> getTestsForClass(Long classId) {
        return testRepository.findByClassIdOrderByName(classId);
    }
    
    /**
     * Get all tests created by a specific teacher.
     * 
     * @param teacherId the teacher's ID
     * @return list of tests created by the teacher
     */
    @Transactional(readOnly = true)
    public List<Test> getTestsByTeacher(Long teacherId) {
        return testRepository.findByCreatedByIdOrderByName(teacherId);
    }
    
    /**
     * Get a test by ID.
     * 
     * @param testId the test ID
     * @return Optional containing the test if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<Test> getTestById(Long testId) {
        return testRepository.findById(testId);
    }
    
    /**
     * Delete a test and all its assignments.
     * 
     * @param testId the ID of the test to delete
     * @throws IllegalArgumentException if test not found
     */
    @Transactional
    public void deleteTest(Long testId) {
        if (!testRepository.existsById(testId)) {
            throw new IllegalArgumentException("Test not found with ID: " + testId);
        }
        testRepository.deleteById(testId);
    }
    
    /**
     * Assign a test to all classes in a specific grade level.
     * This is a convenience method for grade-level assignment.
     * 
     * @param testId the ID of the test to assign
     * @param gradeLevel the grade level (י, יא, יב)
     * @param teacherId the teacher's ID (for authorization)
     * @throws IllegalArgumentException if test not found
     */
    @Transactional
    public void assignTestToGradeLevel(Long testId, String gradeLevel, Long teacherId) {
        // Verify test exists
        if (!testRepository.existsById(testId)) {
            throw new IllegalArgumentException("Test not found with ID: " + testId);
        }
        
        // Get all classes for the teacher and grade level
        List<Class> classes = classRepository.findByTeacherIdAndGradeLevel(teacherId, gradeLevel);
        
        // Extract class IDs
        List<Long> classIds = classes.stream()
                .map(Class::getId)
                .toList();
        
        // Assign test to all classes
        if (!classIds.isEmpty()) {
            assignTestToClasses(testId, classIds);
        }
    }
    
    /**
     * Remove a test assignment from a class.
     * 
     * @param testId the test ID
     * @param classId the class ID
     */
    @Transactional
    public void removeTestAssignment(Long testId, Long classId) {
        Optional<TestAssignment> assignmentOpt = 
                testAssignmentRepository.findByTestIdAndClassEntityId(testId, classId);
        assignmentOpt.ifPresent(testAssignmentRepository::delete);
    }
    
    /**
     * Get all classes that a test is assigned to.
     * 
     * @param testId the test ID
     * @return list of classes the test is assigned to
     */
    @Transactional(readOnly = true)
    public List<Class> getClassesForTest(Long testId) {
        List<TestAssignment> assignments = testAssignmentRepository.findByTestId(testId);
        return assignments.stream()
                .map(TestAssignment::getClassEntity)
                .toList();
    }
    
    /**
     * Validate test configuration based on calculation type.
     * 
     * Requirements:
     * - 3.4: RATIO tests require maxValue
     * - 3.5: PENALTY tests require targetValue and penaltyPerUnit
     * 
     * @param test the test to validate
     * @throws IllegalArgumentException if configuration is invalid
     */
    private void validateTestConfiguration(Test test) {
        if (test == null) {
            throw new IllegalArgumentException("Test cannot be null");
        }
        
        if (test.getName() == null || test.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Test name is required");
        }
        
        if (test.getCalculationType() == null) {
            throw new IllegalArgumentException("Calculation type is required");
        }
        
        if (test.getUnitType() == null) {
            throw new IllegalArgumentException("Unit type is required");
        }
        
        if (test.getCreatedBy() == null) {
            throw new IllegalArgumentException("Test creator (teacher) is required");
        }
        
        // Validate RATIO configuration
        if (test.getCalculationType() == CalculationType.RATIO) {
            if (test.getMaxValue() == null) {
                throw new IllegalArgumentException("RATIO calculation requires maxValue parameter");
            }
            if (test.getMaxValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("maxValue must be greater than zero");
            }
        }
        
        // Validate PENALTY configuration
        if (test.getCalculationType() == CalculationType.PENALTY) {
            if (test.getTargetValue() == null) {
                throw new IllegalArgumentException("PENALTY calculation requires targetValue parameter");
            }
            if (test.getPenaltyPerUnit() == null) {
                throw new IllegalArgumentException("PENALTY calculation requires penaltyPerUnit parameter");
            }
            if (test.getTargetValue().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("targetValue must be greater than zero");
            }
            if (test.getPenaltyPerUnit().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("penaltyPerUnit must be greater than zero");
            }

            // For TIME unit type, penaltyUnit defines the deduction interval (e.g. 0.75 = every 45s).
            // Default to 1.0 (one minute) when missing for backward compatibility.
            if (test.getUnitType() == UnitType.TIME) {
                if (test.getPenaltyUnit() == null) {
                    test.setPenaltyUnit(BigDecimal.ONE);
                } else if (test.getPenaltyUnit().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("penaltyUnit must be greater than zero");
                }
            }
        }
    }
}
