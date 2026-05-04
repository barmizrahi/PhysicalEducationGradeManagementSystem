package com.pe.grademanagement.exception;

/**
 * Exception thrown when a requested resource is not found.
 * 
 * Used for cases like:
 * - Student not found by ID
 * - Test not found by ID
 * - Class not found by ID
 * 
 * Requirements:
 * - Error Handling: Handle resource not found errors with descriptive messages
 */
public class ResourceNotFoundException extends RuntimeException {
    
    private final String resourceType;
    private final Object resourceId;
    
    public ResourceNotFoundException(String resourceType, Object resourceId) {
        super(String.format("%s not found with id: %s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    public ResourceNotFoundException(String message) {
        super(message);
        this.resourceType = "Resource";
        this.resourceId = null;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public Object getResourceId() {
        return resourceId;
    }
}
