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
 * - Support teacher authentication via Google OAuth
 * - Associate teachers with their assigned classes
 */
@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    
    /**
     * Find a teacher by email address.
     * Used for authentication and user identification.
     * 
     * @param email the teacher's email address
     * @return Optional containing the teacher if found, empty otherwise
     */
    Optional<Teacher> findByEmail(String email);
    
    /**
     * Find a teacher by Google ID.
     * Used for OAuth authentication.
     * 
     * @param googleId the teacher's Google user ID
     * @return Optional containing the teacher if found, empty otherwise
     */
    Optional<Teacher> findByGoogleId(String googleId);
    
    /**
     * Check if a teacher exists with the given email.
     * Useful for validation during teacher creation.
     * 
     * @param email the email to check
     * @return true if a teacher with this email exists, false otherwise
     */
    boolean existsByEmail(String email);
}
