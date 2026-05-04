package com.pe.grademanagement.controller;

import com.pe.grademanagement.entity.Class;
import com.pe.grademanagement.entity.Student;
import com.pe.grademanagement.repository.ClassRepository;
import com.pe.grademanagement.repository.TeacherRepository;
import com.pe.grademanagement.service.StudentService;
import com.pe.grademanagement.util.ExcelImporter;
import com.pe.grademanagement.util.InvalidExcelFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * REST controller for student management endpoints.
 * Handles student data retrieval, organization, and import.
 * 
 * Requirements:
 * - 2.1, 2.2, 2.3, 2.4: Provide endpoints for student data access
 * - 13.1: Require authentication for all student data endpoints
 * - 13.4: Return clear Hebrew error messages when import format is incorrect
 * - 13.5: Validate that file has exactly 4 columns before processing
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {
    
    private final StudentService studentService;
    private final ExcelImporter excelImporter;
    private final ClassRepository classRepository;
    private final TeacherRepository teacherRepository;
    
    @Autowired
    public StudentController(StudentService studentService, 
                           ExcelImporter excelImporter,
                           ClassRepository classRepository,
                           TeacherRepository teacherRepository) {
        this.studentService = studentService;
        this.excelImporter = excelImporter;
        this.classRepository = classRepository;
        this.teacherRepository = teacherRepository;
    }
    
    /**
     * Import students from Excel file with fixed 4-column format.
     * Expected columns: studentId, name, gradeLevel, className
     * 
     * Requirements:
     * - 13.1: Expect exactly 4 columns in fixed order
     * - 13.4: Return clear Hebrew error messages when format is incorrect
     * - 13.5: Validate that file has exactly 4 columns before processing
     * - 9.1: All error messages must be displayed in Hebrew
     * 
     * @param file Excel file containing student data
     * @param authentication Spring Security authentication object
     * @return ImportResult with counts of created/updated students and any errors
     */
    @PostMapping("/import")
    public ResponseEntity<?> importStudents(@RequestParam("file") MultipartFile file,
                                           Authentication authentication) {
        try {
            // Get authenticated teacher's email
            String teacherEmail = authentication.getName();
            var teacher = teacherRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("מורה לא נמצא"));
            
            // Parse Excel file using fixed format
            List<ExcelImporter.StudentData> studentDataList = excelImporter.importStudents(file);
            
            int studentsCreated = 0;
            int studentsUpdated = 0;
            List<String> errors = new ArrayList<>();
            
            // Process each student
            for (ExcelImporter.StudentData studentData : studentDataList) {
                try {
                    // Find or create class
                    Class classEntity = classRepository
                        .findByNameAndGradeLevelAndTeacherId(
                            studentData.getClassName(), 
                            studentData.getGradeLevel(), 
                            teacher.getId())
                        .orElseGet(() -> {
                            // Create new class if it doesn't exist
                            Class newClass = new Class();
                            newClass.setName(studentData.getClassName());
                            newClass.setGradeLevel(studentData.getGradeLevel());
                            newClass.setTeacher(teacher);
                            return classRepository.save(newClass);
                        });
                    
                    // Check if student already exists
                    Optional<Student> existingStudent = studentService.findExistingStudent(
                        studentData.getStudentId(),
                        studentData.getName(),
                        classEntity.getId()
                    );
                    
                    if (existingStudent.isPresent()) {
                        // Update existing student
                        Student student = existingStudent.get();
                        student.setName(studentData.getName());
                        student.setStudentId(studentData.getStudentId());
                        student.setGradeLevel(studentData.getGradeLevel());
                        student.setClassEntity(classEntity);
                        studentService.saveStudent(student);
                        studentsUpdated++;
                    } else {
                        // Create new student
                        Student student = new Student();
                        student.setName(studentData.getName());
                        student.setStudentId(studentData.getStudentId());
                        student.setGradeLevel(studentData.getGradeLevel());
                        student.setClassEntity(classEntity);
                        studentService.saveStudent(student);
                        studentsCreated++;
                    }
                    
                } catch (Exception e) {
                    errors.add("שגיאה בעיבוד תלמיד " + studentData.getName() + ": " + e.getMessage());
                }
            }
            
            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("studentsCreated", studentsCreated);
            response.put("studentsUpdated", studentsUpdated);
            if (!errors.isEmpty()) {
                response.put("errors", errors);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (InvalidExcelFormatException e) {
            // Return Hebrew error message from ExcelImporter
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            
        } catch (Exception e) {
            // Generic error with Hebrew message
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "שגיאה בייבוא קובץ: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
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
        try {
            // Get authenticated teacher's email
            String teacherEmail = authentication.getName();
            System.out.println("DEBUG: Teacher email: " + teacherEmail);
            
            var teacher = teacherRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("מורה לא נמצא"));
            System.out.println("DEBUG: Teacher ID: " + teacher.getId() + ", Name: " + teacher.getFullName());
            
            // Get all classes for this teacher
            List<Class> classes = classRepository.findByTeacherId(teacher.getId());
            System.out.println("DEBUG: Found " + classes.size() + " classes for teacher");
            
            // Group students by grade level and class name
            Map<String, Map<String, List<Map<String, Object>>>> result = new HashMap<>();
            
            for (Class classEntity : classes) {
                String gradeLevel = classEntity.getGradeLevel();
                String className = classEntity.getName();
                System.out.println("DEBUG: Processing class: " + className + " (Grade: " + gradeLevel + ", ID: " + classEntity.getId() + ")");
                
                // Get students for this class
                List<Student> students = studentService.getStudentsByClass(classEntity.getId());
                System.out.println("DEBUG: Found " + students.size() + " students in class " + className);
                
                // Convert students to simple DTOs to avoid circular references
                // Match the frontend Student interface structure
                List<Map<String, Object>> studentDTOs = students.stream()
                    .map(student -> {
                        Map<String, Object> dto = new HashMap<>();
                        dto.put("id", student.getId());
                        dto.put("name", student.getName());
                        dto.put("studentId", student.getStudentId());
                        dto.put("gradeLevel", student.getGradeLevel());
                        dto.put("classId", classEntity.getId());
                        dto.put("className", className);
                        dto.put("createdAt", student.getCreatedAt() != null ? student.getCreatedAt().toString() : null);
                        dto.put("updatedAt", student.getUpdatedAt() != null ? student.getUpdatedAt().toString() : null);
                        return dto;
                    })
                    .collect(java.util.stream.Collectors.toList());
                
                // Initialize grade level map if needed
                result.putIfAbsent(gradeLevel, new HashMap<>());
                
                // Add students to the appropriate grade level and class
                result.get(gradeLevel).put(className, studentDTOs);
            }
            
            System.out.println("DEBUG: Returning data with " + result.size() + " grade levels");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "שגיאה בטעינת רשימת תלמידים: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
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
        try {
            // Get authenticated teacher's email
            String teacherEmail = authentication.getName();
            var teacher = teacherRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("מורה לא נמצא"));
            
            // Verify the class belongs to this teacher
            Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("כיתה לא נמצאה"));
            
            if (!classEntity.getTeacher().getId().equals(teacher.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "אין הרשאה לגשת לכיתה זו"));
            }
            
            // Get students for this class
            List<Student> students = studentService.getStudentsByClass(classId);
            
            // Convert to DTOs to avoid circular references
            List<Map<String, Object>> studentDTOs = students.stream()
                .map(student -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", student.getId());
                    dto.put("name", student.getName());
                    dto.put("studentId", student.getStudentId());
                    dto.put("gradeLevel", student.getGradeLevel());
                    dto.put("classId", classEntity.getId());
                    dto.put("className", classEntity.getName());
                    dto.put("createdAt", student.getCreatedAt() != null ? student.getCreatedAt().toString() : null);
                    dto.put("updatedAt", student.getUpdatedAt() != null ? student.getUpdatedAt().toString() : null);
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(studentDTOs);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "שגיאה בטעינת תלמידים: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Delete a single student.
     * Requires authentication - only deletes if the student belongs to the authenticated teacher's class.
     * Cascade deletes all test results for this student.
     * 
     * @param studentId student ID
     * @param authentication Spring Security authentication object
     * @return Success message
     */
    @DeleteMapping("/{studentId}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long studentId, Authentication authentication) {
        try {
            // Get authenticated teacher's email
            String teacherEmail = authentication.getName();
            var teacher = teacherRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("מורה לא נמצא"));
            
            // Delete student (service verifies access)
            studentService.deleteStudent(studentId);
            
            return ResponseEntity.ok(Map.of("message", "תלמיד נמחק בהצלחה"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "שגיאה במחיקת תלמיד: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Delete an entire class and all its students.
     * Requires authentication - only deletes if the class belongs to the authenticated teacher.
     * Cascade deletes all students and their test results.
     * 
     * @param classId class ID
     * @param authentication Spring Security authentication object
     * @return Success message with count of deleted students
     */
    @DeleteMapping("/class/{classId}")
    public ResponseEntity<?> deleteClass(@PathVariable Long classId, Authentication authentication) {
        try {
            // Get authenticated teacher's email
            String teacherEmail = authentication.getName();
            var teacher = teacherRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("מורה לא נמצא"));
            
            // Verify the class belongs to this teacher
            Class classEntity = classRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("כיתה לא נמצאה"));
            
            if (!classEntity.getTeacher().getId().equals(teacher.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "אין הרשאה למחוק כיתה זו"));
            }
            
            // Count students before deletion
            long studentCount = studentService.countStudentsByClass(classId);
            
            // Delete the class (cascade deletes students and their test results)
            classRepository.deleteById(classId);
            
            return ResponseEntity.ok(Map.of(
                "message", "כיתה נמחקה בהצלחה",
                "studentsDeleted", studentCount
            ));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "שגיאה במחיקת כיתה: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
