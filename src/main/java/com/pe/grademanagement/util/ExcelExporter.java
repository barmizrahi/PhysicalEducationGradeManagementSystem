package com.pe.grademanagement.util;

import com.pe.grademanagement.entity.Student;
import com.pe.grademanagement.entity.Test;
import com.pe.grademanagement.entity.TestResult;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Component for exporting grade data to Excel files.
 * Generates Ministry of Education-compatible Excel exports.
 * Uses Apache POI to create Excel files.
 * 
 * Requirements:
 * - 9.1: Generate Excel file in Ministry format
 * - 9.2: Include student name, student ID, grade level, class name, and calculated grades
 * - 9.3: Optionally include notes column
 * - 9.4: Allow selection of which tests to include
 * - 9.5: Allow selection of which classes to include
 * - 9.6: Generate downloadable file
 * - 8.3: Include students without test results (grade = 0)
 */
@Component
public class ExcelExporter {
    
    // Column headers for Ministry format
    private static final String HEADER_STUDENT_NAME = "שם התלמיד";
    private static final String HEADER_STUDENT_ID = "תעודת זהות";
    private static final String HEADER_GRADE_LEVEL = "שכבה";
    private static final String HEADER_CLASS_NAME = "כיתה";
    private static final String HEADER_NOTES = "הערות";
    
    /**
     * Exports grades to Excel file based on the provided configuration.
     * This is the main entry point for export operations.
     * 
     * @param exportConfig Configuration specifying classes, tests, and options
     * @return Excel file as byte array
     * @throws IllegalArgumentException if exportConfig is null
     */
    public byte[] exportGrades(ExportConfig exportConfig) {
        if (exportConfig == null) {
            throw new IllegalArgumentException("ExportConfig cannot be null");
        }
        
        // Note: This method signature is defined in the design document
        // The actual implementation would require access to repositories to fetch data
        // For now, this is a placeholder that would be called by a service layer
        throw new UnsupportedOperationException(
            "This method should be called from a service layer that provides the necessary data. " +
            "Use generateMinistryFormatExcel() with pre-fetched data instead."
        );
    }
    
    /**
     * Generates Ministry-format Excel file with the specified data.
     * This method contains the core export logic and can be called with pre-fetched data.
     * 
     * Excel format:
     * - Column 1: Student Name (Hebrew supported)
     * - Column 2: Student ID
     * - Column 3: Grade Level (י, יא, יב)
     * - Column 4: Class Name
     * - Columns 5+: Test grades (one column per test)
     * - Last column (optional): Notes
     * 
     * @param students List of students to include in export
     * @param testResults Map of student → test → result
     * @param includeNotes Whether to include notes column
     * @return Excel file as byte array in Ministry format
     * @throws IllegalArgumentException if students list is null
     */
    public byte[] generateMinistryFormatExcel(List<Student> students, 
                                               Map<Student, Map<Test, TestResult>> testResults,
                                               boolean includeNotes) {
        if (students == null) {
            throw new IllegalArgumentException("Students list cannot be null");
        }
        
        if (testResults == null) {
            testResults = new HashMap<>();
        }
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("ציונים");
            
            // Collect all unique tests from the results
            Set<Test> allTests = collectAllTests(testResults);
            List<Test> testList = new ArrayList<>(allTests);
            // Sort tests by ID for consistent column ordering
            testList.sort(Comparator.comparing(Test::getId));
            
            // Create header row
            createHeaderRow(sheet, testList, includeNotes);
            
            // Create data rows for each student
            int rowNum = 1;
            for (Student student : students) {
                createStudentRow(sheet, rowNum++, student, testList, testResults.get(student), includeNotes);
            }
            
            // Auto-size columns for better readability
            autoSizeColumns(sheet, testList.size(), includeNotes);
            
            // Write workbook to byte array
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }
    
