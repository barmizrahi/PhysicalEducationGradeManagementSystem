package com.pe.grademanagement.controller;

import com.pe.grademanagement.dto.LoginRequest;
import com.pe.grademanagement.dto.LoginResponse;
import com.pe.grademanagement.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * Handles teacher login and logout operations.
 * 
 * Requirements:
 * - 13.1: Provide authentication endpoints for teachers
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Teacher login endpoint.
     * Authenticates teacher credentials and returns JWT token.
     * 
     * @param loginRequest login credentials (username and password)
     * @return LoginResponse with JWT token and teacher information
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Authentication failed: " + e.getMessage()));
        }
    }
    
    /**
     * Teacher logout endpoint.
     * Since JWT is stateless, this is primarily for API consistency.
     * Actual logout is handled client-side by removing the JWT token.
     * 
     * @return success message
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        authService.logout();
        return ResponseEntity.ok(new MessageResponse("Logout successful"));
    }
    
    /**
     * Simple error response DTO.
     */
    private static class ErrorResponse {
        private final String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() {
            return error;
        }
    }
    
    /**
     * Simple message response DTO.
     */
    private static class MessageResponse {
        private final String message;
        
        public MessageResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
