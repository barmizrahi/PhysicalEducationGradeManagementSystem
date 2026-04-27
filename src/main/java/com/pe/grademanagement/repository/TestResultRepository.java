package com.pe.grademanagement.repository;

import com.pe.grademanagement.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for TestResult entity.
 * Provides CRUD operations and custom query methods for test result data access.
 * 
 * Requirements:
 * - 6.1: Display all students in a class for grade entry
 * - 7.1: Store and retrieve test results
 * - 7.2: Support updating existing test results
 * - 8.3: Include students without test results in exports
 */
@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    
    /**
     * Find a test result by student and test.
     * Used for checking if a result already exists before creating or updating.
     * 
     * @param studentId the student's ID
     * @param testId the test ID
     * @return Optional containing the test result if found, empty otherwise
     */
    Optional<TestResult> findByStudentIdAndTestId(Long studentId, Long testId);
    
    /**
     * Find all test results for a specific student.
     * Used for displaying a student's complete grade history.
     * 
     * @param studentId the student's ID
     * @return list of test results for the student
     */
    List<TestResult> findByStudentId(Long studentId);
    
    /**
     * Find all test results for a specific test.
     * Used for displaying all results for a test across all students.
     * 
     * @param testId the test ID
     * @return list of test results for the test
     */
    List<TestResult> findByTestId(Long testId);
    
    /**
     * Find all test results for a specific class and test.
     * Used for grade entry interface - displays all students' results for a test.
     * 
     * @param classId the class ID
     * @param testId the test ID
     * @return list of test results for the class and test
     */
    @Query("SELECT tr FROM TestResult tr WHERE tr.student.classEntity.id = :classId AND tr.test.id = :testId")
    List<TestResult> findByClassIdAndTestId(@Param("classId") Long classId, 
                                             @Param("testId") Long testId);
    
    /**
     * Find all test results for a specific class and test, ordered by student name.
     * Used for displaying results in alphabetical order during grade entry.
     * 
     * @param classId the class ID
     * @param testId the test ID
     * @return list of test results ordered by student name
     */
    @Query("SELECT tr FROM TestResult tr WHERE tr.student.classEntity.id = :classId AND tr.test.id = :testId " +
           "ORDER BY tr.student.name")
    List<TestResult> findByClassIdAndTestIdOrderByStudentName(@Param("classId") Long classId, 
                                                                @Param("testId") Long testId);
    
    /**
     * Find all test results for a specific class.
     * Used for displaying all results for a class across all tests.
     * 
     * @param classId the class ID
     * @return list of test results for the class
     */
    @Query("SELECT tr FROM TestResult tr WHERE tr.student.classEntity.id = :classId")
    List<TestResult> findByClassId(@Param("classId") Long classId);
    
    /**
     * Find all test results for a specific teacher (through class relationship).
     * Used for teacher authorization and data isolation.
     * 
     * @param teacherId the teacher's ID
     * @return list of test results for the teacher's students
     */
    @Query("SELECT tr FROM TestResult tr WHERE tr.student.classEntity.teacher.id = :teacherId")
    List<TestResult> findByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * Find all test results for a specific grade level.
     * Used for filtering results by grade level.
     * 
     * @param gradeLevel the grade level (י, יא, יב)
     * @return list of test results for the grade level
     */
    @Query("SELECT tr FROM TestResult tr WHERE tr.student.gradeLevel = :gradeLevel")
    List<TestResult> findByGradeLevel(@Param("gradeLevel") String gradeLevel);
    
    /**
     * Find all test results with null raw results (students who didn't take the test).
     * Used for identifying students who need to take the test.
     * 
     * @param testId the test ID
     * @return list of test results with null raw results
     */
    @Query("SELECT tr FROM TestResult tr WHERE tr.test.id = :testId AND tr.rawResult IS NULL")
    List<TestResult> findByTestIdAndRawResultIsNull(@Param("testId") Long testId);
    
    /**
     * Find all test results with notes.
     * Used for reviewing special cases or comments.
     * 
     * @return list of test results that have notes
     */
    @Query("SELECT tr FROM TestResult tr WHERE tr.notes IS NOT NULL AND tr.notes <> ''")
    List<TestResult> findAllWithNotes();
    
    /**
     * Check if a test result exists for a specific student and test.
     * Useful for validation before creating a new result.
     * 
     * @param studentId the student's ID
     * @param testId the test ID
     * @return true if a result exists, false otherwise
     */
    boolean existsByStudentIdAndTestId(Long studentId, Long testId);
    
    /**
     * Count test results for a specific test.
     * Useful for displaying how many students have taken the test.
     * 
     * @param testId the test ID
     * @return number of test results for the test
     */
    long countByTestId(Long testId);
    
    /**
     * Count test results with non-null raw results for a specific test.
     * Useful for displaying how many students actually took the test.
     * 
     * @param testId the test ID
     * @return number of test results with raw results
     */
    @Query("SELECT COUNT(tr) FROM TestResult tr WHERE tr.test.id = :testId AND tr.rawResult IS NOT NULL")
    long countByTestIdAndRawResultIsNotNull(@Param("testId") Long testId);
    
    /**
     * Delete all test results for a specific student.
     * Used when deleting a student.
     * 
     * @param studentId the student's ID
     */
    void deleteByStudentId(Long studentId);
    
    /**
     * Delete all test results for a specific test.
     * Used when deleting a test.
     * 
     * @param testId the test ID
     */
    void deleteByTestId(Long testId);
}
