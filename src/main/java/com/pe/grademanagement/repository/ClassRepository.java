package com.pe.grademanagement.repository;

import com.pe.grademanagement.entity.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Class entity.
 * Provides CRUD operations and custom query methods for class data access.
 * 
 * Requirements:
 * - 2.1: Display students grouped by grade level
 * - 2.2: Display students grouped by class name within each grade level
 * - 2.3: Filter classes by grade level
 * - 13.3: Display only classes assigned to authenticated teacher
 */
@Repository
public interface ClassRepository extends JpaRepository<Class, Long> {
    
    /**
     * Find all classes assigned to a specific teacher.
     * Used for teacher authorization and data isolation.
     * 
     * @param teacherId the teacher's ID
     * @return list of classes assigned to the teacher
     */
    List<Class> findByTeacherId(Long teacherId);
    
    /**
     * Find all classes for a specific grade level.
     * Used for filtering classes by grade level.
     * 
     * @param gradeLevel the grade level (י, יא, יב)
     * @return list of classes for the specified grade level
     */
    List<Class> findByGradeLevel(String gradeLevel);
    
    /**
     * Find all classes for a specific teacher and grade level.
     * Combines teacher authorization with grade level filtering.
     * 
     * @param teacherId the teacher's ID
     * @param gradeLevel the grade level (י, יא, יב)
     * @return list of classes for the teacher and grade level
     */
    List<Class> findByTeacherIdAndGradeLevel(Long teacherId, String gradeLevel);
    
    /**
     * Find all classes for a specific teacher, ordered by grade level and name.
     * Used for displaying classes in a structured manner.
     * 
     * @param teacherId the teacher's ID
     * @return list of classes ordered by grade level and name
     */
    @Query("SELECT c FROM Class c WHERE c.teacher.id = :teacherId ORDER BY c.gradeLevel, c.name")
    List<Class> findByTeacherIdOrderByGradeLevelAndName(@Param("teacherId") Long teacherId);
    
    /**
     * Check if a class exists with the given name, grade level, and teacher.
     * Useful for preventing duplicate class creation.
     * 
     * @param name the class name
     * @param gradeLevel the grade level
     * @param teacherId the teacher's ID
     * @return true if such a class exists, false otherwise
     */
    boolean existsByNameAndGradeLevelAndTeacherId(String name, String gradeLevel, Long teacherId);
    
    /**
     * Find a class by name, grade level, and teacher.
     * Used during student import to find or create classes.
     * 
     * @param name the class name
     * @param gradeLevel the grade level
     * @param teacherId the teacher's ID
     * @return Optional containing the class if found
     */
    java.util.Optional<Class> findByNameAndGradeLevelAndTeacherId(String name, String gradeLevel, Long teacherId);
}
