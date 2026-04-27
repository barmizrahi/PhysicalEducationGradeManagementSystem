package com.pe.grademanagement.util;

import java.util.List;

/**
 * Configuration class for Excel export operations.
 * Specifies which classes, tests, and options to include in the export.
 * 
 * Requirements:
 * - 9.3: Support optional notes inclusion
 * - 9.4: Allow selection of which tests to include
 * - 9.5: Allow selection of which classes to include
 */
public class ExportConfig {
    
    /**
     * List of class IDs to include in the export.
     * If null or empty, all classes are included.
     */
    private List<Long> classIds;
    
    /**
     * List of test IDs to include in the export.
     * If null or empty, all tests are included.
     */
    private List<Long> testIds;
    
    /**
     * Whether to include the notes column in the export.
     * Default: false
     */
    private boolean includeNotes;
    
    // Constructors
    
    public ExportConfig() {
        this.includeNotes = false;
    }
    
    public ExportConfig(List<Long> classIds, List<Long> testIds, boolean includeNotes) {
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
    
    // Utility methods
    
    /**
     * Checks if specific classes are selected.
     * @return true if classIds is not null and not empty
     */
    public boolean hasClassFilter() {
        return classIds != null && !classIds.isEmpty();
    }
    
    /**
     * Checks if specific tests are selected.
     * @return true if testIds is not null and not empty
     */
    public boolean hasTestFilter() {
        return testIds != null && !testIds.isEmpty();
    }
    
    /**
     * Checks if a class ID is included in the export.
     * @param classId Class ID to check
     * @return true if the class should be included
     */
    public boolean includesClass(Long classId) {
        if (!hasClassFilter()) {
            return true; // No filter means include all
        }
        return classIds.contains(classId);
    }
    
    /**
     * Checks if a test ID is included in the export.
     * @param testId Test ID to check
     * @return true if the test should be included
     */
    public boolean includesTest(Long testId) {
        if (!hasTestFilter()) {
            return true; // No filter means include all
        }
        return testIds.contains(testId);
    }
    
    @Override
    public String toString() {
        return "ExportConfig{" +
                "classIds=" + classIds +
                ", testIds=" + testIds +
                ", includeNotes=" + includeNotes +
                '}';
    }
}
