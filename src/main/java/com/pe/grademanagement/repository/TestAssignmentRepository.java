package com.pe.grademanagement.repository;

import com.pe.grademanagement.entity.TestAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TestAssignment entity.
 * Provides CRUD operations and custom query methods for test assignment data access.
 * 
 * Requirements:
 * - 15: Support test assignment to classes
 * - Allow assigning tests at class or grade level
 */
@Repository
public interface TestAssignmentRepository extends JpaRepository<TestAssignment, Long> {
    
    /**
     * Find all test assignments for a specific test.
     * Used for displaying which classes a test is assigned to.
     * 
     * @param testId the test ID
     * @return list of test assignments for the test
     */
    List<TestAssignment> findByTestId(Long testId);
    
    /**
     * Find all test assignments for a specific class.
     * Used for displaying which tests are assigned to a class.
     * 
     * @param classId the class ID
     * @return list of test assignments for the class
     */
    List<TestAssignment> findByClassEntityId(Long classId);
    
    /**
     * Find a specific test assignment by test and class.
     * Used for checking if a test is already assigned to a class.
     * 
     * @param testId the test ID
     * @param classId the class ID
     * @return Optional containing the test assignment if found, empty otherwise
     */
    Optional<TestAssignment> findByTestIdAndClassEntityId(Long testId, Long classId);
    
    /**
     * Find all test assignments for tests created by a specific teacher.
     * Used for displaying all assignments for a teacher's tests.
     * 
     * @param teacherId the teacher's ID
     * @return list of test assignments for the teacher's tests
     */
    @Query("SELECT ta FROM TestAssignment ta WHERE ta.test.createdBy.id = :teacherId")
    List<TestAssignment> findByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * Find all test assignments for a specific grade level.
     * Used for grade-level test assignment operations.
     * 
     * @param gradeLevel the grade level (י, יא, יב)
     * @return list of test assignments for classes in the grade level
     */
    @Query("SELECT ta FROM TestAssignment ta WHERE ta.classEntity.gradeLevel = :gradeLevel")
    List<TestAssignment> findByGradeLevel(@Param("gradeLevel") String gradeLevel);
    
    /**
     * Find all test assignments for a specific test and grade level.
     * Used for checking which classes in a grade level have a test assigned.
     * 
     * @param testId the test ID
     * @param gradeLevel the grade level (י, יא, יב)
     * @return list of test assignments matching both criteria
     */
    @Query("SELECT ta FROM TestAssignment ta WHERE ta.test.id = :testId AND ta.classEntity.gradeLevel = :gradeLevel")
    List<TestAssignment> findByTestIdAndGradeLevel(@Param("testId") Long testId, 
                                                     @Param("gradeLevel") String gradeLevel);
    
    /**
     * Check if a test is assigned to a specific class.
     * Useful for validation before creating a new assignment.
     * 
     * @param testId the test ID
     * @param classId the class ID
     * @return true if the test is assigned to the class, false otherwise
     */
    boolean existsByTestIdAndClassEntityId(Long testId, Long classId);
    
    /**
     * Count the number of classes a test is assigned to.
     * Useful for displaying assignment statistics.
     * 
     * @param testId the test ID
     * @return number of classes the test is assigned to
     */
    long countByTestId(Long testId);
    
    /**
     * Count the number of tests assigned to a class.
     * Useful for displaying class statistics.
     * 
     * @param classId the class ID
     * @return number of tests assigned to the class
     */
    long countByClassEntityId(Long classId);
    
    /**
     * Delete all test assignments for a specific test.
     * Used when deleting a test or reassigning it to different classes.
     * 
     * @param testId the test ID
     */
    void deleteByTestId(Long testId);
    
    /**
     * Delete all test assignments for a specific class.
     * Used when deleting a class.
     * 
     * @param classId the class ID
     */
    void deleteByClassEntityId(Long classId);
}
