package com.pe.grademanagement.util;

import com.pe.grademanagement.entity.Student;
import java.util.ArrayList;
import java.util.List;

/**
 * Result of student import operation.
 * Contains lists of created/updated students and any errors encountered.
 */
public class ImportResult {
    
    private List<Student> createdStudents;
    private List<Student> updatedStudents;
    private List<String> errors;
    private boolean success;
    
    /**
     * Default constructor.
     */
    public ImportResult() {
        this.createdStudents = new ArrayList<>();
        this.updatedStudents = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.success = true;
    }
    
    /**
     * Adds a created student to the result.
     * 
     * @param student Student that was created
     */
    public void addCreatedStudent(Student student) {
        this.createdStudents.add(student);
    }
    
    /**
     * Adds an updated student to the result.
     * 
     * @param student Student that was updated
     */
    public void addUpdatedStudent(Student student) {
        this.updatedStudents.add(student);
    }
    
    /**
     * Adds an error message and marks the import as failed.
     * 
     * @param error Error message to add
     */
    public void addError(String error) {
        this.errors.add(error);
        this.success = false;
    }
    
    /**
     * Gets the total number of students processed (created + updated).
     * 
     * @return Total number of students
     */
    public int getTotalStudents() {
        return createdStudents.size() + updatedStudents.size();
    }
    
    /**
     * Checks if the import was successful.
     * 
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * Sets the success status.
     * 
     * @param success Success status
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    /**
     * Gets the list of created students.
     * 
     * @return List of created students
     */
    public List<Student> getCreatedStudents() {
        return createdStudents;
    }
    
    /**
     * Sets the list of created students.
     * 
     * @param createdStudents List of created students
     */
    public void setCreatedStudents(List<Student> createdStudents) {
        this.createdStudents = createdStudents;
    }
    
    /**
     * Gets the list of updated students.
     * 
     * @return List of updated students
     */
    public List<Student> getUpdatedStudents() {
        return updatedStudents;
    }
    
    /**
     * Sets the list of updated students.
     * 
     * @param updatedStudents List of updated students
     */
    public void setUpdatedStudents(List<Student> updatedStudents) {
        this.updatedStudents = updatedStudents;
    }
    
    /**
     * Gets all error messages.
     * 
     * @return List of error messages
     */
    public List<String> getErrors() {
        return errors;
    }
    
    /**
     * Sets the error messages.
     * 
     * @param errors List of error messages
     */
    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
    
    @Override
    public String toString() {
        return "ImportResult{" +
                "createdStudents=" + createdStudents.size() +
                ", updatedStudents=" + updatedStudents.size() +
                ", errors=" + errors.size() +
                ", success=" + success +
                '}';
    }
}
