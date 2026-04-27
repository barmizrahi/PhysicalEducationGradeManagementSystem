package com.pe.grademanagement.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO for assigning tests to classes.
 * Used in TestController assign endpoint.
 */
public class TestAssignmentRequest {
    
    @NotEmpty(message = "At least one class ID is required")
    private List<@NotNull Long> classIds;
    
    // Constructors
    
    public TestAssignmentRequest() {
    }
    
    public TestAssignmentRequest(List<Long> classIds) {
        this.classIds = classIds;
    }
    
    // Getters and Setters
    
    public List<Long> getClassIds() {
        return classIds;
    }
    
    public void setClassIds(List<Long> classIds) {
        this.classIds = classIds;
    }
}
