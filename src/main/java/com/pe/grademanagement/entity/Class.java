package com.pe.grademanagement.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * Class entity representing a class/section in the PE Grade Management System.
 * This is a placeholder implementation to be completed in task 2.2.
 */
@Entity
@Table(name = "classes")
public class Class {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String gradeLevel;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;
    
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;
    
    @OneToMany(mappedBy = "classEntity", cascade = CascadeType.ALL)
    private List<Student> students;
    
    @OneToMany(mappedBy = "classEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestAssignment> testAssignments;
    
    // Constructors, getters, setters
    
    public Class() {
    }
    
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
    
    public String getGradeLevel() {
        return gradeLevel;
    }
    
    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }
    
    public Teacher getTeacher() {
        return teacher;
    }
    
    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public List<Student> getStudents() {
        return students;
    }
    
    public void setStudents(List<Student> students) {
        this.students = students;
    }
    
    public List<TestAssignment> getTestAssignments() {
        return testAssignments;
    }
    
    public void setTestAssignments(List<TestAssignment> testAssignments) {
        this.testAssignments = testAssignments;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Timestamp(System.currentTimeMillis());
    }
}
