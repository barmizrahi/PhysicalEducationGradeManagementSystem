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
 * Expects a fixed format with exactly 4 columns in order: studentId, name, gradeLevel, className.
 * 
 * Requirements:
 * - 13.1: Expect exactly 4 columns in fixed order
 * - 13.2: No column mapping configuration required
 * - 13.3: Validate column count
 * - 13.4: Return Hebrew error for wrong column count
 * - 13.5: Return Hebrew error for wrong column order
 */
@Component
public class ExcelImporter {
    
    private static final Set<String> VALID_GRADE_LEVELS = Set.of("י", "יא", "יב");
    private static final int MAX_ROWS = 10000; // Safety limit to prevent memory issues
    private static final int EXPECTED_COLUMN_COUNT = 4; // Fixed format: studentId, name, gradeLevel, className
    
    /**
     * Validates the format of an Excel file.
     * Checks if the file is a valid Excel file, can be opened, and has exactly 4 columns.
     * 
     * @param file Excel file to validate
     * @return ValidationResult with errors if any
     */
    public ValidationResult validateExcelFormat(MultipartFile file) {
        ValidationResult result = new ValidationResult();
        
        // Check if file is null or empty
        if (file == null || file.isEmpty()) {
            result.addError("הקובץ ריק או לא סופק");
            return result;
        }
        
        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            result.addError("פורמט קובץ Excel לא תקין. אנא העלה קובץ .xlsx");
            return result;
        }
        
        // Try to open the file and validate structure
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            // Check if workbook has at least one sheet
            if (workbook.getNumberOfSheets() == 0) {
                result.addError("קובץ Excel לא מכיל גיליונות");
                return result;
            }
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // Check if sheet has at least one row
            if (sheet.getPhysicalNumberOfRows() == 0) {
                result.addError("גיליון Excel ריק");
                return result;
            }
            
