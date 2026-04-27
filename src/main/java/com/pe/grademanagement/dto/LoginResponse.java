package com.pe.grademanagement.dto;

/**
 * Data Transfer Object for login responses.
 * Contains JWT token and teacher information after successful authentication.
 * 
 * Requirements:
 * - 13.1: Return JWT token to authenticated teachers
 */
public class LoginResponse {
    
    private String token;
    private Long teacherId;
    private String username;
    private String fullName;
    
    // Constructors
    
    public LoginResponse() {
    }
    
    public LoginResponse(String token, Long teacherId, String username, String fullName) {
        this.token = token;
        this.teacherId = teacherId;
        this.username = username;
        this.fullName = fullName;
    }
    
    // Getters and setters
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public Long getTeacherId() {
        return teacherId;
    }
    
    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
