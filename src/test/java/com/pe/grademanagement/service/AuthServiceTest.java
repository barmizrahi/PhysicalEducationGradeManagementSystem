package com.pe.grademanagement.service;

import com.pe.grademanagement.dto.LoginRequest;
import com.pe.grademanagement.dto.LoginResponse;
import com.pe.grademanagement.entity.Teacher;
import com.pe.grademanagement.repository.TeacherRepository;
import com.pe.grademanagement.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Timestamp;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 * Tests authentication, JWT token generation, and password hashing.
 * 
 * Requirements:
 * - 13.1: Teacher authentication with JWT tokens
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private TeacherRepository teacherRepository;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @InjectMocks
    private AuthService authService;
    
    private Teacher testTeacher;
    private BCryptPasswordEncoder passwordEncoder;
    
    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        
        // Create test teacher with hashed password
        testTeacher = new Teacher();
        testTeacher.setId(1L);
        testTeacher.setUsername("test.teacher");
        testTeacher.setPasswordHash(passwordEncoder.encode("password123"));
        testTeacher.setFullName("Test Teacher");
        testTeacher.setCreatedAt(new Timestamp(System.currentTimeMillis()));
    }
    
    @Test
    void testLoginWithValidCredentials() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("test.teacher", "password123");
        String expectedToken = "jwt.token.here";
        
        when(teacherRepository.findByUsername("test.teacher")).thenReturn(Optional.of(testTeacher));
        when(jwtUtil.generateToken("test.teacher", 1L)).thenReturn(expectedToken);
        
        // Act
        LoginResponse response = authService.login(loginRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());
        assertEquals(1L, response.getTeacherId());
        assertEquals("test.teacher", response.getUsername());
        assertEquals("Test Teacher", response.getFullName());
        
        verify(teacherRepository).findByUsername("test.teacher");
        verify(jwtUtil).generateToken("test.teacher", 1L);
    }
    
    @Test
    void testLoginWithDirectParameters() {
        // Arrange
        String expectedToken = "jwt.token.here";
        
        when(teacherRepository.findByUsername("test.teacher")).thenReturn(Optional.of(testTeacher));
        when(jwtUtil.generateToken("test.teacher", 1L)).thenReturn(expectedToken);
        
        // Act
        LoginResponse response = authService.login("test.teacher", "password123");
        
        // Assert
        assertNotNull(response);
        assertEquals(expectedToken, response.getToken());
        assertEquals(1L, response.getTeacherId());
        assertEquals("test.teacher", response.getUsername());
        assertEquals("Test Teacher", response.getFullName());
    }
    
    @Test
    void testLoginWithInvalidUsername() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("nonexistent.teacher", "password123");
        
        when(teacherRepository.findByUsername("nonexistent.teacher")).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(loginRequest)
        );
        
        assertEquals("Invalid username or password", exception.getMessage());
        verify(teacherRepository).findByUsername("nonexistent.teacher");
        verify(jwtUtil, never()).generateToken(anyString(), any());
    }
    
    @Test
    void testLoginWithInvalidPassword() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("test.teacher", "wrongpassword");
        
        when(teacherRepository.findByUsername("test.teacher")).thenReturn(Optional.of(testTeacher));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(loginRequest)
        );
        
        assertEquals("Invalid username or password", exception.getMessage());
        verify(teacherRepository).findByUsername("test.teacher");
        verify(jwtUtil, never()).generateToken(anyString(), any());
    }
    
    @Test
    void testLoginWithEmptyPassword() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("test.teacher", "");
        
        when(teacherRepository.findByUsername("test.teacher")).thenReturn(Optional.of(testTeacher));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(loginRequest)
        );
        
        assertEquals("Invalid username or password", exception.getMessage());
    }
    
    @Test
    void testLogout() {
        // Act - should not throw any exception
        assertDoesNotThrow(() -> authService.logout());
        
        // Logout is a no-op for stateless JWT, but method should exist
        // for API consistency and future enhancements
    }
    
    @Test
    void testHashPassword() {
        // Arrange
        String plainPassword = "mySecurePassword123";
        
        // Act
        String hashedPassword = authService.hashPassword(plainPassword);
        
        // Assert
        assertNotNull(hashedPassword);
        assertNotEquals(plainPassword, hashedPassword);
        assertTrue(hashedPassword.startsWith("$2a$")); // BCrypt hash format
        
        // Verify the hash can be used to verify the original password
        assertTrue(passwordEncoder.matches(plainPassword, hashedPassword));
    }
    
    @Test
    void testHashPasswordProducesDifferentHashesForSamePassword() {
        // Arrange
        String plainPassword = "password123";
        
        // Act
        String hash1 = authService.hashPassword(plainPassword);
        String hash2 = authService.hashPassword(plainPassword);
        
        // Assert - BCrypt uses salt, so same password produces different hashes
        assertNotEquals(hash1, hash2);
        
        // But both hashes should verify the original password
        assertTrue(passwordEncoder.matches(plainPassword, hash1));
        assertTrue(passwordEncoder.matches(plainPassword, hash2));
    }
    
    @Test
    void testVerifyPasswordWithCorrectPassword() {
        // Arrange
        String plainPassword = "correctPassword";
        String hashedPassword = passwordEncoder.encode(plainPassword);
        
        // Act
        boolean result = authService.verifyPassword(plainPassword, hashedPassword);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void testVerifyPasswordWithIncorrectPassword() {
        // Arrange
        String plainPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        String hashedPassword = passwordEncoder.encode(plainPassword);
        
        // Act
        boolean result = authService.verifyPassword(wrongPassword, hashedPassword);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void testVerifyPasswordWithEmptyPassword() {
        // Arrange
        String hashedPassword = passwordEncoder.encode("password");
        
        // Act
        boolean result = authService.verifyPassword("", hashedPassword);
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void testLoginGeneratesTokenWithCorrectParameters() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("test.teacher", "password123");
        String expectedToken = "generated.jwt.token";
        
        when(teacherRepository.findByUsername("test.teacher")).thenReturn(Optional.of(testTeacher));
        when(jwtUtil.generateToken("test.teacher", 1L)).thenReturn(expectedToken);
        
        // Act
        LoginResponse response = authService.login(loginRequest);
        
        // Assert
        verify(jwtUtil).generateToken("test.teacher", 1L);
        assertEquals(expectedToken, response.getToken());
    }
    
    @Test
    void testLoginReturnsCompleteTeacherInformation() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("test.teacher", "password123");
        
        when(teacherRepository.findByUsername("test.teacher")).thenReturn(Optional.of(testTeacher));
        when(jwtUtil.generateToken(anyString(), any())).thenReturn("token");
        
        // Act
        LoginResponse response = authService.login(loginRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals(testTeacher.getId(), response.getTeacherId());
        assertEquals(testTeacher.getUsername(), response.getUsername());
        assertEquals(testTeacher.getFullName(), response.getFullName());
        assertNotNull(response.getToken());
    }
    
    @Test
    void testPasswordHashingIsSecure() {
        // Arrange
        String password = "securePassword123!";
        
        // Act
        String hash = authService.hashPassword(password);
        
        // Assert
        // BCrypt hashes should be 60 characters long
        assertEquals(60, hash.length());
        
        // Should start with BCrypt identifier
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));
        
        // Should not contain the original password
        assertFalse(hash.contains(password));
    }
}