    /**
     * Generates fixed-format Excel file with exactly 6 columns and integer grades.
     * This is the new Ministry format for grade submission.
     * 
     * Excel format (fixed 6 columns):
     * - Column 1: Student ID
     * - Column 2: Student Name (Hebrew supported)
     * - Column 3: Grade Level (י, יא, יב)
     * - Column 4: Class Name
     * - Column 5: Grade (final grade as integer, rounded to nearest whole number)
     * - Column 6: Notes (optional notes about the student's performance)
     * 
     * Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7, 14.8
     * 
     * @param students List of students to include in export
     * @param testResults Map of student → test → result
     * @return Excel file as byte array in fixed Ministry format
     * @throws IllegalArgumentException if students list is null
     */
    public byte[] generateFixedFormatExcel(List<Student> students, 
                                           Map<Student, Map<Test, TestResult>> testResults) {
        if (students == null) {
            throw new IllegalArgumentException("Students list cannot be null");
        }
        
        if (testResults == null) {
            testResults = new HashMap<>();
        }
        
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("ציונים");
            
            // Create header row with fixed 5 columns
            createFixedFormatHeaderRow(sheet);
            
            // Create data rows for each student
            int rowNum = 1;
            for (Student student : students) {
                createFixedFormatStudentRow(sheet, rowNum++, student, testResults.get(student));
            }
            
            // Auto-size columns for better readability
            autoSizeFixedFormatColumns(sheet);
            
            // Write workbook to byte array
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }
    
    /**
     * Collects all unique tests from the test results map.
     * 
     * @param testResults Map of student → test → result
     * @return Set of all unique tests
     */
    private Set<Test> collectAllTests(Map<Student, Map<Test, TestResult>> testResults) {
        Set<Test> allTests = new LinkedHashSet<>();
        
        for (Map<Test, TestResult> studentResults : testResults.values()) {
            if (studentResults != null) {
                allTests.addAll(studentResults.keySet());
            }
        }
        
        return allTests;
    }
    
    /**
     * Creates the header row with column names.
     * 
     * @param sheet Excel sheet
     * @param tests List of tests (for test grade columns)
     * @param includeNotes Whether to include notes column
     */
    private void createHeaderRow(Sheet sheet, List<Test> tests, boolean includeNotes) {
        Row headerRow = sheet.createRow(0);
        
        // Create cell style for headers (bold)
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        
        int colNum = 0;
        
        // Fixed columns
        createStyledCell(headerRow, colNum++, HEADER_STUDENT_NAME, headerStyle);
        createStyledCell(headerRow, colNum++, HEADER_STUDENT_ID, headerStyle);
        createStyledCell(headerRow, colNum++, HEADER_GRADE_LEVEL, headerStyle);
        createStyledCell(headerRow, colNum++, HEADER_CLASS_NAME, headerStyle);
        
        // Test columns (one per test)
        for (Test test : tests) {
            createStyledCell(headerRow, colNum++, test.getName(), headerStyle);
        }
        
        // Optional notes column
        if (includeNotes) {
            createStyledCell(headerRow, colNum++, HEADER_NOTES, headerStyle);
        }
    }
    
    /**
     * Creates a data row for a single student.
     * 
     * @param sheet Excel sheet
     * @param rowNum Row number (0-based)
     * @param student Student entity
     * @param tests List of tests (for test grade columns)
     * @param studentResults Map of test → result for this student (can be null)
     * @param includeNotes Whether to include notes column
     */
    private void createStudentRow(Sheet sheet, int rowNum, Student student, List<Test> tests,
                                   Map<Test, TestResult> studentResults, boolean includeNotes) {
        Row row = sheet.createRow(rowNum);
        
        int colNum = 0;
        
        // Fixed columns
        createCell(row, colNum++, student.getName());
        createCell(row, colNum++, student.getStudentId() != null ? student.getStudentId() : "");
        createCell(row, colNum++, student.getGradeLevel());
        createCell(row, colNum++, student.getClassEntity() != null ? student.getClassEntity().getName() : "");
        
        // Test grade columns
        StringBuilder notesBuilder = new StringBuilder();
        
        for (Test test : tests) {
            TestResult result = (studentResults != null) ? studentResults.get(test) : null;
            
            if (result != null) {
                // Student has a result for this test
                BigDecimal grade = result.getCalculatedGrade();
                createNumericCell(row, colNum++, grade);
                
                // Collect notes if present
                if (includeNotes && result.hasNotes()) {
                    if (notesBuilder.length() > 0) {
                        notesBuilder.append("; ");
                    }
                    notesBuilder.append(test.getName()).append(": ").append(result.getNotes());
                }
            } else {
                // Student has no result for this test - grade = 0
                createNumericCell(row, colNum++, BigDecimal.ZERO);
            }
        }
        
        // Optional notes column
        if (includeNotes) {
            createCell(row, colNum++, notesBuilder.toString());
        }
    }
    
