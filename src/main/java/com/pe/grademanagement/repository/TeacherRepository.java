package com.pe.grademanagement.repository;

import com.pe.grademanagement.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Teacher entity.
 * Provides CRUD operations and custom query methods for teacher data access.
 * 
 * Requirements:
 * - 13.1: Support teacher authentication
 * - 13.2: Associate teachers with their assigned classes
 */
@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    
    /**
     * Find a teacher by username.
     * Used for authentication.
     * 
     * @param username the teacher's username
     * @return Optional containing the teacher if found, empty otherwise
     */
    Optional<Teacher> findByUsername(String username);
    
    /**
     * Check if a teacher exists with the given username.
     * Useful for validation during teacher creation.
     * 
     * @param username the username to check
     * @return true if a teacher with this username exists, false otherwise
     */
    boolean existsByUsername(String username);
}
