package com.pe.grademanagement.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtil.
 * Tests JWT token generation, parsing, and validation.
 * 
 * Requirements:
 * - 13.1: JWT token generation and validation for teacher authentication
 */
class JwtUtilTest {
    
    private JwtUtil jwtUtil;
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        // Set test values using reflection
        ReflectionTestUtils.setField(jwtUtil, "secret", "testSecretKeyForJwtTokenGenerationAndValidation1234567890");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L); // 1 hour
    }
    
    @Test
    void testGenerateToken() {
        // Arrange
        String username = "test.teacher";
        Long teacherId = 1L;
        
        // Act
        String token = jwtUtil.generateToken(username, teacherId);
        
        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts separated by dots
    }
    
    @Test
    void testExtractUsername() {
        // Arrange
        String username = "test.teacher";
        Long teacherId = 1L;
        String token = jwtUtil.generateToken(username, teacherId);
        
        // Act
        String extractedUsername = jwtUtil.extractUsername(token);
        
        // Assert
        assertEquals(username, extractedUsername);
    }
    
    @Test
    void testExtractTeacherId() {
        // Arrange
        String username = "test.teacher";
        Long teacherId = 42L;
        String token = jwtUtil.generateToken(username, teacherId);
        
        // Act
        Long extractedTeacherId = jwtUtil.extractTeacherId(token);
        
        // Assert
        assertEquals(teacherId, extractedTeacherId);
    }
    
    @Test
    void testExtractExpiration() {
        // Arrange
        String username = "test.teacher";
        Long teacherId = 1L;
        Date beforeGeneration = new Date();
        String token = jwtUtil.generateToken(username, teacherId);
        Date afterGeneration = new Date();
        
        // Act
        Date expiration = jwtUtil.extractExpiration(token);
        
        // Assert
        assertNotNull(expiration);
        // Expiration should be approximately 1 hour from now (3600000 ms)
        long expectedExpiration = beforeGeneration.getTime() + 3600000L;
        long actualExpiration = expiration.getTime();
        assertTrue(Math.abs(actualExpiration - expectedExpiration) < 1000); // Within 1 second tolerance
    }
    
    @Test
    void testValidateTokenWithValidToken() {
        // Arrange
        String username = "test.teacher";
        Long teacherId = 1L;
        String token = jwtUtil.generateToken(username, teacherId);
        
        // Act
        Boolean isValid = jwtUtil.validateToken(token, username);
        
        // Assert
        assertTrue(isValid);
    }
    
    @Test
    void testValidateTokenWithWrongUsername() {
        // Arrange
        String username = "test.teacher";
        Long teacherId = 1L;
        String token = jwtUtil.generateToken(username, teacherId);
        
        // Act
        Boolean isValid = jwtUtil.validateToken(token, "wrong.teacher");
        
        // Assert
        assertFalse(isValid);
    }
    
    @Test
    void testValidateTokenWithExpiredToken() {
        // Arrange
        String username = "test.teacher";
        Long teacherId = 1L;
        
        // Set expiration to -1 ms (already expired)
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1L);
        String token = jwtUtil.generateToken(username, teacherId);
        
        // Reset expiration to normal value
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);
        
        // Act & Assert
        assertThrows(ExpiredJwtException.class, () -> {
            jwtUtil.validateToken(token, username);
        });
    }
    
    @Test
    void testTokenContainsIssuedAtClaim() {
        // Arrange
        String username = "test.teacher";
        Long teacherId = 1L;
        
        // Act
        String token = jwtUtil.generateToken(username, teacherId);
        Date issuedAt = jwtUtil.extractClaim(token, Claims::getIssuedAt);
        Date now = new Date();
        
        // Assert
        assertNotNull(issuedAt);
        // IssuedAt should be within the last few seconds (allow 5 second tolerance)
        long timeDifference = now.getTime() - issuedAt.getTime();
        assertTrue(timeDifference >= 0 && timeDifference < 5000, 
                "IssuedAt should be within the last 5 seconds");
    }
    
    @Test
    void testMultipleTokensForSameUserAreDifferent() {
        // Arrange
        String username = "test.teacher";
        Long teacherId = 1L;
        
        // Act
        String token1 = jwtUtil.generateToken(username, teacherId);
        // Longer delay to ensure different issuedAt timestamp (JWT uses seconds precision)
        try {
            Thread.sleep(1100); // Sleep for more than 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String token2 = jwtUtil.generateToken(username, teacherId);
        
        // Assert
        assertNotEquals(token1, token2, "Tokens generated at different times should be different");
        
        // But both should be valid for the same user
        assertTrue(jwtUtil.validateToken(token1, username));
        assertTrue(jwtUtil.validateToken(token2, username));
    }
    
    @Test
    void testTokensForDifferentUsersAreDifferent() {
        // Arrange & Act
        String token1 = jwtUtil.generateToken("teacher1", 1L);
        String token2 = jwtUtil.generateToken("teacher2", 2L);
        
        // Assert
        assertNotEquals(token1, token2);
        
        // Each token should only be valid for its own user
        assertTrue(jwtUtil.validateToken(token1, "teacher1"));
        assertFalse(jwtUtil.validateToken(token1, "teacher2"));
        assertTrue(jwtUtil.validateToken(token2, "teacher2"));
        assertFalse(jwtUtil.validateToken(token2, "teacher1"));
    }
    
    @Test
    void testExtractTeacherIdFromDifferentTeachers() {
        // Arrange & Act
        String token1 = jwtUtil.generateToken("teacher1", 100L);
        String token2 = jwtUtil.generateToken("teacher2", 200L);
        
        Long teacherId1 = jwtUtil.extractTeacherId(token1);
        Long teacherId2 = jwtUtil.extractTeacherId(token2);
        
        // Assert
        assertEquals(100L, teacherId1);
        assertEquals(200L, teacherId2);
    }
    
    @Test
    void testTokenStructure() {
        // Arrange
        String username = "test.teacher";
        Long teacherId = 1L;
        
        // Act
        String token = jwtUtil.generateToken(username, teacherId);
        
        // Assert
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts: header, payload, signature");
        
        // Each part should be non-empty
        assertTrue(parts[0].length() > 0, "Header should not be empty");
        assertTrue(parts[1].length() > 0, "Payload should not be empty");
        assertTrue(parts[2].length() > 0, "Signature should not be empty");
    }
}