    /**
     * Creates a cell with string value.
     * 
     * @param row Excel row
     * @param colNum Column number (0-based)
     * @param value String value
     * @return Created cell
     */
    private Cell createCell(Row row, int colNum, String value) {
        Cell cell = row.createCell(colNum);
        cell.setCellValue(value != null ? value : "");
        return cell;
    }
    
    /**
     * Creates a cell with styled string value.
     * 
     * @param row Excel row
     * @param colNum Column number (0-based)
     * @param value String value
     * @param style Cell style
     * @return Created cell
     */
    private Cell createStyledCell(Row row, int colNum, String value, CellStyle style) {
        Cell cell = createCell(row, colNum, value);
        cell.setCellStyle(style);
        return cell;
    }
    
    /**
     * Creates a cell with numeric value (for grades).
     * Formats the number to 2 decimal places.
     * 
     * @param row Excel row
     * @param colNum Column number (0-based)
     * @param value Numeric value (BigDecimal)
     * @return Created cell
     */
    private Cell createNumericCell(Row row, int colNum, BigDecimal value) {
        Cell cell = row.createCell(colNum);
        
        if (value != null) {
            cell.setCellValue(value.doubleValue());
            
            // Create number format for 2 decimal places
            CellStyle style = row.getSheet().getWorkbook().createCellStyle();
            DataFormat format = row.getSheet().getWorkbook().createDataFormat();
            style.setDataFormat(format.getFormat("0.00"));
            cell.setCellStyle(style);
        } else {
            cell.setCellValue(0.0);
        }
        
        return cell;
    }
    
