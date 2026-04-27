package com.pe.grademanagement.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

/**
 * TestAssignment entity representing the assignment of a test to a class.
 * This is a join table that tracks which tests are assigned to which classes.
 * 
 * Requirements:
 * - 15: Support test assignment to classes
 * - Tests can be assigned at class or grade level
 */
@Entity
@Table(name = "test_assignments", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"test_id", "class_id"}))
public class TestAssignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The test being assigned.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;
    
    /**
     * The class to which the test is assigned.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private Class classEntity;
    
    /**
     * Timestamp when the test was assigned to the class.
     */
    @Column(nullable = false, updatable = false)
    private Timestamp assignedAt;
    
    /**
     * JPA lifecycle callback to set timestamp before persisting.
     */
    @PrePersist
    protected void onCreate() {
        assignedAt = new Timestamp(System.currentTimeMillis());
    }
    
    // Constructors
    
    public TestAssignment() {
    }
    
    public TestAssignment(Test test, Class classEntity) {
        this.test = test;
        this.classEntity = classEntity;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Test getTest() {
        return test;
    }
    
    public void setTest(Test test) {
        this.test = test;
    }
    
    public Class getClassEntity() {
        return classEntity;
    }
    
    public void setClassEntity(Class classEntity) {
        this.classEntity = classEntity;
    }
    
    public Timestamp getAssignedAt() {
        return assignedAt;
    }
    
    public void setAssignedAt(Timestamp assignedAt) {
        this.assignedAt = assignedAt;
    }
    
    // Utility methods
    
    @Override
    public String toString() {
        return "TestAssignment{" +
                "id=" + id +
                ", testId=" + (test != null ? test.getId() : null) +
                ", classId=" + (classEntity != null ? classEntity.getId() : null) +
                ", assignedAt=" + assignedAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        TestAssignment that = (TestAssignment) o;
        
        if (id != null && id.equals(that.id)) return true;
        
        // For uniqueness: compare by test and class
        return test != null && test.equals(that.test) &&
               classEntity != null && classEntity.equals(that.classEntity);
    }
    
    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        
        int result = test != null ? test.hashCode() : 0;
        result = 31 * result + (classEntity != null ? classEntity.hashCode() : 0);
        return result;
    }
}
