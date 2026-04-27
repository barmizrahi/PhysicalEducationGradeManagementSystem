package com.pe.grademanagement.repository;

import com.pe.grademanagement.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Student entity.
 * Provides CRUD operations and custom query methods for student data access.
 * 
 * Requirements:
 * - 1.7: Detect existing students using student ID if available
 * - 1.8: Use name and class combination for duplicate detection when student ID is not available
 * - 2.1: Display students grouped by grade level
 * - 2.2: Display students grouped by class name within each grade level
 * - 2.4: Display all students in a selected class
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    /**
     * Find a student by student ID.
     * Used for duplicate detection when student ID is available.
     * 
     * @param studentId the student's ID
     * @return Optional containing the student if found, empty otherwise
     */
    Optional<Student> findByStudentId(String studentId);
    
    /**
     * Find a student by name and class.
     * Used for duplicate detection when student ID is not available.
     * 
     * @param name the student's name
     * @param classId the class ID
     * @return Optional containing the student if found, empty otherwise
     */
    Optional<Student> findByNameAndClassEntityId(String name, Long classId);
    
    /**
     * Find all students in a specific class.
     * Used for displaying students in a class and for grade entry.
     * 
     * @param classId the class ID
     * @return list of students in the class
     */
    List<Student> findByClassEntityId(Long classId);
    
    /**
     * Find all students in a specific class, ordered by name.
     * Used for displaying students in alphabetical order.
     * 
     * @param classId the class ID
     * @return list of students in the class, ordered by name
     */
    List<Student> findByClassEntityIdOrderByName(Long classId);
    
    /**
     * Find all students for a specific grade level.
     * Used for filtering students by grade level.
     * 
     * @param gradeLevel the grade level (י, יא, יב)
     * @return list of students in the grade level
     */
    List<Student> findByGradeLevel(String gradeLevel);
    
    /**
     * Find all students for a specific teacher (through class relationship).
     * Used for teacher authorization and data isolation.
     * 
     * @param teacherId the teacher's ID
     * @return list of students assigned to the teacher's classes
     */
    @Query("SELECT s FROM Student s WHERE s.classEntity.teacher.id = :teacherId")
    List<Student> findByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * Find all students for a specific teacher and grade level.
     * Combines teacher authorization with grade level filtering.
     * 
     * @param teacherId the teacher's ID
     * @param gradeLevel the grade level (י, יא, יב)
     * @return list of students for the teacher and grade level
     */
    @Query("SELECT s FROM Student s WHERE s.classEntity.teacher.id = :teacherId AND s.gradeLevel = :gradeLevel")
    List<Student> findByTeacherIdAndGradeLevel(@Param("teacherId") Long teacherId, 
                                                 @Param("gradeLevel") String gradeLevel);
    
    /**
     * Find all students for a specific teacher, ordered by grade level, class name, and student name.
     * Used for displaying students in a structured, hierarchical manner.
     * 
     * @param teacherId the teacher's ID
     * @return list of students ordered by grade level, class name, and name
     */
    @Query("SELECT s FROM Student s WHERE s.classEntity.teacher.id = :teacherId " +
           "ORDER BY s.gradeLevel, s.classEntity.name, s.name")
    List<Student> findByTeacherIdOrderByGradeLevelAndClassAndName(@Param("teacherId") Long teacherId);
    
    /**
     * Count students in a specific class.
     * Useful for displaying class size.
     * 
     * @param classId the class ID
     * @return number of students in the class
     */
    long countByClassEntityId(Long classId);
    
    /**
     * Check if a student exists with the given student ID.
     * Used for duplicate detection validation.
     * 
     * @param studentId the student ID to check
     * @return true if a student with this ID exists, false otherwise
     */
    boolean existsByStudentId(String studentId);
}
