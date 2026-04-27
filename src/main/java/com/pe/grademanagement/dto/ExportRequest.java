package com.pe.grademanagement.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO for exporting grades to Excel.
 * Used in ExportController endpoint.
 */
public class ExportRequest {
    
    @NotEmpty(message = "At least one class ID is required")
    private List<@NotNull Long> classIds;
    
    @NotEmpty(message = "At least one test ID is required")
    private List<@NotNull Long> testIds;
    
    private boolean includeNotes = false;
    
    // Constructors
    
    public ExportRequest() {
    }
    
    public ExportRequest(List<Long> classIds, List<Long> testIds) {
        this.classIds = classIds;
        this.testIds = testIds;
    }
    
    public ExportRequest(List<Long> classIds, List<Long> testIds, boolean includeNotes) {
        this.classIds = classIds;
        this.testIds = testIds;
        this.includeNotes = includeNotes;
    }
    
    // Getters and Setters
    
    public List<Long> getClassIds() {
        return classIds;
    }
    
    public void setClassIds(List<Long> classIds) {
        this.classIds = classIds;
    }
    
    public List<Long> getTestIds() {
        return testIds;
    }
    
    public void setTestIds(List<Long> testIds) {
        this.testIds = testIds;
    }
    
    public boolean isIncludeNotes() {
        return includeNotes;
    }
    
    public void setIncludeNotes(boolean includeNotes) {
        this.includeNotes = includeNotes;
    }
}
