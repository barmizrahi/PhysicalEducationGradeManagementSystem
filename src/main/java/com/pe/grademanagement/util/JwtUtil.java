package com.pe.grademanagement.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for JWT token generation and validation.
 * Provides methods to create, parse, and validate JWT tokens for teacher authentication.
 * 
 * Requirements:
 * - 13.1: Generate JWT tokens for authenticated teachers
 */
@Component
public class JwtUtil {
    
    @Value("${jwt.secret:defaultSecretKeyForDevelopmentOnlyPleaseChangeInProduction}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}") // Default: 24 hours in milliseconds
    private Long expiration;
    
    /**
     * Generate a secret key from the configured secret string.
     * 
     * @return SecretKey for signing JWT tokens
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    
    /**
     * Generate JWT token for a teacher.
     * 
     * @param email the teacher's email address
     * @param teacherId the teacher's ID
     * @return JWT token string
     */
    public String generateToken(String email, Long teacherId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("teacherId", teacherId);
        claims.put("email", email);
        return createToken(claims, email);
    }
    
    /**
     * Create JWT token with claims and subject.
     * 
     * @param claims additional claims to include in the token
     * @param subject the subject (username) of the token
     * @return JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    /**
     * Extract email from JWT token.
     * 
     * @param token JWT token string
     * @return email (subject) from the token
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extract username from JWT token (alias for extractEmail for backward compatibility).
     * 
     * @param token JWT token string
     * @return email (subject) from the token
     */
    public String extractUsername(String token) {
        return extractEmail(token);
    }
    
    /**
     * Extract teacher ID from JWT token.
     * 
     * @param token JWT token string
     * @return teacher ID from the token claims
     */
    public Long extractTeacherId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("teacherId", Long.class);
    }
    
    /**
     * Extract expiration date from JWT token.
     * 
     * @param token JWT token string
     * @return expiration date from the token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extract a specific claim from JWT token.
     * 
     * @param token JWT token string
     * @param claimsResolver function to extract the desired claim
     * @param <T> type of the claim
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extract all claims from JWT token.
     * 
     * @param token JWT token string
     * @return all claims from the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Check if JWT token is expired.
     * 
     * @param token JWT token string
     * @return true if token is expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * Validate JWT token against email.
     * 
     * @param token JWT token string
     * @param email email to validate against
     * @return true if token is valid for the email, false otherwise
     */
    public Boolean validateToken(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail.equals(email) && !isTokenExpired(token));
    }
}
