package com.pe.grademanagement.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pe.grademanagement.dto.LoginRequest;
import com.pe.grademanagement.dto.LoginResponse;
import com.pe.grademanagement.entity.Teacher;
import com.pe.grademanagement.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.Timestamp;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for JWT authentication.
 * Tests the complete authentication flow including login, token validation, and protected endpoints.
 * 
 * Requirements:
 * - 13.1: Verify JWT authentication works end-to-end
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtAuthenticationIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private TeacherRepository teacherRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private Teacher testTeacher;
    private String testPassword = "password123";
    
    @BeforeEach
    void setUp() {
        // Clean up database
        teacherRepository.deleteAll();
        
        // Create test teacher
        testTeacher = new Teacher();
        testTeacher.setUsername("testteacher");
        testTeacher.setPasswordHash(passwordEncoder.encode(testPassword));
        testTeacher.setFullName("Test Teacher");
        testTeacher.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        testTeacher = teacherRepository.save(testTeacher);
    }
    
    @Test
    void testLoginWithValidCredentials() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(testTeacher.getUsername(), testPassword);
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value(testTeacher.getUsername()))
                .andExpect(jsonPath("$.fullName").value(testTeacher.getFullName()))
                .andExpect(jsonPath("$.teacherId").value(testTeacher.getId()));
    }
    
    @Test
    void testLoginWithInvalidUsername() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("nonexistent", testPassword);
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }
    
    @Test
    void testLoginWithInvalidPassword() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(testTeacher.getUsername(), "wrongpassword");
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }
    
    @Test
    void testAccessProtectedEndpointWithoutToken() throws Exception {
        // Act & Assert - try to access a protected endpoint without token
        mockMvc.perform(get("/api/students/by-grade-and-class"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testAccessProtectedEndpointWithValidToken() throws Exception {
        // Arrange - login to get token
        LoginRequest loginRequest = new LoginRequest(testTeacher.getUsername(), testPassword);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        String responseBody = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseBody, LoginResponse.class);
        String token = loginResponse.getToken();
        
        // Act & Assert - access protected endpoint with token
        mockMvc.perform(get("/api/students/by-grade-and-class")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
    
    @Test
    void testAccessProtectedEndpointWithInvalidToken() throws Exception {
        // Act & Assert - try to access protected endpoint with invalid token
        mockMvc.perform(get("/api/students/by-grade-and-class")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testAccessProtectedEndpointWithMalformedAuthHeader() throws Exception {
        // Act & Assert - try to access protected endpoint with malformed header
        mockMvc.perform(get("/api/students/by-grade-and-class")
                .header("Authorization", "InvalidFormat token"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testLogoutEndpoint() throws Exception {
        // Arrange - login to get token
        LoginRequest loginRequest = new LoginRequest(testTeacher.getUsername(), testPassword);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        
        String responseBody = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseBody, LoginResponse.class);
        String token = loginResponse.getToken();
        
        // Act & Assert - logout
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    void testAuthEndpointsArePublic() throws Exception {
        // Act & Assert - verify auth endpoints don't require authentication
        LoginRequest loginRequest = new LoginRequest(testTeacher.getUsername(), testPassword);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }
}
