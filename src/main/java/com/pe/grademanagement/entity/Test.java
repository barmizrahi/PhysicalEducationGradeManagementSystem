package com.pe.grademanagement.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * Test entity representing a physical education test configuration.
 * Supports two calculation methods: RATIO and PENALTY.
 * 
 * Requirements:
 * - 3.1: Tests have name, calculation type, and unit type
 * - 3.2: Support RATIO and PENALTY calculation types
 * - 3.3: Support TIME and COUNT unit types
 * - 3.4: RATIO tests require maxValue parameter
 * - 3.5: PENALTY tests require targetValue and penaltyPerUnit parameters
 */
@Entity
@Table(name = "tests")
public class Test {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Test name (e.g., "1500m Run", "Push-ups", "Sit-ups").
     * Supports Hebrew characters.
     */
    @Column(nullable = false, length = 255)
    private String name;
    
    /**
     * Calculation type: RATIO or PENALTY.
     * Determines how grades are calculated from raw results.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CalculationType calculationType;
    
    /**
     * Unit type: TIME or COUNT.
     * Determines the measurement unit for raw results.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnitType unitType;
    
    /**
     * Maximum value for RATIO calculation.
     * Required when calculationType = RATIO.
     * Null when calculationType = PENALTY.
     * Example: 20 repetitions for 100% grade
     */
    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal maxValue;
    
    /**
     * Target value for PENALTY calculation.
     * Required when calculationType = PENALTY.
     * Null when calculationType = RATIO.
     * Example: 10 minutes for 100% grade
     */
    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal targetValue;
    
    /**
     * Penalty per unit for PENALTY calculation.
     * Required when calculationType = PENALTY.
     * Null when calculationType = RATIO.
     * Example: 5 points deducted per minute over target
     */
    @Column(nullable = true, precision = 10, scale = 4)
    private BigDecimal penaltyPerUnit;
    
    /**
     * Penalty unit for PENALTY calculation with TIME unit type.
     * Defines the time interval for penalty application.
     * Required when calculationType = PENALTY and unitType = TIME.
     * Null when calculationType = RATIO or unitType = COUNT.
     * Example: 0.75 (45 seconds) means penalty applies every 45 seconds
     * Default: 1.0 (1 minute) for backward compatibility
     */
    @Column(nullable = true, precision = 10, scale = 4)
    private BigDecimal penaltyUnit;
    
    /**
     * Teacher who created this test configuration.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Teacher createdBy;
    
    /**
     * Timestamp when the test was created.
     */
    @Column(nullable = false, updatable = false)
    private Timestamp createdAt;
    
    /**
     * Timestamp when the test was last updated.
     */
    @Column(nullable = false)
    private Timestamp updatedAt;
    
    /**
     * Test assignments to classes.
     */
    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestAssignment> assignments;
    
    /**
     * Test results from students.
     */
    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestResult> results;
    
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
    
    public Test() {
    }
    
    public Test(String name, CalculationType calculationType, UnitType unitType, Teacher createdBy) {
        this.name = name;
        this.calculationType = calculationType;
        this.unitType = unitType;
        this.createdBy = createdBy;
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
    
    public CalculationType getCalculationType() {
        return calculationType;
    }
    
    public void setCalculationType(CalculationType calculationType) {
        this.calculationType = calculationType;
    }
    
    public UnitType getUnitType() {
        return unitType;
    }
    
    public void setUnitType(UnitType unitType) {
        this.unitType = unitType;
    }
    
    public BigDecimal getMaxValue() {
        return maxValue;
    }
    
    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }
    
    public BigDecimal getTargetValue() {
        return targetValue;
    }
    
    public void setTargetValue(BigDecimal targetValue) {
        this.targetValue = targetValue;
    }
    
    public BigDecimal getPenaltyPerUnit() {
        return penaltyPerUnit;
    }
    
    public void setPenaltyPerUnit(BigDecimal penaltyPerUnit) {
        this.penaltyPerUnit = penaltyPerUnit;
    }
    
    public BigDecimal getPenaltyUnit() {
        return penaltyUnit;
    }
    
    public void setPenaltyUnit(BigDecimal penaltyUnit) {
        this.penaltyUnit = penaltyUnit;
    }
    
    public Teacher getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Teacher createdBy) {
        this.createdBy = createdBy;
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
    
    public List<TestAssignment> getAssignments() {
        return assignments;
    }
    
    public void setAssignments(List<TestAssignment> assignments) {
        this.assignments = assignments;
    }
    
    public List<TestResult> getResults() {
        return results;
    }
    
    public void setResults(List<TestResult> results) {
        this.results = results;
    }
    
    // Utility methods
    
    /**
     * Validates that the test configuration is complete and consistent.
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (calculationType == null || unitType == null || createdBy == null) {
            return false;
        }
        
        // Validate RATIO configuration
        if (calculationType == CalculationType.RATIO) {
            return maxValue != null && maxValue.compareTo(BigDecimal.ZERO) > 0;
        }
        
        // Validate PENALTY configuration
        if (calculationType == CalculationType.PENALTY) {
            boolean basicValid = targetValue != null && targetValue.compareTo(BigDecimal.ZERO) > 0
                    && penaltyPerUnit != null && penaltyPerUnit.compareTo(BigDecimal.ZERO) > 0;
            
            // For TIME unit type, penaltyUnit is required (default to 1.0 if missing for backward compatibility)
            if (unitType == UnitType.TIME && penaltyUnit == null) {
                penaltyUnit = BigDecimal.ONE;
            }
            
            return basicValid;
        }
        
        return false;
    }
    
    @Override
    public String toString() {
        return "Test{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", calculationType=" + calculationType +
                ", unitType=" + unitType +
                ", maxValue=" + maxValue +
                ", targetValue=" + targetValue +
                ", penaltyPerUnit=" + penaltyPerUnit +
                ", createdBy=" + (createdBy != null ? createdBy.getId() : null) +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Test test = (Test) o;
        
        return id != null && id.equals(test.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
