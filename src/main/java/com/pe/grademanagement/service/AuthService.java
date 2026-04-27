package com.pe.grademanagement.service;

import com.pe.grademanagement.dto.LoginRequest;
import com.pe.grademanagement.dto.LoginResponse;
import com.pe.grademanagement.entity.Teacher;
import com.pe.grademanagement.repository.TeacherRepository;
import com.pe.grademanagement.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for handling teacher authentication.
 * Provides login and logout functionality with JWT token generation.
 * 
 * Requirements:
 * - 13.1: Authenticate teachers and generate JWT tokens
 */
@Service
public class AuthService {
    
    private final TeacherRepository teacherRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    public AuthService(TeacherRepository teacherRepository, JwtUtil jwtUtil) {
        this.teacherRepository = teacherRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    
    /**
     * Authenticate a teacher and generate JWT token.
     * 
     * @param loginRequest login credentials (username and password)
     * @return LoginResponse containing JWT token and teacher information
     * @throws IllegalArgumentException if credentials are invalid
     */
    public LoginResponse login(LoginRequest loginRequest) {
        // Find teacher by username
        Teacher teacher = teacherRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        
        // Verify password using BCrypt
        if (!passwordEncoder.matches(loginRequest.getPassword(), teacher.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        
        // Generate JWT token
        String token = jwtUtil.generateToken(teacher.getUsername(), teacher.getId());
        
        // Return response with token and teacher info
        return new LoginResponse(
                token,
                teacher.getId(),
                teacher.getUsername(),
                teacher.getFullName()
        );
    }
    
    /**
     * Authenticate a teacher with username and password.
     * Alternative method signature for direct parameter passing.
     * 
     * @param username teacher's username
     * @param password teacher's password
     * @return LoginResponse containing JWT token and teacher information
     * @throws IllegalArgumentException if credentials are invalid
     */
    public LoginResponse login(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        return login(loginRequest);
    }
    
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
    
    /**
     * Hash a password using BCrypt.
     * Utility method for creating password hashes when registering new teachers.
     * 
     * @param plainPassword plain text password
     * @return BCrypt hashed password
     */
    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }
    
    /**
     * Verify a password against a hash.
     * Utility method for password verification.
     * 
     * @param plainPassword plain text password
     * @param hashedPassword BCrypt hashed password
     * @return true if password matches hash, false otherwise
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }
}
