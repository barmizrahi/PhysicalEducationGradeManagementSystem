package com.pe.grademanagement.controller;

import com.pe.grademanagement.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for student management endpoints.
 * Handles student data retrieval and organization.
 * 
 * Requirements:
 * - 2.1, 2.2, 2.3, 2.4: Provide endpoints for student data access
 * - 13.1: Require authentication for all student data endpoints
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {
    
    private final StudentService studentService;
    
    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }
    
    /**
     * Get students grouped by grade level and class.
     * Requires authentication - only returns students for the authenticated teacher's classes.
     * 
     * @param authentication Spring Security authentication object
     * @return Map of grade level → class → students
     */
    @GetMapping("/by-grade-and-class")
    public ResponseEntity<?> getStudentsByGradeAndClass(Authentication authentication) {
        // For now, return empty map - full implementation in Task 16.1
        // This endpoint is used to test authentication in integration tests
        return ResponseEntity.ok(Map.of());
    }
    
    /**
     * Get students in a specific class.
     * Requires authentication - only returns students if the class belongs to the authenticated teacher.
     * 
     * @param classId class ID
     * @param authentication Spring Security authentication object
     * @return List of students in the class
     */
    @GetMapping("/class/{classId}")
    public ResponseEntity<?> getStudentsByClass(@PathVariable Long classId, Authentication authentication) {
        // For now, return empty list - full implementation in Task 16.1
        // This endpoint is used to test authentication in integration tests
        return ResponseEntity.ok(List.of());
    }
}
