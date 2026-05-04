package com.pe.grademanagement.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * Teacher entity representing a PE teacher in the system.
 * Uses Google OAuth for authentication.
 */
@Entity
@Table(name = "teachers")
public class Teacher {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(unique = true)
    private String googleId;
    
    @Column(nullable = false)
    private String fullName;
    
    @Column
    private String picture;
    
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;
    
    @Column
    private Timestamp updatedAt;
    
    @OneToMany(mappedBy = "teacher")
    private List<Class> classes;
    
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Test> tests;
    
    // Constructors, getters, setters
    
    public Teacher() {
    }
    
    public Teacher(String email, String googleId, String fullName, String picture) {
        this.email = email;
        this.googleId = googleId;
        this.fullName = fullName;
        this.picture = picture;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getGoogleId() {
        return googleId;
    }
    
    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPicture() {
        return picture;
    }
    
    public void setPicture(String picture) {
        this.picture = picture;
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
    
    public List<Class> getClasses() {
        return classes;
    }
    
    public void setClasses(List<Class> classes) {
        this.classes = classes;
    }
    
    public List<Test> getTests() {
        return tests;
    }
    
    public void setTests(List<Test> tests) {
        this.tests = tests;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = new Timestamp(System.currentTimeMillis());
        updatedAt = new Timestamp(System.currentTimeMillis());
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
