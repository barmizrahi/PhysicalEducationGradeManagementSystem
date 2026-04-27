package com.pe.grademanagement.service;

import com.pe.grademanagement.entity.Class;
import com.pe.grademanagement.entity.Student;
import com.pe.grademanagement.entity.Teacher;
import com.pe.grademanagement.repository.ClassRepository;
import com.pe.grademanagement.repository.StudentRepository;
import com.pe.grademanagement.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing student data with CRUD operations and duplicate detection.
 * 
 * Key Responsibilities:
 * - Create or update student records
 * - Detect duplicates by student ID (if available) or name+class combination
 * - Update existing records instead of creating duplicates
 * - Filter students by teacher authorization
 * - Group students by grade level and class
 * 
 * Requirements:
 * - 1.7: Detect existing students using student ID if available
 * - 1.8: Use name and class combination for duplicate detection when student ID is not available
 * - 1.9: Update existing student records instead of creating duplicates
 * - 2.1: Display students grouped by grade level
 * - 2.2: Display students grouped by class name within each grade level
 * - 2.3: Filter classes by grade level
 * - 2.4: Display all students in a selected class
 * - 13.3: Display only classes assigned to authenticated teacher
 */
@Service
@Transactional
public class StudentService {
    
    private final StudentRepository studentRepository;
    private final ClassRepository classRepository;
    private final TeacherRepository teacherRepository;
    
    @Autowired
    public StudentService(StudentRepository studentRepository, ClassRepository classRepository, TeacherRepository teacherRepository) {
        this.studentRepository = studentRepository;
        this.classRepository = classRepository;
        this.teacherRepository = teacherRepository;
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
    
    /**
     * Verify that the authenticated teacher has access to the specified class.
     * 
     * @param classId Class ID to check
     * @throws AccessDeniedException if teacher doesn't have access to the class
     */
    private void verifyClassAccess(Long classId) {
        Teacher teacher = getAuthenticatedTeacher();
        
        Class classEntity = classRepository.findById(classId)
            .orElseThrow(() -> new IllegalArgumentException("Class not found with ID: " + classId));
        
        if (!classEntity.getTeacher().getId().equals(teacher.getId())) {
            throw new AccessDeniedException("Teacher does not have access to class with ID: " + classId);
        }
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
     * Create or update student record.
     * Detects duplicates and updates existing records instead of creating new ones.
     * Verifies that the authenticated teacher has access to the student's class.
     * 
     * Duplicate detection logic:
     * - If student has a student ID, search by student ID
     * - If no student ID, search by name + class combination
     * - If existing student found, update it
     * - Otherwise, create new student
     * 
     * @param student Student data to save
     * @return Created or updated Student entity
     * @throws IllegalArgumentException if student data is invalid
     * @throws AccessDeniedException if teacher doesn't have access to the class
     */
    public Student saveStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("Student cannot be null");
        }
        
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Student name is required");
        }
        
        if (student.getClassEntity() == null) {
            throw new IllegalArgumentException("Student must be assigned to a class");
        }
        
        if (student.getGradeLevel() == null || student.getGradeLevel().trim().isEmpty()) {
            throw new IllegalArgumentException("Student grade level is required");
        }
        
        // Verify teacher has access to the class
        verifyClassAccess(student.getClassEntity().getId());
        
        // Find existing student for duplicate detection
        Optional<Student> existingStudent = findExistingStudent(
            student.getStudentId(),
            student.getName(),
            student.getClassEntity().getId()
        );
        