            // Validate column count - check first data row (skip header if present)
            Row firstRow = sheet.getRow(0);
            if (firstRow != null) {
                int columnCount = firstRow.getLastCellNum();
                if (columnCount != EXPECTED_COLUMN_COUNT) {
                    result.addError("מספר עמודות שגוי - נדרשות בדיוק 4 עמודות (תעודת זהות, שם, שכבה, כיתה)");
                    return result;
                }
            }
            
        } catch (IOException e) {
            result.addError("שגיאה בקריאת קובץ Excel: " + e.getMessage());
        } catch (Exception e) {
            result.addError("פורמט קובץ Excel לא תקין: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Imports students from an Excel file with fixed format.
     * Expects exactly 4 columns in this order: studentId, name, gradeLevel, className.
     * Automatically detects the header row by looking for "ת.ז" in the first column.
     * Skips empty rows before the header and stops reading when encountering an empty row.
     * 
     * Note: This method only parses the Excel file and returns StudentData objects.
     * The service layer is responsible for creating Student entities and handling database operations.
     * 
     * @param file Excel file containing student data
     * @return List of StudentData objects parsed from the file
     * @throws InvalidExcelFormatException if the file format is invalid
     */
    public List<StudentData> importStudents(MultipartFile file) 
            throws InvalidExcelFormatException {
        
        List<StudentData> students = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        // Basic file validation
        if (file == null || file.isEmpty()) {
            throw new InvalidExcelFormatException("הקובץ ריק או לא סופק");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new InvalidExcelFormatException("פורמט קובץ Excel לא תקין. אנא העלה קובץ .xlsx");
        }
        
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            
            if (workbook.getNumberOfSheets() == 0) {
                throw new InvalidExcelFormatException("קובץ Excel לא מכיל גיליונות");
            }
            
            Sheet sheet = workbook.getSheetAt(0);
            
            if (sheet.getPhysicalNumberOfRows() == 0) {
                throw new InvalidExcelFormatException("גיליון Excel ריק");
            }
            
            // Find the header row by looking for "ת.ז" in the first column
            int headerRowIndex = findHeaderRow(sheet);
            if (headerRowIndex == -1) {
                throw new InvalidExcelFormatException("לא נמצאה שורת כותרות עם 'ת.ז' בעמודה הראשונה");
            }
            
            // Validate that header row has exactly 4 columns
            Row headerRow = sheet.getRow(headerRowIndex);
            int columnCount = headerRow.getLastCellNum();
            if (columnCount != EXPECTED_COLUMN_COUNT) {
                throw new InvalidExcelFormatException("מספר עמודות שגוי - נדרשות בדיוק 4 עמודות (ת.ז, שם התלמיד, שכבה, מקבילה)");
            }
            
            // Parse data rows starting from the row after the header
            int rowCount = 0;
            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                
                // Stop if we encounter an empty row (end of data)
                if (row == null || isRowEmpty(row)) {
                    break;
                }
                
                // Safety check to prevent processing too many rows
                if (rowCount++ > MAX_ROWS) {
                    errors.add("הקובץ מכיל יותר מדי שורות (מקסימום: " + MAX_ROWS + ")");
                    break;
                }
                
                // Validate column count for each row
                int rowColumnCount = row.getLastCellNum();
                if (rowColumnCount != EXPECTED_COLUMN_COUNT) {
                    errors.add("שורה " + (i + 1) + ": מספר עמודות שגוי - נדרשות בדיוק 4 עמודות");
                    continue;
                }
                
                try {
                    StudentData studentData = parseRowFixedFormat(row);
                    
                    // Validate student data
                    String validationError = validateStudentData(studentData, i + 1);
                    if (validationError != null) {
                        errors.add(validationError);
                        continue;
                    }
                    
                    students.add(studentData);
                    
                } catch (Exception e) {
                    errors.add("שגיאה בעיבוד שורה " + (i + 1) + ": " + e.getMessage());
                }
            }
            
            // Check if we found any students
            if (students.isEmpty() && errors.isEmpty()) {
                throw new InvalidExcelFormatException("לא נמצאו נתוני תלמידים בקובץ");
            }
            
            // If there were errors, throw exception with all error messages
            if (!errors.isEmpty()) {
                throw new InvalidExcelFormatException(String.join("; ", errors));
            }
            
        } catch (InvalidExcelFormatException e) {
            throw e; // Re-throw our custom exceptions
        } catch (IOException e) {
            throw new InvalidExcelFormatException("שגיאה בקריאת קובץ Excel", e);
        } catch (Exception e) {
            throw new InvalidExcelFormatException("פורמט קובץ Excel לא תקין: " + e.getMessage(), e);
        }
        
        return students;
    }
    
    /**
     * Finds the header row by looking for "ת.ז" in the first column.
     * Skips empty rows at the beginning of the sheet.
     * 
     * @param sheet Excel sheet to search
     * @return Index of the header row, or -1 if not found
     */
    private int findHeaderRow(Sheet sheet) {
        for (Row row : sheet) {
            if (row == null || isRowEmpty(row)) {
                continue; // Skip empty rows
            }
            
            Cell firstCell = row.getCell(0);
            if (firstCell != null) {
                String cellValue = getCellValueAsString(firstCell);
                if (cellValue != null && cellValue.trim().equals("ת.ז")) {
                    return row.getRowNum();
                }
            }
        }
        return -1; // Header not found
    }
    
    /**
     * Checks if a row is empty (all cells are null or blank).
     * 
     * @param row Row to check
     * @return true if the row is empty, false otherwise
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false; // Found a non-empty cell
                }
            }
        }
        return true; // All cells are empty
    }
    
    /**
     * Imports students from an Excel file (deprecated method with column mapping).
     * 
     * @deprecated Use importStudents(MultipartFile file) instead. Column mapping is no longer supported.
     * @param file Excel file containing student data
     * @param columnMapping Column mapping configuration (ignored)
     * @return List of StudentData objects parsed from the file
     * @throws InvalidExcelFormatException if the file format is invalid
     */
    @Deprecated
    public List<StudentData> importStudents(MultipartFile file, ColumnMapping columnMapping) 
            throws InvalidExcelFormatException {
        // Ignore column mapping and use fixed format
        return importStudents(file);
    }
    
    /**
     * Parses a single row from the Excel sheet using fixed format.
     * Expected columns: 0=studentId, 1=name, 2=gradeLevel, 3=className
     * 
     * @param row Excel row to parse
     * @return StudentData extracted from the row
     */
    private StudentData parseRowFixedFormat(Row row) {
        StudentData data = new StudentData();
        
        // Column 0: Student ID
        data.studentId = getCellValueAsString(row.getCell(0));
        // Treat empty string as null
        if (data.studentId != null && data.studentId.trim().isEmpty()) {
            data.studentId = null;
        }
        
        // Column 1: Name
        data.name = getCellValueAsString(row.getCell(1));
        
        // Column 2: Grade Level
        data.gradeLevel = getCellValueAsString(row.getCell(2));
        
        // Column 3: Class Name
        data.className = getCellValueAsString(row.getCell(3));
        
        return data;
    }
    
    /**
     * Parses a single row from the Excel sheet (deprecated method with column mapping).
     * 
     * @deprecated Use parseRowFixedFormat(Row row) instead
     * @param row Excel row to parse
     * @param columnMapping Column mapping configuration
     * @return StudentData extracted from the row
     */
    @Deprecated
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
            return "שורה " + rowNumber + ": שם התלמיד נדרש";
        }
        
        if (data.gradeLevel == null || data.gradeLevel.trim().isEmpty()) {
            return "שורה " + rowNumber + ": שכבה נדרשת";
        }
        
        if (data.className == null || data.className.trim().isEmpty()) {
            return "שורה " + rowNumber + ": שם הכיתה נדרש";
        }
        
        // Validate grade level
        if (!VALID_GRADE_LEVELS.contains(data.gradeLevel.trim())) {
            return "שורה " + rowNumber + ": שכבה לא תקינה '" + data.gradeLevel + 
                   "'. ערכים נתמכים: י, יא, יב";
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
