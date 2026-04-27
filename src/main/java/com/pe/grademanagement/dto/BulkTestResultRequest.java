package com.pe.grademanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO for bulk saving test results.
 * Used in GradeController bulk save endpoint.
 */
public class BulkTestResultRequest {
    
    @NotEmpty(message = "At least one test result is required")
    private List<@Valid TestResultRequest> results;
    
    // Constructors
    
    public BulkTestResultRequest() {
    }
    
    public BulkTestResultRequest(List<TestResultRequest> results) {
        this.results = results;
    }
    
    // Getters and Setters
    
    public List<TestResultRequest> getResults() {
        return results;
    }
    
    public void setResults(List<TestResultRequest> results) {
        this.results = results;
    }
}
