package com.pe.grademanagement.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO for saving test results.
 * Used in GradeController endpoints.
 */
public class TestResultRequest {
    
    @NotNull(message = "Student ID is required")
    private Long studentId;
    
    @NotNull(message = "Test ID is required")
    private Long testId;
    
    private BigDecimal rawResult;
    private String notes;
    
    // Constructors
    
    public TestResultRequest() {
    }
    
    public TestResultRequest(Long studentId, Long testId, BigDecimal rawResult) {
        this.studentId = studentId;
        this.testId = testId;
        this.rawResult = rawResult;
    }
    
    public TestResultRequest(Long studentId, Long testId, BigDecimal rawResult, String notes) {
        this.studentId = studentId;
        this.testId = testId;
        this.rawResult = rawResult;
        this.notes = notes;
    }
    
    // Getters and Setters
    
    public Long getStudentId() {
        return studentId;
    }
    
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }
    
    public Long getTestId() {
        return testId;
    }
    
    public void setTestId(Long testId) {
        this.testId = testId;
    }
    
    public BigDecimal getRawResult() {
        return rawResult;
    }
    
    public void setRawResult(BigDecimal rawResult) {
        this.rawResult = rawResult;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
