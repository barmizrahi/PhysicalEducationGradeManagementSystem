package com.pe.grademanagement.controller;

import com.pe.grademanagement.dto.TestAssignmentRequest;
import com.pe.grademanagement.dto.TestRequest;
import com.pe.grademanagement.entity.Teacher;
import com.pe.grademanagement.entity.Test;
import com.pe.grademanagement.repository.TeacherRepository;
import com.pe.grademanagement.service.TestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for test management endpoints.
 * Handles test configuration creation, updates, and assignment to classes.
 * 
 * Requirements:
 * - 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7: Test configuration management
 * - 15: Test assignment to classes
 */
@RestController
@RequestMapping("/api/tests")
@CrossOrigin
public class TestController {
    
    private final TestService testService;
    private final TeacherRepository teacherRepository;
    
    @Autowired
    public TestController(TestService testService, TeacherRepository teacherRepository) {
        this.testService = testService;
        this.teacherRepository = teacherRepository;
    }
    
    /**
     * Create a new test configuration.
     * Requires authentication - test is associated with the authenticated teacher.
     * 
     * @param testRequest Test configuration data
     * @param authentication Spring Security authentication object
     * @return Created test entity with HTTP 201 status
     */
    @PostMapping
    public ResponseEntity<?> createTest(@Valid @RequestBody TestRequest testRequest,
                                       Authentication authentication) {
        try {
            // Get authenticated teacher
            Teacher teacher = getAuthenticatedTeacher(authentication);
            
            // Create test entity from request
            Test test = new Test(
                testRequest.getName(),
                testRequest.getCalculationType(),
                testRequest.getUnitType(),
                teacher
            );
            test.setMaxValue(testRequest.getMaxValue());
            test.setTargetValue(testRequest.getTargetValue());
            test.setPenaltyPerUnit(testRequest.getPenaltyPerUnit());
            
            // Save test
            Test createdTest = testService.createTest(test);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTest);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid test configuration: " + e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to create test: " + e.getMessage()));
        }
    }
    
    /**
     * Update an existing test configuration.
     * Requires authentication - only the teacher who created the test can update it.
     * 
     * @param id Test ID
     * @param testRequest Updated test configuration data
     * @param authentication Spring Security authentication object
     * @return Updated test entity with HTTP 200 status
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTest(@PathVariable Long id,
                                       @Valid @RequestBody TestRequest testRequest,
                                       Authentication authentication) {
        try {
            // Get authenticated teacher
            Teacher teacher = getAuthenticatedTeacher(authentication);
            
            // Verify test exists and teacher has access
            Test existingTest = testService.getTestById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Test not found with ID: " + id));
            
            if (!existingTest.getCreatedBy().getId().equals(teacher.getId())) {
                throw new AccessDeniedException("You do not have permission to update this test");
            }
            
            // Create updated test entity from request
            Test updatedTest = new Test(
                testRequest.getName(),
                testRequest.getCalculationType(),
                testRequest.getUnitType(),
                teacher
            );
            updatedTest.setMaxValue(testRequest.getMaxValue());
            updatedTest.setTargetValue(testRequest.getTargetValue());
            updatedTest.setPenaltyPerUnit(testRequest.getPenaltyPerUnit());
            
            // Update test
            Test result = testService.updateTest(id, updatedTest);
            
            return ResponseEntity.ok(result);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid test configuration: " + e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to update test: " + e.getMessage()));
        }
    }
    
    /**
     * Assign a test to one or more classes.
     * Requires authentication - only the teacher who created the test can assign it.
     * 
     * @param id Test ID
     * @param assignmentRequest List of class IDs to assign the test to
     * @param authentication Spring Security authentication object
     * @return Success message with HTTP 200 status
     */
    @PostMapping("/{id}/assign")
    public ResponseEntity<?> assignTest(@PathVariable Long id,
                                       @Valid @RequestBody TestAssignmentRequest assignmentRequest,
                                       Authentication authentication) {
        try {
            // Get authenticated teacher
            Teacher teacher = getAuthenticatedTeacher(authentication);
            
            // Verify test exists and teacher has access
            Test test = testService.getTestById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Test not found with ID: " + id));
            
            if (!test.getCreatedBy().getId().equals(teacher.getId())) {
                throw new AccessDeniedException("You do not have permission to assign this test");
            }
            
            // Assign test to classes
            testService.assignTestToClasses(id, assignmentRequest.getClassIds());
            
            return ResponseEntity.ok(new MessageResponse("Test assigned successfully to " + 
                    assignmentRequest.getClassIds().size() + " class(es)"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid assignment: " + e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to assign test: " + e.getMessage()));
        }
    }
    
    /**
     * Get all tests assigned to a specific class.
     * Requires authentication - only returns tests for classes taught by the authenticated teacher.
     * 
     * @param classId Class ID
     * @param authentication Spring Security authentication object
     * @return List of tests assigned to the class
     */
    @GetMapping("/class/{classId}")
    public ResponseEntity<?> getTestsForClass(@PathVariable Long classId,
                                              Authentication authentication) {
        try {
            // Get authenticated teacher (for authorization)
            getAuthenticatedTeacher(authentication);
            
            // Get tests for class
            List<Test> tests = testService.getTestsForClass(classId);
            
            return ResponseEntity.ok(tests);
            
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to retrieve tests: " + e.getMessage()));
        }
    }
    
    /**
     * Get the authenticated teacher from SecurityContext.
     * 
     * @param authentication Spring Security authentication object
     * @return Authenticated Teacher entity
     * @throws AccessDeniedException if no authentication or teacher not found
     */
    private Teacher getAuthenticatedTeacher(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated user found");
        }
        
        String username = authentication.getName();
        
        return teacherRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Teacher not found for username: " + username));
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
    
    /**
     * Simple message response DTO.
     */
    private static class MessageResponse {
        private final String message;
        
        public MessageResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
