package com.pe.grademanagement.dto;

import com.pe.grademanagement.entity.CalculationType;
import com.pe.grademanagement.entity.UnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO for creating or updating test configurations.
 * Used in TestController endpoints.
 */
public class TestRequest {
    
    @NotBlank(message = "Test name is required")
    private String name;
    
    @NotNull(message = "Calculation type is required")
    private CalculationType calculationType;
    
    @NotNull(message = "Unit type is required")
    private UnitType unitType;
    
    private BigDecimal maxValue;
    private BigDecimal targetValue;
    private BigDecimal penaltyPerUnit;
    private BigDecimal penaltyUnit;
    
    // Constructors
    
    public TestRequest() {
    }
    
    public TestRequest(String name, CalculationType calculationType, UnitType unitType) {
        this.name = name;
        this.calculationType = calculationType;
        this.unitType = unitType;
    }
    
    // Getters and Setters
    
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
}
