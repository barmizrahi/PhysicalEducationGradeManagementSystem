package com.pe.grademanagement.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * Student entity representing a student in the PE Grade Management System.
 * Supports Hebrew characters in the name field for Israeli students.
 * Student ID is optional to accommodate cases where it's not available during import.
 */
@Entity
@Table(name = "students")
public class Student {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Student's full name. Supports Hebrew characters.
     */
    @Column(nullable = false, length = 255)
    private String name;
    
    /**
     * Optional student ID for duplicate detection.
     * When available, used as primary identifier for detecting existing students.
     * When null, name+class combination is used for duplicate detection.
     */
    @Column(nullable = true, length = 50)
    private String studentId;
    
    /**
     * Grade level in Hebrew format: י (10th), יא (11th), יב (12th)
     */
    @Column(nullable = false, length = 10)
    private String gradeLevel;
    
    /**
     * Many-to-one relationship with Class entity.
     * Each student belongs to exactly one class.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Class classEntity;
    
    /**
     * Timestamp when the student record was created.
     */
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;
    
    /**
     * Timestamp when the student record was last updated.
     */
    @Column(nullable = false)
    private Timestamp updatedAt;
    
    /**
     * One-to-many relationship with TestResult entity.
     * A student can have multiple test results.
     */
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestResult> testResults;
    
    /**
     * JPA lifecycle callback to set timestamps before persisting.
     */
    @PrePersist
    protected void onCreate() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        createdAt = now;
        updatedAt = now;
    }
    
    /**
     * JPA lifecycle callback to update timestamp before updating.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
    }
    
    // Constructors
    
    public Student() {
    }
    
    public Student(String name, String studentId, String gradeLevel, Class classEntity) {
        this.name = name;
        this.studentId = studentId;
        this.gradeLevel = gradeLevel;
        this.classEntity = classEntity;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getStudentId() {
        return studentId;
    }
    
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
    
    public String getGradeLevel() {
        return gradeLevel;
    }
    
    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }
    
    public Class getClassEntity() {
        return classEntity;
    }
    
    public void setClassEntity(Class classEntity) {
        this.classEntity = classEntity;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<TestResult> getTestResults() {
        return testResults;
    }
    
    public void setTestResults(List<TestResult> testResults) {
        this.testResults = testResults;
    }
    
    // Utility methods
    
    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", studentId='" + studentId + '\'' +
                ", gradeLevel='" + gradeLevel + '\'' +
                ", classId=" + (classEntity != null ? classEntity.getId() : null) +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Student student = (Student) o;
        
        if (id != null && id.equals(student.id)) return true;
        
        // For duplicate detection: compare by studentId if available, otherwise by name+class
        if (studentId != null && student.studentId != null) {
            return studentId.equals(student.studentId);
        }
        
        return name.equals(student.name) && 
               classEntity != null && 
               classEntity.equals(student.classEntity);
    }
    
    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        
        if (studentId != null) {
            return studentId.hashCode();
        }
        
        int result = name.hashCode();
        result = 31 * result + (classEntity != null ? classEntity.hashCode() : 0);
        return result;
    }
}
