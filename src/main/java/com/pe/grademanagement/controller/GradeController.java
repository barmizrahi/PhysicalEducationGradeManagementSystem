package com.pe.grademanagement.controller;

import com.pe.grademanagement.dto.BulkTestResultRequest;
import com.pe.grademanagement.dto.TestResultRequest;
import com.pe.grademanagement.entity.Student;
import com.pe.grademanagement.entity.Test;
import com.pe.grademanagement.entity.TestResult;
import com.pe.grademanagement.repository.StudentRepository;
import com.pe.grademanagement.repository.TestRepository;
import com.pe.grademanagement.service.GradeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for grade entry endpoints.
 * Handles test result storage and retrieval.
 * 
 * Requirements:
 * - 6.1, 6.2, 6.3, 6.4, 6.5: Grade entry interface support
 * - 7.1, 7.2: Test result storage and updates
 */
@RestController
@RequestMapping("/api/grades")
@CrossOrigin
public class GradeController {
    
    private final GradeService gradeService;
    private final StudentRepository studentRepository;
    private final TestRepository testRepository;
    
    @Autowired
    public GradeController(GradeService gradeService,
                          StudentRepository studentRepository,
                          TestRepository testRepository) {
        this.gradeService = gradeService;
        this.studentRepository = studentRepository;
        this.testRepository = testRepository;
    }
    
    /**
     * Get test results for a specific class and test.
     * Requires authentication - only returns results for classes taught by the authenticated teacher.
     * 
     * @param classId Class ID
     * @param testId Test ID
     * @param authentication Spring Security authentication object
     * @return List of test results for the class and test
     */
    @GetMapping("/class/{classId}/test/{testId}")
    public ResponseEntity<?> getTestResultsForClass(@PathVariable Long classId,
                                                    @PathVariable Long testId,
                                                    Authentication authentication) {
        try {
            // Authorization is handled by GradeService
            List<TestResult> results = gradeService.getTestResultsForClass(classId, testId);
            
            return ResponseEntity.ok(results);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid request: " + e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to retrieve test results: " + e.getMessage()));
        }
    }
    
    /**
     * Save a single test result.
     * Requires authentication - only allows saving results for students in the teacher's classes.
     * Automatically calculates grade from raw result.
     * 
     * @param resultRequest Test result data
     * @param authentication Spring Security authentication object
     * @return Saved test result with calculated grade and HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<?> saveTestResult(@Valid @RequestBody TestResultRequest resultRequest,
                                           Authentication authentication) {
        try {
            // Create TestResult entity from request
            TestResult testResult = createTestResultFromRequest(resultRequest);
            
            // Save test result (authorization handled by GradeService)
            TestResult savedResult = gradeService.saveTestResult(testResult);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedResult);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid test result: " + e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to save test result: " + e.getMessage()));
        }
    }
    
    /**
     * Bulk save test results.
     * Requires authentication - only allows saving results for students in the teacher's classes.
     * Automatically calculates grades from raw results.
     * 
     * @param bulkRequest List of test results to save
     * @param authentication Spring Security authentication object
     * @return List of saved test results with calculated grades and HTTP 201 status
     */
    @PostMapping("/bulk")
    public ResponseEntity<?> bulkSaveTestResults(@Valid @RequestBody BulkTestResultRequest bulkRequest,
                                                 Authentication authentication) {
        try {
            // Create TestResult entities from requests
            List<TestResult> testResults = new ArrayList<>();
            for (TestResultRequest resultRequest : bulkRequest.getResults()) {
                testResults.add(createTestResultFromRequest(resultRequest));
            }
            
            // Bulk save test results (authorization handled by GradeService)
            List<TestResult> savedResults = gradeService.bulkSaveTestResults(testResults);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedResults);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid test results: " + e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to save test results: " + e.getMessage()));
        }
    }
    
    /**
     * Create a TestResult entity from a TestResultRequest DTO.
     * Loads the Student and Test entities from the database.
     * 
     * @param request Test result request DTO
     * @return TestResult entity
     * @throws IllegalArgumentException if student or test not found
     */
    private TestResult createTestResultFromRequest(TestResultRequest request) {
        // Load student
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Student not found with ID: " + request.getStudentId()));
        
        // Load test
        Test test = testRepository.findById(request.getTestId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Test not found with ID: " + request.getTestId()));
        
        // Create TestResult entity
        TestResult testResult = new TestResult();
        testResult.setStudent(student);
        testResult.setTest(test);
        testResult.setRawResult(request.getRawResult());
        testResult.setNotes(request.getNotes());
        
        return testResult;
    }
    
    /**
     * Simple error response DTO.
     */
    private static class ErrorResponse {
        private final String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() {
            return error;
        }
    }
}
