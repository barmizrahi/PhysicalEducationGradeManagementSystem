package com.pe.grademanagement.controller;

import com.pe.grademanagement.dto.GoogleAuthRequest;
import com.pe.grademanagement.dto.GoogleAuthResponse;
import com.pe.grademanagement.entity.Teacher;
import com.pe.grademanagement.repository.TeacherRepository;
import com.pe.grademanagement.service.AuthService;
import com.pe.grademanagement.service.GoogleOAuthService;
import com.pe.grademanagement.util.JwtUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for authentication endpoints.
 * Handles Google OAuth authentication and logout operations.
 * 
 * Requirements:
 * - Provide Google OAuth authentication endpoints
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthService authService;
    private final GoogleOAuthService googleOAuthService;
    private final TeacherRepository teacherRepository;
    private final JwtUtil jwtUtil;
    
    @Autowired
    public AuthController(AuthService authService, 
                         GoogleOAuthService googleOAuthService,
                         TeacherRepository teacherRepository,
                         JwtUtil jwtUtil) {
        this.authService = authService;
        this.googleOAuthService = googleOAuthService;
        this.teacherRepository = teacherRepository;
        this.jwtUtil = jwtUtil;
    }
    
    /**
     * Google OAuth callback endpoint.
     * Exchanges authorization code for user information, creates or updates teacher record,
     * and returns JWT token.
     * 
     * @param request GoogleAuthRequest containing authorization code and optional redirect URI
     * @return GoogleAuthResponse with JWT token and user information
     */
    @PostMapping("/google/callback")
    public ResponseEntity<?> googleCallback(@Valid @RequestBody GoogleAuthRequest request) {
        try {
            logger.info("Processing Google OAuth callback");
            
            // Step 1: Exchange authorization code for user info (with redirect URI from frontend)
            Map<String, Object> userInfo = googleOAuthService.exchangeCodeForUserInfo(
                request.getCode(), 
                request.getRedirectUri()
            );
            Map<String, String> profile = googleOAuthService.extractUserProfile(userInfo);
            
            String email = profile.get("email");
            String googleId = profile.get("googleId");
            String name = profile.get("name");
            String picture = profile.get("picture");
            
            logger.debug("Retrieved user profile for email: {}", email);
            
            // Step 2: Create or update teacher record
            Teacher teacher = teacherRepository.findByEmail(email)
                    .orElseGet(() -> {
                        logger.info("Creating new teacher record for email: {}", email);
                        Teacher newTeacher = new Teacher(email, googleId, name, picture);
                        return teacherRepository.save(newTeacher);
                    });
            
            // Update existing teacher with latest info
            if (teacher.getId() != null) {
                logger.debug("Updating existing teacher record for email: {}", email);
                teacher.setGoogleId(googleId);
                teacher.setFullName(name);
                teacher.setPicture(picture);
                teacher = teacherRepository.save(teacher);
            }
            
            // Step 3: Generate JWT token
            String token = jwtUtil.generateToken(email, teacher.getId());
            logger.info("Successfully authenticated user: {}", email);
            
            // Step 4: Build response
            GoogleAuthResponse.UserInfo userInfoDto = new GoogleAuthResponse.UserInfo(
                    teacher.getId(),
                    teacher.getEmail(),
                    teacher.getFullName(),
                    teacher.getPicture()
            );
            
            GoogleAuthResponse response = new GoogleAuthResponse(token, userInfoDto);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Google OAuth authentication failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("אימות Google נכשל: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during Google OAuth: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("שגיאה בלתי צפויה במהלך האימות"));
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
        return ResponseEntity.ok(new MessageResponse("התנתקות בוצעה בהצלחה"));
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
