package com.pe.grademanagement.service;

import com.pe.grademanagement.entity.Student;
import com.pe.grademanagement.entity.Teacher;
import com.pe.grademanagement.entity.Test;
import com.pe.grademanagement.entity.TestResult;
import com.pe.grademanagement.repository.StudentRepository;
import com.pe.grademanagement.repository.TeacherRepository;
import com.pe.grademanagement.repository.TestRepository;
import com.pe.grademanagement.repository.TestResultRepository;
import com.pe.grademanagement.util.ExcelExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for exporting grades to Excel format.
 * Handles data retrieval and authorization for export operations.
 * 
 * Requirements:
 * - 9.1, 9.2, 9.3, 9.4, 9.5, 9.6: Excel export functionality
 * - 8.3: Include students without test results
 */
@Service
@Transactional(readOnly = true)
public class ExportService {
    
    private final ExcelExporter excelExporter;
    private final StudentRepository studentRepository;
    private final TestRepository testRepository;
    private final TestResultRepository testResultRepository;
    private final TeacherRepository teacherRepository;
    
    @Autowired
    public ExportService(ExcelExporter excelExporter,
                        StudentRepository studentRepository,
                        TestRepository testRepository,
                        TestResultRepository testResultRepository,
                        TeacherRepository teacherRepository) {
        this.excelExporter = excelExporter;
        this.studentRepository = studentRepository;
        this.testRepository = testRepository;
        this.testResultRepository = testResultRepository;
        this.teacherRepository = teacherRepository;
    }
    
    /**
     * Export grades to Excel file.
     * Verifies that the authenticated teacher has access to all specified classes and tests.
     * 
     * @param classIds List of class IDs to include in export
     * @param testIds List of test IDs to include in export
     * @param includeNotes Whether to include notes column
     * @return Excel file as byte array
     * @throws IllegalArgumentException if classIds or testIds is null or empty
     * @throws AccessDeniedException if teacher doesn't have access to specified classes or tests
     */
    public byte[] exportGrades(List<Long> classIds, List<Long> testIds, boolean includeNotes) {
        if (classIds == null || classIds.isEmpty()) {
            throw new IllegalArgumentException("At least one class ID is required");
        }
        
        if (testIds == null || testIds.isEmpty()) {
            throw new IllegalArgumentException("At least one test ID is required");
        }
        
        // Get authenticated teacher
        Teacher teacher = getAuthenticatedTeacher();
        
        // Get all students from selected classes
        List<Student> students = new ArrayList<>();
        for (Long classId : classIds) {
            List<Student> classStudents = studentRepository.findByClassEntityIdOrderByName(classId);
            
            // Verify teacher has access to this class
            if (!classStudents.isEmpty()) {
                Student firstStudent = classStudents.get(0);
                if (!firstStudent.getClassEntity().getTeacher().getId().equals(teacher.getId())) {
                    throw new AccessDeniedException("You do not have access to class with ID: " + classId);
                }
            }
            
            students.addAll(classStudents);
        }
        
        // Get all tests
        List<Test> tests = new ArrayList<>();
        for (Long testId : testIds) {
            Test test = testRepository.findById(testId)
                    .orElseThrow(() -> new IllegalArgumentException("Test not found with ID: " + testId));
            
            // Verify teacher created this test
            if (!test.getCreatedBy().getId().equals(teacher.getId())) {
                throw new AccessDeniedException("You do not have access to test with ID: " + testId);
            }
            
            tests.add(test);
        }
        
        // Get all test results for the selected students and tests
        Map<Student, Map<Test, TestResult>> testResults = new HashMap<>();
        
        for (Student student : students) {
            Map<Test, TestResult> studentResults = new HashMap<>();
            
            for (Test test : tests) {
                Optional<TestResult> resultOpt = testResultRepository.findByStudentIdAndTestId(
                        student.getId(), test.getId());
                resultOpt.ifPresent(result -> studentResults.put(test, result));
            }
            
            testResults.put(student, studentResults);
        }
        
        // Generate Excel file
        return excelExporter.generateMinistryFormatExcel(students, testResults, includeNotes);
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
        
        String username = authentication.getName();
        
        return teacherRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Teacher not found for username: " + username));
    }
}
