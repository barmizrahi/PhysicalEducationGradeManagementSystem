package com.pe.grademanagement.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Component for importing student data from Excel files.
 * Uses Apache POI to parse Excel files and extract student information.
 * Supports flexible column mapping and Hebrew characters.
 */
@Component
public class ExcelImporter {
    
    private static final Set<String> VALID_GRADE_LEVELS = Set.of("י", "יא", "יב");
    private static final int MAX_ROWS = 10000; // Safety limit to prevent memory issues
    
    /**
     * Validates the format of an Excel file.
     * Checks if the file is a valid Excel file and can be opened.
     * 
     * @param file Excel file to validate
     * @return ValidationResult with errors if any
     */
    public ValidationResult validateExcelFormat(MultipartFile file) {
        ValidationResult result = new ValidationResult();
        
        // Check if file is null or empty
        if (file == null || file.isEmpty()) {
            result.addError("File is empty or not provided");
            return result;
        }
        
        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            result.addError("Invalid Excel file format. Please upload a .xlsx file.");
            return result;
        }
        
        // Try to open the file
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            // Check if workbook has at least one sheet
            if (workbook.getNumberOfSheets() == 0) {
                result.addError("Excel file contains no sheets");
                return result;
            }
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // Check if sheet has at least one row
            if (sheet.getPhysicalNumberOfRows() == 0) {
                result.addError("Excel sheet is empty");
                return result;
            }
            
        } catch (IOException e) {
            result.addError("Failed to read Excel file: " + e.getMessage());
        } catch (Exception e) {
            result.addError("Invalid Excel file format: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Imports students from an Excel file.
     * Parses the file according to the provided column mapping and extracts student data.
     * Note: This method only parses the Excel file and returns StudentData objects.
     * The service layer is responsible for creating Student entities and handling database operations.
     * 
     * @param file Excel file containing student data
     * @param columnMapping Mapping of Excel columns to Student fields
     * @return List of StudentData objects parsed from the file
     * @throws InvalidExcelFormatException if the file format is invalid
     */
    public List<StudentData> importStudents(MultipartFile file, ColumnMapping columnMapping) 
            throws InvalidExcelFormatException {
        
        List<StudentData> students = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        // Validate column mapping
        if (columnMapping == null || !columnMapping.isValid()) {
            throw new InvalidExcelFormatException(
                "Invalid column mapping. Required columns: name, gradeLevel, className");
        }
        
        // Validate file format
        ValidationResult validation = validateExcelFormat(file);
        if (!validation.isValid()) {
            throw new InvalidExcelFormatException(validation.getErrorMessage());
        }
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // Parse rows (skip header row if present)
            int rowCount = 0;
            for (Row row : sheet) {
                // Skip first row (assumed to be header)
                if (row.getRowNum() == 0) {
                    continue;
                }
                
                // Safety check to prevent processing too many rows
                if (rowCount++ > MAX_ROWS) {
                    errors.add("File contains too many rows (max: " + MAX_ROWS + ")");
                    break;
                }
                
                try {
                    StudentData studentData = parseRow(row, columnMapping);
                    
                    // Validate student data
                    String validationError = validateStudentData(studentData, row.getRowNum() + 1);
                    if (validationError != null) {
                        errors.add(validationError);
                        continue;
                    }
                    
                    students.add(studentData);
                    
                } catch (Exception e) {
                    errors.add("Error parsing row " + (row.getRowNum() + 1) + ": " + e.getMessage());
                }
            }
            
            // If there were errors, throw exception with all error messages
            if (!errors.isEmpty()) {
                throw new InvalidExcelFormatException(String.join("; ", errors));
            }
            
        } catch (IOException e) {
            throw new InvalidExcelFormatException("Failed to read Excel file", e);
        }
        
        return students;
    }
    
    /**
     * Parses a single row from the Excel sheet.
     * 
     * @param row Excel row to parse
     * @param columnMapping Column mapping configuration
     * @return StudentData extracted from the row
     */
    private StudentData parseRow(Row row, ColumnMapping columnMapping) {
        StudentData data = new StudentData();
        
        // Extract name
        data.name = getCellValueAsString(row.getCell(columnMapping.getNameColumn()));
        
        // Extract student ID (optional)
        if (columnMapping.getStudentIdColumn() != null) {
            data.studentId = getCellValueAsString(row.getCell(columnMapping.getStudentIdColumn()));
            // Treat empty string as null
            if (data.studentId != null && data.studentId.trim().isEmpty()) {
                data.studentId = null;
            }
        }
        
        // Extract grade level
        data.gradeLevel = getCellValueAsString(row.getCell(columnMapping.getGradeLevelColumn()));
        
        // Extract class name
        data.className = getCellValueAsString(row.getCell(columnMapping.getClassNameColumn()));
        
        return data;
    }
    
    /**
     * Extracts cell value as a string, handling different cell types.
     * 
     * @param cell Excel cell
     * @return String value of the cell, or null if cell is null or blank
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            
            case NUMERIC:
                // Handle numeric values (e.g., student IDs entered as numbers)
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Format as integer if it's a whole number, otherwise as decimal
                    double numValue = cell.getNumericCellValue();
                    if (numValue == Math.floor(numValue)) {
                        return String.valueOf((long) numValue);
                    } else {
                        return String.valueOf(numValue);
                    }
                }
            
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            
            case FORMULA:
                // Evaluate formula and get the result
                try {
                    return cell.getStringCellValue().trim();
                } catch (IllegalStateException e) {
                    // If formula result is not a string, try numeric
                    return String.valueOf(cell.getNumericCellValue());
                }
            
            case BLANK:
                return null;
            
            default:
                return null;
        }
    }
    
    /**
     * Validates student data extracted from a row.
     * 
     * @param data Student data to validate
     * @param rowNumber Row number for error reporting (1-based)
     * @return Error message if validation fails, null if valid
     */
    private String validateStudentData(StudentData data, int rowNumber) {
        // Check required fields
        if (data.name == null || data.name.trim().isEmpty()) {
            return "Row " + rowNumber + ": Student name is required";
        }
        
        if (data.gradeLevel == null || data.gradeLevel.trim().isEmpty()) {
            return "Row " + rowNumber + ": Grade level is required";
        }
        
        if (data.className == null || data.className.trim().isEmpty()) {
            return "Row " + rowNumber + ": Class name is required";
        }
        
        // Validate grade level
        if (!VALID_GRADE_LEVELS.contains(data.gradeLevel.trim())) {
            return "Row " + rowNumber + ": Invalid grade level '" + data.gradeLevel + 
                   "'. Supported values: י, יא, יב";
        }
        
        return null; // Valid
    }
    
    /**
     * Data class to hold student data parsed from Excel.
     * Used to transfer parsed data from ExcelImporter to service layer.
     */
    public static class StudentData {
        private String name;
        private String studentId;
        private String gradeLevel;
        private String className;
        
        public StudentData() {
        }
        
        public StudentData(String name, String studentId, String gradeLevel, String className) {
            this.name = name;
            this.studentId = studentId;
            this.gradeLevel = gradeLevel;
            this.className = className;
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
        
        public String getClassName() {
            return className;
        }
        
        public void setClassName(String className) {
            this.className = className;
        }
        
        @Override
        public String toString() {
            return "StudentData{" +
                    "name='" + name + '\'' +
                    ", studentId='" + studentId + '\'' +
                    ", gradeLevel='" + gradeLevel + '\'' +
                    ", className='" + className + '\'' +
                    '}';
        }
    }
}