    /**
     * Auto-sizes columns for better readability.
     * 
     * @param sheet Excel sheet
     * @param numTests Number of test columns
     * @param includeNotes Whether notes column is included
     */
    private void autoSizeColumns(Sheet sheet, int numTests, boolean includeNotes) {
        // Fixed columns (4) + test columns + optional notes column
        int totalColumns = 4 + numTests + (includeNotes ? 1 : 0);
        
        for (int i = 0; i < totalColumns; i++) {
            sheet.autoSizeColumn(i);
            
            // Add some padding to the auto-sized width
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, currentWidth + 500);
        }
    }
    
    /**
     * Creates the header row for fixed format export (6 columns with notes).
     * Requirements: 14.1
     * 
     * @param sheet Excel sheet
     */
    private void createFixedFormatHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        
        // Create cell style for headers (bold)
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        
        int colNum = 0;
        
        // Fixed 6 columns in order: studentId, name, gradeLevel, className, grade, notes
        createStyledCell(headerRow, colNum++, HEADER_STUDENT_ID, headerStyle);
        createStyledCell(headerRow, colNum++, HEADER_STUDENT_NAME, headerStyle);
        createStyledCell(headerRow, colNum++, HEADER_GRADE_LEVEL, headerStyle);
        createStyledCell(headerRow, colNum++, HEADER_CLASS_NAME, headerStyle);
        createStyledCell(headerRow, colNum++, "ציון", headerStyle); // "Grade" in Hebrew
        createStyledCell(headerRow, colNum++, HEADER_NOTES, headerStyle); // "Notes" in Hebrew
    }
    
    /**
     * Creates a data row for a single student in fixed format (6 columns with notes).
     * Calculates the final grade as the average of all test results, rounded to nearest integer.
     * Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 14.6, 14.7, 14.8
     * 
     * @param sheet Excel sheet
     * @param rowNum Row number (0-based)
     * @param student Student entity
     * @param studentResults Map of test → result for this student (can be null)
     */
    private void createFixedFormatStudentRow(Sheet sheet, int rowNum, Student student,
                                             Map<Test, TestResult> studentResults) {
        Row row = sheet.createRow(rowNum);
        
        int colNum = 0;
        
        // Fixed 6 columns: studentId, name, gradeLevel, className, grade, notes
        createCell(row, colNum++, student.getStudentId() != null ? student.getStudentId() : "");
        createCell(row, colNum++, student.getName());
        createCell(row, colNum++, student.getGradeLevel());
        createCell(row, colNum++, student.getClassEntity() != null ? student.getClassEntity().getName() : "");
        
        // Calculate final grade as average of all test results, rounded to nearest integer
        int finalGrade = calculateFinalGrade(studentResults);
        createIntegerCell(row, colNum++, finalGrade);
        
        // Collect notes from all test results
        StringBuilder notesBuilder = new StringBuilder();
        if (studentResults != null && !studentResults.isEmpty()) {
            for (Map.Entry<Test, TestResult> entry : studentResults.entrySet()) {
                TestResult result = entry.getValue();
                if (result != null && result.hasNotes()) {
                    if (notesBuilder.length() > 0) {
                        notesBuilder.append("; ");
                    }
                    notesBuilder.append(result.getNotes());
                }
            }
        }
        createCell(row, colNum++, notesBuilder.toString());
    }
    
    /**
     * Calculates the final grade as the average of all test results.
     * Rounds to the nearest integer using standard rounding rules (0.5 and above rounds up).
     * Requirements: 14.2, 14.3, 14.4, 14.5, 14.6, 14.7, 14.8
     * 
     * @param studentResults Map of test → result for a student (can be null or empty)
     * @return Final grade as integer (0 if no results)
     */
    private int calculateFinalGrade(Map<Test, TestResult> studentResults) {
        if (studentResults == null || studentResults.isEmpty()) {
            return 0;
        }
        
        // Calculate average of all test grades
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        
        for (TestResult result : studentResults.values()) {
            if (result != null && result.getCalculatedGrade() != null) {
                sum = sum.add(result.getCalculatedGrade());
                count++;
            }
        }
        
        if (count == 0) {
            return 0;
        }
        
        // Calculate average
        BigDecimal average = sum.divide(BigDecimal.valueOf(count), 2, BigDecimal.ROUND_HALF_UP);
        
        // Round to nearest integer using Math.round()
        // Math.round() uses standard rounding: 0.5 and above rounds up
        return (int) Math.round(average.doubleValue());
    }
    
    /**
     * Creates a cell with integer value (for grades).
     * Requirements: 14.3, 14.8
     * 
     * @param row Excel row
     * @param colNum Column number (0-based)
     * @param value Integer value
     * @return Created cell
     */
    private Cell createIntegerCell(Row row, int colNum, int value) {
        Cell cell = row.createCell(colNum);
        cell.setCellValue(value);
        
        // Create number format for integers (no decimal places)
        CellStyle style = row.getSheet().getWorkbook().createCellStyle();
        DataFormat format = row.getSheet().getWorkbook().createDataFormat();
        style.setDataFormat(format.getFormat("0")); // Integer format
        cell.setCellStyle(style);
        
        return cell;
    }
    
    /**
     * Auto-sizes columns for fixed format export (6 columns with notes).
     * 
     * @param sheet Excel sheet
     */
    private void autoSizeFixedFormatColumns(Sheet sheet) {
        // Fixed 6 columns
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
            
            // Add some padding to the auto-sized width
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, currentWidth + 500);
        }
    }
}
