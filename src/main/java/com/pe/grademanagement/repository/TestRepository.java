package com.pe.grademanagement.repository;

import com.pe.grademanagement.entity.CalculationType;
import com.pe.grademanagement.entity.Test;
import com.pe.grademanagement.entity.UnitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Test entity.
 * Provides CRUD operations and custom query methods for test data access.
 * 
 * Requirements:
 * - 3.1: Create and manage test configurations
 * - 3.7: Allow editing of existing test configurations
 * - 15: Support test assignment to classes
 */
@Repository
public interface TestRepository extends JpaRepository<Test, Long> {
    
    /**
     * Find all tests created by a specific teacher.
     * Used for displaying a teacher's test library.
     * 
     * @param teacherId the teacher's ID
     * @return list of tests created by the teacher
     */
    List<Test> findByCreatedById(Long teacherId);
    
    /**
     * Find all tests created by a specific teacher, ordered by name.
     * Used for displaying tests in alphabetical order.
     * 
     * @param teacherId the teacher's ID
     * @return list of tests created by the teacher, ordered by name
     */
    List<Test> findByCreatedByIdOrderByName(Long teacherId);
    
    /**
     * Find all tests assigned to a specific class.
     * Used for displaying tests available for a class during grade entry.
     * 
     * @param classId the class ID
     * @return list of tests assigned to the class
     */
    @Query("SELECT t FROM Test t JOIN t.assignments ta WHERE ta.classEntity.id = :classId")
    List<Test> findByClassId(@Param("classId") Long classId);
    
    /**
     * Find all tests assigned to a specific class, ordered by name.
     * Used for displaying tests in alphabetical order during grade entry.
     * 
     * @param classId the class ID
     * @return list of tests assigned to the class, ordered by name
     */
    @Query("SELECT t FROM Test t JOIN t.assignments ta WHERE ta.classEntity.id = :classId ORDER BY t.name")
    List<Test> findByClassIdOrderByName(@Param("classId") Long classId);
    
    /**
     * Find all tests by calculation type.
     * Useful for filtering tests by calculation method.
     * 
     * @param calculationType the calculation type (RATIO or PENALTY)
     * @return list of tests with the specified calculation type
     */
    List<Test> findByCalculationType(CalculationType calculationType);
    
    /**
     * Find all tests by unit type.
     * Useful for filtering tests by measurement unit.
     * 
     * @param unitType the unit type (TIME or COUNT)
     * @return list of tests with the specified unit type
     */
    List<Test> findByUnitType(UnitType unitType);
    
    /**
     * Find all tests created by a teacher with a specific calculation type.
     * Combines teacher filtering with calculation type filtering.
     * 
     * @param teacherId the teacher's ID
     * @param calculationType the calculation type (RATIO or PENALTY)
     * @return list of tests matching both criteria
     */
    List<Test> findByCreatedByIdAndCalculationType(Long teacherId, CalculationType calculationType);
    
    /**
     * Find all tests created by a teacher with a specific unit type.
     * Combines teacher filtering with unit type filtering.
     * 
     * @param teacherId the teacher's ID
     * @param unitType the unit type (TIME or COUNT)
     * @return list of tests matching both criteria
     */
    List<Test> findByCreatedByIdAndUnitType(Long teacherId, UnitType unitType);
    
    /**
     * Find all tests assigned to classes of a specific grade level.
     * Used for filtering tests by grade level.
     * 
     * @param gradeLevel the grade level (י, יא, יב)
     * @return list of tests assigned to classes in the grade level
     */
    @Query("SELECT DISTINCT t FROM Test t JOIN t.assignments ta WHERE ta.classEntity.gradeLevel = :gradeLevel")
    List<Test> findByGradeLevel(@Param("gradeLevel") String gradeLevel);
    
    /**
     * Check if a test with the given name exists for a specific teacher.
     * Useful for preventing duplicate test names.
     * 
     * @param name the test name
     * @param teacherId the teacher's ID
     * @return true if such a test exists, false otherwise
     */
    boolean existsByNameAndCreatedById(String name, Long teacherId);
}