        if (existingStudent.isPresent()) {
            // Update existing student
            Student existing = existingStudent.get();
            existing.setName(student.getName());
            existing.setStudentId(student.getStudentId());
            existing.setGradeLevel(student.getGradeLevel());
            existing.setClassEntity(student.getClassEntity());
            return studentRepository.save(existing);
        } else {
            // Create new student
            return studentRepository.save(student);
        }
    }
    
    /**
     * Find existing student by ID or name+class.
     * Used for duplicate detection during student import and creation.
     * 
     * Detection strategy:
     * - If studentId is provided and not empty, search by student ID (primary method)
     * - If studentId is null or empty, search by name + class combination (fallback method)
     * 
     * @param studentId Optional student ID
     * @param name Student name
     * @param classId Class ID
     * @return Optional containing existing student if found, empty otherwise
     */
    public Optional<Student> findExistingStudent(String studentId, String name, Long classId) {
        // Primary detection: by student ID if available
        if (studentId != null && !studentId.trim().isEmpty()) {
            Optional<Student> byId = studentRepository.findByStudentId(studentId);
            if (byId.isPresent()) {
                return byId;
            }
        }
        
        // Fallback detection: by name + class combination
        if (name != null && !name.trim().isEmpty() && classId != null) {
            return studentRepository.findByNameAndClassEntityId(name, classId);
        }
        
        return Optional.empty();
    }
    
    /**
     * Get students grouped by grade level and class for the authenticated teacher.
     * Returns a nested map structure: gradeLevel → className → List<Student>
     * 
     * This method:
     * - Filters students to only those in classes assigned to the authenticated teacher
     * - Groups students first by grade level (י, יא, יב)
     * - Within each grade level, groups by class name
     * - Sorts students alphabetically within each class
     * 
     * @return Map of grade level → class name → students
     * @throws AccessDeniedException if no authenticated teacher found
     */
    public Map<String, Map<String, List<Student>>> getStudentsByGradeAndClass() {
        Teacher teacher = getAuthenticatedTeacher();
        
        // Fetch all students for this teacher, ordered by grade level, class, and name
        List<Student> students = studentRepository.findByTeacherIdOrderByGradeLevelAndClassAndName(teacher.getId());
        
        // Group by grade level, then by class name
        Map<String, Map<String, List<Student>>> groupedStudents = new LinkedHashMap<>();
        
        for (Student student : students) {
            String gradeLevel = student.getGradeLevel();
            String className = student.getClassEntity().getName();
            
            // Ensure grade level map exists
            groupedStudents.putIfAbsent(gradeLevel, new LinkedHashMap<>());
            Map<String, List<Student>> classMap = groupedStudents.get(gradeLevel);
            
            // Ensure class list exists
            classMap.putIfAbsent(className, new ArrayList<>());
            List<Student> studentList = classMap.get(className);
            
            // Add student to the list
            studentList.add(student);
        }
        
        return groupedStudents;
    }
    
    /**
     * Get students grouped by grade level and class for a specific teacher.
     * Returns a nested map structure: gradeLevel → className → List<Student>
     * 
     * This method:
     * - Filters students to only those in classes assigned to the specified teacher
     * - Groups students first by grade level (י, יא, יב)
     * - Within each grade level, groups by class name
     * - Sorts students alphabetically within each class
     * 
     * @param teacherId Teacher ID for authorization filtering
     * @return Map of grade level → class name → students
     * @deprecated Use getStudentsByGradeAndClass() instead, which uses authenticated teacher
     */
    @Deprecated
    public Map<String, Map<String, List<Student>>> getStudentsByGradeAndClass(Long teacherId) {
        if (teacherId == null) {
            throw new IllegalArgumentException("Teacher ID cannot be null");
        }
        
        // Fetch all students for this teacher, ordered by grade level, class, and name
        List<Student> students = studentRepository.findByTeacherIdOrderByGradeLevelAndClassAndName(teacherId);
        
        // Group by grade level, then by class name
        Map<String, Map<String, List<Student>>> groupedStudents = new LinkedHashMap<>();
        
        for (Student student : students) {
            String gradeLevel = student.getGradeLevel();
            String className = student.getClassEntity().getName();
            
            // Ensure grade level map exists
            groupedStudents.putIfAbsent(gradeLevel, new LinkedHashMap<>());
            Map<String, List<Student>> classMap = groupedStudents.get(gradeLevel);
            
            // Ensure class list exists
            classMap.putIfAbsent(className, new ArrayList<>());
            List<Student> studentList = classMap.get(className);
            
            // Add student to the list
            studentList.add(student);
        }
        
        return groupedStudents;
    }
    
    /**
     * Get all students in a specific class.
     * Filters by class ID and returns students ordered by name.
     * Verifies that the authenticated teacher has access to the class.
     * 
     * @param classId Class ID
     * @return List of students in the class, ordered by name
     * @throws AccessDeniedException if teacher doesn't have access to the class
     */
    public List<Student> getStudentsByClass(Long classId) {
        if (classId == null) {
            throw new IllegalArgumentException("Class ID cannot be null");
        }
        
        // Verify teacher has access to the class
        verifyClassAccess(classId);
        
        return studentRepository.findByClassEntityIdOrderByName(classId);
    }
    
    /**
     * Get all students for the authenticated teacher and grade level.
     * Used for filtering students by grade level within a teacher's classes.
     * 
     * @param gradeLevel Grade level (י, יא, יב)
     * @return List of students for the teacher and grade level
     * @throws AccessDeniedException if no authenticated teacher found
     */
    public List<Student> getStudentsByTeacherAndGradeLevel(String gradeLevel) {
        Teacher teacher = getAuthenticatedTeacher();
        
        if (gradeLevel == null || gradeLevel.trim().isEmpty()) {
            throw new IllegalArgumentException("Grade level cannot be null or empty");
        }
        
        return studentRepository.findByTeacherIdAndGradeLevel(teacher.getId(), gradeLevel);
    }
    
    /**
     * Get all students for a specific teacher and grade level.
     * Used for filtering students by grade level within a teacher's classes.
     * 
     * @param teacherId Teacher ID for authorization
     * @param gradeLevel Grade level (י, יא, יב)
     * @return List of students for the teacher and grade level
     * @deprecated Use getStudentsByTeacherAndGradeLevel(String gradeLevel) instead
     */
    @Deprecated
    public List<Student> getStudentsByTeacherAndGradeLevel(Long teacherId, String gradeLevel) {
        if (teacherId == null) {
            throw new IllegalArgumentException("Teacher ID cannot be null");
        }
        
        if (gradeLevel == null || gradeLevel.trim().isEmpty()) {
            throw new IllegalArgumentException("Grade level cannot be null or empty");
        }
        
        return studentRepository.findByTeacherIdAndGradeLevel(teacherId, gradeLevel);
    }
    
    /**
     * Get all classes for the authenticated teacher and grade level.
     * Used for displaying classes when a grade level is selected.
     * 
     * @param gradeLevel Grade level (י, יא, יב)
     * @return List of classes for the teacher and grade level
     * @throws AccessDeniedException if no authenticated teacher found
     */
    public List<Class> getClassesByTeacherAndGradeLevel(String gradeLevel) {
        Teacher teacher = getAuthenticatedTeacher();
        
        if (gradeLevel == null || gradeLevel.trim().isEmpty()) {
            throw new IllegalArgumentException("Grade level cannot be null or empty");
        }
        
        return classRepository.findByTeacherIdAndGradeLevel(teacher.getId(), gradeLevel);
    }
    
    /**
     * Get all classes for a specific teacher and grade level.
     * Used for displaying classes when a grade level is selected.
     * 
     * @param teacherId Teacher ID for authorization
     * @param gradeLevel Grade level (י, יא, יב)
     * @return List of classes for the teacher and grade level
     * @deprecated Use getClassesByTeacherAndGradeLevel(String gradeLevel) instead
     */
    @Deprecated
    public List<Class> getClassesByTeacherAndGradeLevel(Long teacherId, String gradeLevel) {
        if (teacherId == null) {
            throw new IllegalArgumentException("Teacher ID cannot be null");
        }
        
        if (gradeLevel == null || gradeLevel.trim().isEmpty()) {
            throw new IllegalArgumentException("Grade level cannot be null or empty");
        }
        
        return classRepository.findByTeacherIdAndGradeLevel(teacherId, gradeLevel);
    }
    
    /**
     * Get a student by ID.
     * Verifies that the authenticated teacher has access to the student.
     * 
     * @param studentId Student ID
     * @return Optional containing the student if found
     * @throws AccessDeniedException if teacher doesn't have access to the student
     */
    public Optional<Student> getStudentById(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        // Verify teacher has access to the student
        verifyStudentAccess(studentId);
        
        return studentRepository.findById(studentId);
    }
    
    /**
     * Delete a student by ID.
     * Verifies that the authenticated teacher has access to the student.
     * 
     * @param studentId Student ID
     * @throws IllegalArgumentException if student ID is null or student not found
     * @throws AccessDeniedException if teacher doesn't have access to the student
     */
    public void deleteStudent(Long studentId) {
        if (studentId == null) {
            throw new IllegalArgumentException("Student ID cannot be null");
        }
        
        if (!studentRepository.existsById(studentId)) {
            throw new IllegalArgumentException("Student with ID " + studentId + " not found");
        }
        
        // Verify teacher has access to the student
        verifyStudentAccess(studentId);
        
        studentRepository.deleteById(studentId);
    }
    
    /**
     * Count students in a specific class.
     * Verifies that the authenticated teacher has access to the class.
     * 
     * @param classId Class ID
     * @return Number of students in the class
     * @throws AccessDeniedException if teacher doesn't have access to the class
     */
    public long countStudentsByClass(Long classId) {
        if (classId == null) {
            throw new IllegalArgumentException("Class ID cannot be null");
        }
        
        // Verify teacher has access to the class
        verifyClassAccess(classId);
        
        return studentRepository.countByClassEntityId(classId);
    }
    
    /**
     * Check if a student exists with the given student ID.
     * 
     * @param studentId Student ID to check
     * @return true if a student with this ID exists, false otherwise
     */
    public boolean existsByStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            return false;
        }
        
        return studentRepository.existsByStudentId(studentId);
    }
}
