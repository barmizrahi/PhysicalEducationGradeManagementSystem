package com.pe.grademanagement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * TestResult entity representing a student's result on a specific test.
 * Stores both the raw result (actual performance) and calculated grade (0-100).
 * 
 * Requirements:
 * - 7.1: Store student ID, test ID, raw result, calculated grade, and notes
 * - 7.2: Support updating existing test results
 * - 7.4: Preserve creation and modification timestamps
 * - 8.1: Support null rawResult with calculatedGrade = 0
 * - 8.2: Allow notes without raw result (e.g., "not tested", "was injured")
 */
@Entity
@Table(name = "test_results",
       uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "test_id"}))
public class TestResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * The student who took the test.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    /**
     * The test that was taken.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;
    
    /**
     * Raw test result (actual performance measurement).
     * Nullable - null indicates student did not take the test.
     * For TIME tests: decimal minutes (e.g., 10.5)
     * For COUNT tests: number of repetitions (e.g., 15.5)
     */
    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal rawResult;
    
    /**
     * Calculated grade (0-100).
     * Always non-null. Set to 0 when rawResult is null.
     * Rounded to 2 decimal places.
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal calculatedGrade;
    
    /**
     * Optional notes about the test result.
     * Examples: "not tested", "was injured", "excellent performance"
     * Supports Hebrew characters.
     */
    @Column(nullable = true, length = 500)
    private String notes;
    
    /**
     * Timestamp when the test result was created.
     */
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;
    
    /**
     * Timestamp when the test result was last updated.
     */
    @Column(nullable = false)
    private Timestamp updatedAt;
    
    /**
     * JPA lifecycle callback to set timestamps before persisting.
     */
    @PrePersist
    protected void onCreate() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        createdAt = now;
        updatedAt = now;
        
        // Ensure calculatedGrade is never null
        if (calculatedGrade == null) {
            calculatedGrade = BigDecimal.ZERO;
        }
    }
    
    /**
     * JPA lifecycle callback to update timestamp before updating.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
        
        // Ensure calculatedGrade is never null
        if (calculatedGrade == null) {
            calculatedGrade = BigDecimal.ZERO;
        }
    }
    
    // Constructors
    
    public TestResult() {
    }
    
    public TestResult(Student student, Test test, BigDecimal rawResult, BigDecimal calculatedGrade) {
        this.student = student;
        this.test = test;
        this.rawResult = rawResult;
        this.calculatedGrade = calculatedGrade != null ? calculatedGrade : BigDecimal.ZERO;
    }
    
    public TestResult(Student student, Test test, BigDecimal rawResult, BigDecimal calculatedGrade, String notes) {
        this.student = student;
        this.test = test;
        this.rawResult = rawResult;
        this.calculatedGrade = calculatedGrade != null ? calculatedGrade : BigDecimal.ZERO;
        this.notes = notes;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Student getStudent() {
        return student;
    }
    
    public void setStudent(Student student) {
        this.student = student;
    }
    
    public Test getTest() {
        return test;
    }
    
    public void setTest(Test test) {
        this.test = test;
    }
    
    public BigDecimal getRawResult() {
        return rawResult;
    }
    
    public void setRawResult(BigDecimal rawResult) {
        this.rawResult = rawResult;
    }
    
    public BigDecimal getCalculatedGrade() {
        return calculatedGrade;
    }
    
    public void setCalculatedGrade(BigDecimal calculatedGrade) {
        this.calculatedGrade = calculatedGrade != null ? calculatedGrade : BigDecimal.ZERO;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
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
    
    // Utility methods
    
    /**
     * Checks if the student took the test (has a raw result).
     * @return true if rawResult is not null, false otherwise
     */
    public boolean hasRawResult() {
        return rawResult != null;
    }
    
    /**
     * Checks if the result has notes.
     * @return true if notes is not null and not empty, false otherwise
     */
    public boolean hasNotes() {
        return notes != null && !notes.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return "TestResult{" +
                "id=" + id +
                ", studentId=" + (student != null ? student.getId() : null) +
                ", testId=" + (test != null ? test.getId() : null) +
                ", rawResult=" + rawResult +
                ", calculatedGrade=" + calculatedGrade +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        TestResult that = (TestResult) o;
        
        if (id != null && id.equals(that.id)) return true;
        
        // For uniqueness: compare by student and test
        return student != null && student.equals(that.student) &&
               test != null && test.equals(that.test);
    }
    
    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        
        int result = student != null ? student.hashCode() : 0;
        result = 31 * result + (test != null ? test.hashCode() : 0);
        return result;
    }
}
