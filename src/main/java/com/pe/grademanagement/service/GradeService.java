package com.pe.grademanagement.service;

import com.pe.grademanagement.entity.Student;
import com.pe.grademanagement.entity.Teacher;
import com.pe.grademanagement.entity.Test;
import com.pe.grademanagement.entity.TestResult;
import com.pe.grademanagement.repository.StudentRepository;
import com.pe.grademanagement.repository.TeacherRepository;
import com.pe.grademanagement.repository.TestRepository;
import com.pe.grademanagement.repository.TestResultRepository;
import com.pe.grademanagement.util.GradeCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing test results and grade storage.
 * 
 * Key Responsibilities:
 * - Save or update test results with automatic grade calculation
 * - Retrieve test results for classes and tests
 * - Support bulk save operations for class-wide grade entry
 * - Handle null raw results (students who didn't take the test)
 * - Maintain creation and modification timestamps
 * 
 * Requirements:
 * - 6.3: Automatically calculate and display grades when raw results are entered
 * - 6.5: Support bulk save of all entered results
 * - 7.1: Store student ID, test ID, raw result, calculated grade, and notes
 * - 7.2: Allow updating of existing test results
 * - 7.3: Recalculate grade when test result is updated
 * - 7.4: Preserve timestamps for creation and modification
 * - 8.1: Assign grade of 0 when student has no test result
 * - 8.2: Allow entry of notes without raw result
 * - 8.4: Store rawResult as null and calculatedGrade as 0 for students without results
 */
@Service
@Transactional
public class GradeService {
    
    private final TestResultRepository testResultRepository;
    private final StudentRepository studentRepository;
    private final TestRepository testRepository;
    private final TeacherRepository teacherRepository;
    private final GradeCalculator gradeCalculator;
    
    @Autowired
    public GradeService(TestResultRepository testResultRepository,
                       StudentRepository studentRepository,
                       TestRepository testRepository,
                       TeacherRepository teacherRepository,
                       GradeCalculator gradeCalculator) {
        this.testResultRepository = testResultRepository;
        this.studentRepository = studentRepository;
        this.testRepository = testRepository;
        this.teacherRepository = teacherRepository;
        this.gradeCalculator = gradeCalculator;
    }
    
    /**
     * Get the authenticated teacher from SecurityContext.
     * 
     * @return Authenticated Teacher entity
     * @throws AccessDeniedException if no authentication or teacher not found
     */
    private Teacher getAuthenticatedTeacher() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated user found");
        }
        
        String email = authentication.getName();
        
        return teacherRepository.findByEmail(email)
            .orElseThrow(() -> new AccessDeniedException("Teacher not found for email: " + email));
    }
    
    /**
     * Verify that the authenticated teacher has access to the specified student.
     * Access is granted if the student belongs to a class taught by the teacher.
     * 
     * @param studentId Student ID to check
     * @throws AccessDeniedException if teacher doesn't have access to the student
     */
    private void verifyStudentAccess(Long studentId) {
        Teacher teacher = getAuthenticatedTeacher();
        
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));
        
        if (!student.getClassEntity().getTeacher().getId().equals(teacher.getId())) {
            throw new AccessDeniedException("Teacher does not have access to student with ID: " + studentId);
        }
    }
    
    /**
     * Verify that the authenticated teacher has access to the specified test.
     * Access is granted if the teacher created the test.
     * 
     * @param testId Test ID to check
     * @throws AccessDeniedException if teacher doesn't have access to the test
     */
    private void verifyTestAccess(Long testId) {
        Teacher teacher = getAuthenticatedTeacher();
        
        Test test = testRepository.findById(testId)
            .orElseThrow(() -> new IllegalArgumentException("Test not found with ID: " + testId));
        
        if (!test.getCreatedBy().getId().equals(teacher.getId())) {
            throw new AccessDeniedException("Teacher does not have access to test with ID: " + testId);
        }
    }
    
    /**
     * Save or update test result with automatic grade calculation.
     * Verifies that the authenticated teacher has access to both the student and test.
     * 
     * This method:
     * - Validates that student and test exist
     * - Verifies teacher has access to the student and test
     * - Automatically calculates grade from raw result using GradeCalculator
     * - Handles null rawResult by setting calculatedGrade to 0
     * - Updates existing result if one exists for the student/test combination
     * - Creates new result if none exists
     * - Stores timestamps for creation and modification
     * 
     * @param result Test result with raw value and optional notes
     * @return Saved TestResult with calculated grade and timestamps
     * @throws IllegalArgumentException if result, student, or test is invalid
     * @throws AccessDeniedException if teacher doesn't have access to student or test
     */
    public TestResult saveTestResult(TestResult result) {
        // Validate input
        if (result == null) {
            throw new IllegalArgumentException("Test result cannot be null");
        }
        
        if (result.getStudent() == null || result.getStudent().getId() == null) {
            throw new IllegalArgumentException("Student is required for test result");
        }
        
        if (result.getTest() == null || result.getTest().getId() == null) {
            throw new IllegalArgumentException("Test is required for test result");
        }
        
        // Verify student exists
        Long studentId = result.getStudent().getId();
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (studentOpt.isEmpty()) {
            throw new IllegalArgumentException("Student not found with ID: " + studentId);
        }
        Student student = studentOpt.get();
        
        // Verify test exists
        Long testId = result.getTest().getId();
        Optional<Test> testOpt = testRepository.findById(testId);
        if (testOpt.isEmpty()) {
            throw new IllegalArgumentException("Test not found with ID: " + testId);
        }
        Test test = testOpt.get();
        
        // Verify teacher has access to the student and test
        verifyStudentAccess(studentId);
        verifyTestAccess(testId);
        
        // Calculate grade from raw result
        BigDecimal calculatedGrade = gradeCalculator.calculateGrade(result.getRawResult(), test);
        result.setCalculatedGrade(calculatedGrade);
        
        // Check if result already exists for this student/test combination
        Optional<TestResult> existingResultOpt = testResultRepository.findByStudentIdAndTestId(studentId, testId);
        
        if (existingResultOpt.isPresent()) {
            // Update existing result
            TestResult existingResult = existingResultOpt.get();
            existingResult.setRawResult(result.getRawResult());
            existingResult.setCalculatedGrade(calculatedGrade);
            existingResult.setNotes(result.getNotes());
            // updatedAt timestamp will be set automatically by @PreUpdate
            return testResultRepository.save(existingResult);
        } else {
            // Create new result
            result.setStudent(student);
            result.setTest(test);
            // createdAt and updatedAt timestamps will be set automatically by @PrePersist
            return testResultRepository.save(result);
        }
    }
    
    /**
     * Get test results for a class and test.
     * Returns results for all students in the class who have taken the test.
     * Does NOT include students who haven't taken the test yet.
     * Verifies that the authenticated teacher has access to the test.
     * 
     * Use this method for displaying existing results.
     * For grade entry interface (which needs all students), use a combination
     * of this method and getStudentsByClass from StudentService.
     * 
     * @param classId Class ID
     * @param testId Test ID
     * @return List of test results for the class and test, ordered by student name
     * @throws IllegalArgumentException if classId or testId is null
     * @throws AccessDeniedException if teacher doesn't have access to the test
     */
    @Transactional(readOnly = true)
    public List<TestResult> getTestResultsForClass(Long classId, Long testId) {
        if (classId == null) {
            throw new IllegalArgumentException("Class ID cannot be null");
        }
        
        if (testId == null) {
            throw new IllegalArgumentException("Test ID cannot be null");
        }
        
        // Verify teacher has access to the test
        verifyTestAccess(testId);
        
        return testResultRepository.findByClassIdAndTestIdOrderByStudentName(classId, testId);
    }
    
    /**
     * Bulk save test results.
     * Saves multiple test results in a single transaction.
     * Each result is processed individually with automatic grade calculation.
     * 
     * This method is optimized for the grade entry interface where a teacher
     * enters results for an entire class at once.
     * 
     * @param results List of test results to save
     * @return List of saved results with calculated grades and timestamps
     * @throws IllegalArgumentException if results list is null or contains invalid results
     */
    public List<TestResult> bulkSaveTestResults(List<TestResult> results) {
        if (results == null) {
            throw new IllegalArgumentException("Results list cannot be null");
        }
        
        List<TestResult> savedResults = new ArrayList<>();
        
        for (TestResult result : results) {
            try {
                TestResult savedResult = saveTestResult(result);
                savedResults.add(savedResult);
            } catch (IllegalArgumentException e) {
                // Re-throw with context about which result failed
                throw new IllegalArgumentException(
                    "Failed to save result for student ID " + 
                    (result.getStudent() != null ? result.getStudent().getId() : "null") +
                    " and test ID " + 
                    (result.getTest() != null ? result.getTest().getId() : "null") +
                    ": " + e.getMessage(), e);
            }
        }
        
        return savedResults;
    }
    
    /**
     * Get a test result by ID.
     * Verifies that the authenticated teacher has access to the student and test.
     * 
     * @param resultId Test result ID
     * @return Optional containing the test result if found, empty otherwise
     * @throws IllegalArgumentException if resultId is null
     * @throws AccessDeniedException if teacher doesn't have access
     */
    @Transactional(readOnly = true)
    public Optional<TestResult> getTestResultById(Long resultId) {
        if (resultId == null) {
            throw new IllegalArgumentException("Result ID cannot be null");
        }
        
        Optional<TestResult> resultOpt = testResultRepository.findById(resultId);
        
        if (resultOpt.isPresent()) {
            TestResult result = resultOpt.get();
            // Verify teacher has access to the student and test
            verifyStudentAccess(result.getStudent().getId());
            verifyTestAccess(result.getTest().getId());
        }
        
        return resultOpt;
    }
    
    /**
     * Get test result for a specific student and test.
     * Verifies that the authenticated teacher has access to the student and test.
     * 
     * @param studentId Student ID
     * @param testId Test ID
     * @return Optional containing the test result if found, empty otherwise
     * @throws IllegalArgumentException if studentId or testId is null
     * @throws AccessDeniedException if teacher doesn't have access
     */
    @Transactional(readOnly = true)
    public Optional<TestResult> getTestResultByStudentAndTest(Long studentId, Long testId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        if (testId == null) {
            throw new IllegalArgumentException("Test ID cannot be null");
        }
        
        // Verify teacher has access to the student and test
        verifyStudentAccess(studentId);
        verifyTestAccess(testId);
        
        return testResultRepository.findByStudentIdAndTestId(studentId, testId);
    }
    
    /**
     * Get all test results for a specific student.
     * Verifies that the authenticated teacher has access to the student.
     * 
     * @param studentId Student ID
     * @return List of test results for the student
     * @throws IllegalArgumentException if studentId is null
     * @throws AccessDeniedException if teacher doesn't have access to the student
     */
    @Transactional(readOnly = true)
    public List<TestResult> getTestResultsByStudent(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        // Verify teacher has access to the student
        verifyStudentAccess(studentId);
        
        return testResultRepository.findByStudentId(studentId);
    }
    
    /**
     * Get all test results for a specific test.
     * Verifies that the authenticated teacher has access to the test.
     * 
     * @param testId Test ID
     * @return List of test results for the test
     * @throws IllegalArgumentException if testId is null
     * @throws AccessDeniedException if teacher doesn't have access to the test
     */
    @Transactional(readOnly = true)
    public List<TestResult> getTestResultsByTest(Long testId) {
        if (testId == null) {
            throw new IllegalArgumentException("Test ID cannot be null");
        }
        
        // Verify teacher has access to the test
        verifyTestAccess(testId);
        
        return testResultRepository.findByTestId(testId);
    }
    
    /**
     * Delete a test result by ID.
     * Verifies that the authenticated teacher has access to the student and test.
     * 
     * @param resultId Test result ID
     * @throws IllegalArgumentException if resultId is null or result not found
     * @throws AccessDeniedException if teacher doesn't have access
     */
    public void deleteTestResult(Long resultId) {
        if (resultId == null) {
            throw new IllegalArgumentException("Result ID cannot be null");
        }
        
        TestResult result = testResultRepository.findById(resultId)
            .orElseThrow(() -> new IllegalArgumentException("Test result not found with ID: " + resultId));
        
        // Verify teacher has access to the student and test
        verifyStudentAccess(result.getStudent().getId());
        verifyTestAccess(result.getTest().getId());
        
        testResultRepository.deleteById(resultId);
    }
    
    /**
     * Delete all test results for a specific student.
     * Used when deleting a student.
     * Verifies that the authenticated teacher has access to the student.
     * 
     * @param studentId Student ID
     * @throws IllegalArgumentException if studentId is null
     * @throws AccessDeniedException if teacher doesn't have access to the student
     */
    public void deleteTestResultsByStudent(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        // Verify teacher has access to the student
        verifyStudentAccess(studentId);
        
        testResultRepository.deleteByStudentId(studentId);
    }
    
    /**
     * Delete all test results for a specific test.
     * Used when deleting a test.
     * Verifies that the authenticated teacher has access to the test.
     * 
     * @param testId Test ID
     * @throws IllegalArgumentException if testId is null
     * @throws AccessDeniedException if teacher doesn't have access to the test
     */
    public void deleteTestResultsByTest(Long testId) {
        if (testId == null) {
            throw new IllegalArgumentException("Test ID cannot be null");
        }
        
        // Verify teacher has access to the test
        verifyTestAccess(testId);
        
        testResultRepository.deleteByTestId(testId);
    }
    
    /**
     * Check if a test result exists for a specific student and test.
     * Verifies that the authenticated teacher has access to the student and test.
     * 
     * @param studentId Student ID
     * @param testId Test ID
     * @return true if a result exists, false otherwise
     * @throws IllegalArgumentException if studentId or testId is null
     * @throws AccessDeniedException if teacher doesn't have access
     */
    @Transactional(readOnly = true)
    public boolean existsTestResult(Long studentId, Long testId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        if (testId == null) {
            throw new IllegalArgumentException("Test ID cannot be null");
        }
        
        // Verify teacher has access to the student and test
        verifyStudentAccess(studentId);
        verifyTestAccess(testId);
        
        return testResultRepository.existsByStudentIdAndTestId(studentId, testId);
    }
    
    /**
     * Count test results for a specific test.
     * Verifies that the authenticated teacher has access to the test.
     * 
     * @param testId Test ID
     * @return Number of test results for the test
     * @throws IllegalArgumentException if testId is null
     * @throws AccessDeniedException if teacher doesn't have access to the test
     */
    @Transactional(readOnly = true)
    public long countTestResultsByTest(Long testId) {
        if (testId == null) {
            throw new IllegalArgumentException("Test ID cannot be null");
        }
        
        // Verify teacher has access to the test
        verifyTestAccess(testId);
        
        return testResultRepository.countByTestId(testId);
    }
    
    /**
     * Count test results with non-null raw results for a specific test.
     * This gives the number of students who actually took the test.
     * Verifies that the authenticated teacher has access to the test.
     * 
     * @param testId Test ID
     * @return Number of students who took the test
     * @throws IllegalArgumentException if testId is null
     * @throws AccessDeniedException if teacher doesn't have access to the test
     */
    @Transactional(readOnly = true)
    public long countCompletedTestResults(Long testId) {
        if (testId == null) {
            throw new IllegalArgumentException("Test ID cannot be null");
        }
        
        // Verify teacher has access to the test
        verifyTestAccess(testId);
        
        return testResultRepository.countByTestIdAndRawResultIsNotNull(testId);
    }
}
