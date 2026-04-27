package com.pe.grademanagement.util;

/**
 * Configuration class for mapping Excel columns to Student entity fields.
 * Supports flexible column mapping to accommodate different Excel file formats.
 */
public class ColumnMapping {
    
    private Integer nameColumn;
    private Integer studentIdColumn;
    private Integer gradeLevelColumn;
    private Integer classNameColumn;
    
    /**
     * Default constructor.
     */
    public ColumnMapping() {
    }
    
    /**
     * Constructor with all column mappings.
     * 
     * @param nameColumn Column index for student name (0-based)
     * @param studentIdColumn Column index for student ID (0-based, nullable)
     * @param gradeLevelColumn Column index for grade level (0-based)
     * @param classNameColumn Column index for class name (0-based)
     */
    public ColumnMapping(Integer nameColumn, Integer studentIdColumn, 
                        Integer gradeLevelColumn, Integer classNameColumn) {
        this.nameColumn = nameColumn;
        this.studentIdColumn = studentIdColumn;
        this.gradeLevelColumn = gradeLevelColumn;
        this.classNameColumn = classNameColumn;
    }
    
    // Getters and Setters
    
    public Integer getNameColumn() {
        return nameColumn;
    }
    
    public void setNameColumn(Integer nameColumn) {
        this.nameColumn = nameColumn;
    }
    
    public Integer getStudentIdColumn() {
        return studentIdColumn;
    }
    
    public void setStudentIdColumn(Integer studentIdColumn) {
        this.studentIdColumn = studentIdColumn;
    }
    
    public Integer getGradeLevelColumn() {
        return gradeLevelColumn;
    }
    
    public void setGradeLevelColumn(Integer gradeLevelColumn) {
        this.gradeLevelColumn = gradeLevelColumn;
    }
    
    public Integer getClassNameColumn() {
        return classNameColumn;
    }
    
    public void setClassNameColumn(Integer classNameColumn) {
        this.classNameColumn = classNameColumn;
    }
    
    /**
     * Validates that all required column mappings are present.
     * Student ID column is optional.
     * 
     * @return true if all required columns are mapped, false otherwise
     */
    public boolean isValid() {
        return nameColumn != null && 
               gradeLevelColumn != null && 
               classNameColumn != null;
    }
    
    @Override
    public String toString() {
        return "ColumnMapping{" +
                "nameColumn=" + nameColumn +
                ", studentIdColumn=" + studentIdColumn +
                ", gradeLevelColumn=" + gradeLevelColumn +
                ", classNameColumn=" + classNameColumn +
                '}';
    }
}
