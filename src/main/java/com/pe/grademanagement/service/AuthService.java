package com.pe.grademanagement.service;

import org.springframework.stereotype.Service;

/**
 * Service for handling teacher authentication.
 * Provides logout functionality (login is now handled via Google OAuth).
 * 
 * Requirements:
 * - Support logout functionality
 */
@Service
public class AuthService {
    
    /**
     * Logout a teacher.
     * Since JWT is stateless, this is primarily a placeholder for client-side token removal.
     * In a production system, you might implement token blacklisting here.
     * 
     * Note: The actual logout is handled client-side by removing the JWT token.
     * This method exists for API consistency and future enhancements (e.g., token blacklisting).
     */
    public void logout() {
        // JWT is stateless, so logout is handled client-side by removing the token
        // This method can be extended in the future to implement token blacklisting
        // or other server-side logout mechanisms if needed
    }
}
