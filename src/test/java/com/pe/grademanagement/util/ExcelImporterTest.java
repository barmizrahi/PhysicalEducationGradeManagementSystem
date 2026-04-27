package com.pe.grademanagement.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ExcelImporter component.
 * Tests Excel file parsing, validation, and error handling.
 */
class ExcelImporterTest {
    
    private ExcelImporter excelImporter;
    
    @BeforeEach
    void setUp() {
        excelImporter = new ExcelImporter();
    }
    
    @Test
    void testValidateExcelFormat_ValidFile() throws IOException {
        // Create a valid Excel file
        MultipartFile file = createValidExcelFile();
        
        // Validate
        ValidationResult result = excelImporter.validateExcelFormat(file);
        
        // Assert
        assertTrue(result.isValid(), "Valid Excel file should pass validation");
        assertTrue(result.getErrors().isEmpty(), "Valid file should have no errors");
    }
    
    @Test
    void testValidateExcelFormat_NullFile() {
        // Validate null file
        ValidationResult result = excelImporter.validateExcelFormat(null);
        
        // Assert
        assertFalse(result.isValid(), "Null file should fail validation");
        assertFalse(result.getErrors().isEmpty(), "Null file should have errors");
        assertTrue(result.getErrorMessage().contains("empty"), "Error should mention empty file");
    }
    
    @Test
    void testValidateExcelFormat_EmptyFile() {
        // Create empty file
        MultipartFile file = new MockMultipartFile(
            "test.xlsx",
            "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            new byte[0]
        );
        
        // Validate
        ValidationResult result = excelImporter.validateExcelFormat(file);
        
        // Assert
        assertFalse(result.isValid(), "Empty file should fail validation");
        assertFalse(result.getErrors().isEmpty(), "Empty file should have errors");
    }
    
    @Test
    void testValidateExcelFormat_WrongExtension() {
        // Create file with wrong extension
        MultipartFile file = new MockMultipartFile(
            "test.csv",
            "test.csv",
            "text/csv",
            "some,data".getBytes()
        );
        
        // Validate
        ValidationResult result = excelImporter.validateExcelFormat(file);
        
        // Assert
        assertFalse(result.isValid(), "CSV file should fail validation");
        assertTrue(result.getErrorMessage().contains(".xlsx"), "Error should mention .xlsx format");
    }
    
    @Test
    void testImportStudents_ValidFile() throws Exception {
        // Create valid Excel file with student data
        MultipartFile file = createExcelFileWithStudents();
        
        // Create column mapping
        ColumnMapping mapping = new ColumnMapping(0, 1, 2, 3);
        
        // Import students
        List<ExcelImporter.StudentData> students = excelImporter.importStudents(file, mapping);
        
        // Assert
        assertNotNull(students, "Students list should not be null");
        assertEquals(2, students.size(), "Should import 2 students");
        
        // Check first student
        ExcelImporter.StudentData student1 = students.get(0);
        assertEquals("דוד כהן", student1.getName());
        assertEquals("123456789", student1.getStudentId());
        assertEquals("י", student1.getGradeLevel());
        assertEquals("א1", student1.getClassName());
        
        // Check second student
        ExcelImporter.StudentData student2 = students.get(1);
        assertEquals("שרה לוי", student2.getName());
        assertEquals("987654321", student2.getStudentId());
        assertEquals("יא", student2.getGradeLevel());
        assertEquals("ב2", student2.getClassName());
    }
    
    @Test
    void testImportStudents_OptionalStudentId() throws Exception {
        // Create Excel file without student IDs
        MultipartFile file = createExcelFileWithoutStudentIds();
        
        // Create column mapping without student ID column
        ColumnMapping mapping = new ColumnMapping(0, null, 1, 2);
        
        // Import students
        List<ExcelImporter.StudentData> students = excelImporter.importStudents(file, mapping);
        
        // Assert
        assertNotNull(students, "Students list should not be null");
        assertEquals(1, students.size(), "Should import 1 student");
        
        ExcelImporter.StudentData student = students.get(0);
        assertEquals("יוסי אברהם", student.getName());
        assertNull(student.getStudentId(), "Student ID should be null");
        assertEquals("יב", student.getGradeLevel());
        assertEquals("ג3", student.getClassName());
    }
    
    @Test
    void testImportStudents_InvalidGradeLevel() throws Exception {
        // Create Excel file with invalid grade level
        MultipartFile file = createExcelFileWithInvalidGradeLevel();
        
        // Create column mapping
        ColumnMapping mapping = new ColumnMapping(0, 1, 2, 3);
        
        // Import students - should throw exception
        InvalidExcelFormatException exception = assertThrows(
            InvalidExcelFormatException.class,
            () -> excelImporter.importStudents(file, mapping),
            "Should throw exception for invalid grade level"
        );
        
        // Assert error message
        assertTrue(exception.getMessage().contains("Invalid grade level"),
            "Error message should mention invalid grade level");
        assertTrue(exception.getMessage().contains("י, יא, יב"),
            "Error message should list valid grade levels");
    }
    
    @Test
    void testImportStudents_MissingRequiredField() throws Exception {
        // Create Excel file with missing name
        MultipartFile file = createExcelFileWithMissingName();
        
        // Create column mapping
        ColumnMapping mapping = new ColumnMapping(0, 1, 2, 3);
        
        // Import students - should throw exception
        InvalidExcelFormatException exception = assertThrows(
            InvalidExcelFormatException.class,
            () -> excelImporter.importStudents(file, mapping),
            "Should throw exception for missing required field"
        );
        
        // Assert error message
        assertTrue(exception.getMessage().contains("name is required"),
            "Error message should mention missing name");
    }
    
    @Test
    void testImportStudents_InvalidColumnMapping() {
        // Create valid Excel file
        MultipartFile file = createValidExcelFile();
        
        // Create invalid column mapping (missing required columns)
        ColumnMapping mapping = new ColumnMapping(0, null, null, null);
        
        // Import students - should throw exception
        InvalidExcelFormatException exception = assertThrows(
            InvalidExcelFormatException.class,
            () -> excelImporter.importStudents(file, mapping),
            "Should throw exception for invalid column mapping"
        );
        
        // Assert error message
        assertTrue(exception.getMessage().contains("Invalid column mapping"),
            "Error message should mention invalid column mapping");
    }
    
    // Helper methods to create test Excel files
    
    private MultipartFile createValidExcelFile() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Students");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Name");
            headerRow.createCell(1).setCellValue("Student ID");
            headerRow.createCell(2).setCellValue("Grade Level");
            headerRow.createCell(3).setCellValue("Class");
            
            workbook.write(bos);
            
            return new MockMultipartFile(
                "test.xlsx",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test Excel file", e);
        }
    }
    
    private MultipartFile createExcelFileWithStudents() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Students");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Name");
            headerRow.createCell(1).setCellValue("Student ID");
            headerRow.createCell(2).setCellValue("Grade Level");
            headerRow.createCell(3).setCellValue("Class");
            
            // Create data rows
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("דוד כהן");
            row1.createCell(1).setCellValue("123456789");
            row1.createCell(2).setCellValue("י");
            row1.createCell(3).setCellValue("א1");
            
            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("שרה לוי");
            row2.createCell(1).setCellValue("987654321");
            row2.createCell(2).setCellValue("יא");
            row2.createCell(3).setCellValue("ב2");
            
            workbook.write(bos);
            
            return new MockMultipartFile(
                "test.xlsx",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test Excel file", e);
        }
    }
    
    private MultipartFile createExcelFileWithoutStudentIds() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Students");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Name");
            headerRow.createCell(1).setCellValue("Grade Level");
            headerRow.createCell(2).setCellValue("Class");
            
            // Create data row
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("יוסי אברהם");
            row1.createCell(1).setCellValue("יב");
            row1.createCell(2).setCellValue("ג3");
            
            workbook.write(bos);
            
            return new MockMultipartFile(
                "test.xlsx",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test Excel file", e);
        }
    }
    
    private MultipartFile createExcelFileWithInvalidGradeLevel() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Students");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Name");
            headerRow.createCell(1).setCellValue("Student ID");
            headerRow.createCell(2).setCellValue("Grade Level");
            headerRow.createCell(3).setCellValue("Class");
            
            // Create data row with invalid grade level
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("Test Student");
            row1.createCell(1).setCellValue("123456789");
            row1.createCell(2).setCellValue("13"); // Invalid grade level
            row1.createCell(3).setCellValue("A1");
            
            workbook.write(bos);
            
            return new MockMultipartFile(
                "test.xlsx",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test Excel file", e);
        }
    }
    
    private MultipartFile createExcelFileWithMissingName() {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Students");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Name");
            headerRow.createCell(1).setCellValue("Student ID");
            headerRow.createCell(2).setCellValue("Grade Level");
            headerRow.createCell(3).setCellValue("Class");
            
            // Create data row with missing name
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue(""); // Empty name
            row1.createCell(1).setCellValue("123456789");
            row1.createCell(2).setCellValue("י");
            row1.createCell(3).setCellValue("A1");
            
            workbook.write(bos);
            
            return new MockMultipartFile(
                "test.xlsx",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test Excel file", e);
        }
    }
}
