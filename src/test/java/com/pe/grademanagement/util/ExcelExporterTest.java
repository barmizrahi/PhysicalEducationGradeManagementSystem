package com.pe.grademanagement.util;

import com.pe.grademanagement.entity.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for ExcelExporter component.
 * Tests the generation of Ministry-format Excel files.
 */
class ExcelExporterTest {
    
    private ExcelExporter excelExporter;
    private Teacher teacher;
    private com.pe.grademanagement.entity.Class class1;
    private com.pe.grademanagement.entity.Class class2;
    
    @BeforeEach
    void setUp() {
        excelExporter = new ExcelExporter();
        
        // Create test teacher
        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setUsername("teacher1");
        teacher.setFullName("Test Teacher");
        
        // Create test classes
        class1 = new com.pe.grademanagement.entity.Class();
        class1.setId(1L);
        class1.setName("א1");
        class1.setGradeLevel("י");
        class1.setTeacher(teacher);
        
        class2 = new com.pe.grademanagement.entity.Class();
        class2.setId(2L);
        class2.setName("ב1");
        class2.setGradeLevel("יא");
        class2.setTeacher(teacher);
    }
    
    @Test
    void testGenerateMinistryFormatExcel_withBasicData() throws IOException {
        // Arrange
        Student student1 = createStudent(1L, "דוד כהן", "123456789", "י", class1);
        Student student2 = createStudent(2L, "שרה לוי", "987654321", "י", class1);
        
        com.pe.grademanagement.entity.Test test1 = createTest(1L, "ריצה 1500 מטר", CalculationType.PENALTY, UnitType.TIME);
        com.pe.grademanagement.entity.Test test2 = createTest(2L, "שכיבות סמיכה", CalculationType.RATIO, UnitType.COUNT);
        
        TestResult result1 = createTestResult(1L, student1, test1, new BigDecimal("10.50"), new BigDecimal("85.00"));
        TestResult result2 = createTestResult(2L, student1, test2, new BigDecimal("15.00"), new BigDecimal("75.00"));
        TestResult result3 = createTestResult(3L, student2, test1, new BigDecimal("12.00"), new BigDecimal("90.00"));
        
        List<Student> students = Arrays.asList(student1, student2);
        
        Map<Student, Map<com.pe.grademanagement.entity.Test, TestResult>> testResults = new HashMap<>();
        testResults.put(student1, Map.of(test1, result1, test2, result2));
        testResults.put(student2, Map.of(test1, result3));
        
        // Act
        byte[] excelBytes = excelExporter.generateMinistryFormatExcel(students, testResults, false);
        
        // Assert
        assertThat(excelBytes).isNotNull();
        assertThat(excelBytes.length).isGreaterThan(0);
        
        // Verify Excel content
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Verify header row
            Row headerRow = sheet.getRow(0);
            assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("שם התלמיד");
            assertThat(headerRow.getCell(1).getStringCellValue()).isEqualTo("תעודת זהות");
            assertThat(headerRow.getCell(2).getStringCellValue()).isEqualTo("שכבה");
            assertThat(headerRow.getCell(3).getStringCellValue()).isEqualTo("כיתה");
            assertThat(headerRow.getCell(4).getStringCellValue()).isEqualTo("ריצה 1500 מטר");
            assertThat(headerRow.getCell(5).getStringCellValue()).isEqualTo("שכיבות סמיכה");
            
            // Verify student 1 data
            Row row1 = sheet.getRow(1);
            assertThat(row1.getCell(0).getStringCellValue()).isEqualTo("דוד כהן");
            assertThat(row1.getCell(1).getStringCellValue()).isEqualTo("123456789");
            assertThat(row1.getCell(2).getStringCellValue()).isEqualTo("י");
            assertThat(row1.getCell(3).getStringCellValue()).isEqualTo("א1");
            assertThat(row1.getCell(4).getNumericCellValue()).isEqualTo(85.00);
            assertThat(row1.getCell(5).getNumericCellValue()).isEqualTo(75.00);
            
            // Verify student 2 data
            Row row2 = sheet.getRow(2);
            assertThat(row2.getCell(0).getStringCellValue()).isEqualTo("שרה לוי");
            assertThat(row2.getCell(1).getStringCellValue()).isEqualTo("987654321");
            assertThat(row2.getCell(2).getStringCellValue()).isEqualTo("י");
            assertThat(row2.getCell(3).getStringCellValue()).isEqualTo("א1");
            assertThat(row2.getCell(4).getNumericCellValue()).isEqualTo(90.00);
            assertThat(row2.getCell(5).getNumericCellValue()).isEqualTo(0.00); // No result for test2
        }
    }
    
    @Test
    void testGenerateMinistryFormatExcel_withNotes() throws IOException {
        // Arrange
        Student student1 = createStudent(1L, "דוד כהן", "123456789", "י", class1);
        
        com.pe.grademanagement.entity.Test test1 = createTest(1L, "ריצה 1500 מטר", CalculationType.PENALTY, UnitType.TIME);
        
        TestResult result1 = createTestResult(1L, student1, test1, new BigDecimal("10.50"), new BigDecimal("85.00"));
        result1.setNotes("ביצוע מצוין");
        
        List<Student> students = List.of(student1);
        Map<Student, Map<com.pe.grademanagement.entity.Test, TestResult>> testResults = new HashMap<>();
        testResults.put(student1, Map.of(test1, result1));
        
        // Act
        byte[] excelBytes = excelExporter.generateMinistryFormatExcel(students, testResults, true);
        
        // Assert
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Verify notes column in header
            Row headerRow = sheet.getRow(0);
            assertThat(headerRow.getCell(5).getStringCellValue()).isEqualTo("הערות");
            
            // Verify notes in data row
            Row row1 = sheet.getRow(1);
            assertThat(row1.getCell(5).getStringCellValue()).contains("ריצה 1500 מטר: ביצוע מצוין");
        }
    }
    
    @Test
    void testGenerateMinistryFormatExcel_studentWithoutResults() throws IOException {
        // Arrange
        Student student1 = createStudent(1L, "דוד כהן", "123456789", "י", class1);
        Student student2 = createStudent(2L, "שרה לוי", "987654321", "י", class1);
        
        com.pe.grademanagement.entity.Test test1 = createTest(1L, "ריצה 1500 מטר", CalculationType.PENALTY, UnitType.TIME);
        
        TestResult result1 = createTestResult(1L, student1, test1, new BigDecimal("10.50"), new BigDecimal("85.00"));
        
        List<Student> students = Arrays.asList(student1, student2);
        Map<Student, Map<com.pe.grademanagement.entity.Test, TestResult>> testResults = new HashMap<>();
        testResults.put(student1, Map.of(test1, result1));
        // student2 has no results
        
        // Act
        byte[] excelBytes = excelExporter.generateMinistryFormatExcel(students, testResults, false);
        
        // Assert
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Verify student 2 is included with grade 0
            Row row2 = sheet.getRow(2);
            assertThat(row2.getCell(0).getStringCellValue()).isEqualTo("שרה לוי");
            assertThat(row2.getCell(4).getNumericCellValue()).isEqualTo(0.00); // Grade = 0 for no result
        }
    }
    
    @Test
    void testGenerateMinistryFormatExcel_emptyStudentList() throws IOException {
        // Arrange
        List<Student> students = new ArrayList<>();
        Map<Student, Map<com.pe.grademanagement.entity.Test, TestResult>> testResults = new HashMap<>();
        
        // Act
        byte[] excelBytes = excelExporter.generateMinistryFormatExcel(students, testResults, false);
        
        // Assert
        assertThat(excelBytes).isNotNull();
        
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Should have header row only
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1);
            
            Row headerRow = sheet.getRow(0);
            assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("שם התלמיד");
        }
    }
    
    @Test
    void testGenerateMinistryFormatExcel_nullStudentList() {
        // Arrange
        Map<Student, Map<com.pe.grademanagement.entity.Test, TestResult>> testResults = new HashMap<>();
        
        // Act & Assert
        assertThatThrownBy(() -> excelExporter.generateMinistryFormatExcel(null, testResults, false))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Students list cannot be null");
    }
    
    @Test
    void testGenerateMinistryFormatExcel_nullTestResults() throws IOException {
        // Arrange
        Student student1 = createStudent(1L, "דוד כהן", "123456789", "י", class1);
        List<Student> students = List.of(student1);
        
        // Act
        byte[] excelBytes = excelExporter.generateMinistryFormatExcel(students, null, false);
        
        // Assert
        assertThat(excelBytes).isNotNull();
        
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Should have header row + 1 student row
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(2);
            
            Row row1 = sheet.getRow(1);
            assertThat(row1.getCell(0).getStringCellValue()).isEqualTo("דוד כהן");
        }
    }
    
    @Test
    void testGenerateMinistryFormatExcel_studentWithNullStudentId() throws IOException {
        // Arrange
        Student student1 = createStudent(1L, "דוד כהן", null, "י", class1);
        
        com.pe.grademanagement.entity.Test test1 = createTest(1L, "ריצה 1500 מטר", CalculationType.PENALTY, UnitType.TIME);
        TestResult result1 = createTestResult(1L, student1, test1, new BigDecimal("10.50"), new BigDecimal("85.00"));
        
        List<Student> students = List.of(student1);
        Map<Student, Map<com.pe.grademanagement.entity.Test, TestResult>> testResults = new HashMap<>();
        testResults.put(student1, Map.of(test1, result1));
        
        // Act
        byte[] excelBytes = excelExporter.generateMinistryFormatExcel(students, testResults, false);
        
        // Assert
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            
            Row row1 = sheet.getRow(1);
            assertThat(row1.getCell(0).getStringCellValue()).isEqualTo("דוד כהן");
            assertThat(row1.getCell(1).getStringCellValue()).isEmpty(); // Empty string for null student ID
        }
    }
    
    @Test
    void testExportGrades_throwsUnsupportedOperationException() {
        // Arrange
        ExportConfig config = new ExportConfig();
        
        // Act & Assert
        assertThatThrownBy(() -> excelExporter.exportGrades(config))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("service layer");
    }
    
    // Helper methods
    
    private Student createStudent(Long id, String name, String studentId, String gradeLevel, com.pe.grademanagement.entity.Class classEntity) {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setStudentId(studentId);
        student.setGradeLevel(gradeLevel);
        student.setClassEntity(classEntity);
        student.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        student.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        return student;
    }
    
    private com.pe.grademanagement.entity.Test createTest(Long id, String name, CalculationType calculationType, UnitType unitType) {
        com.pe.grademanagement.entity.Test test = new com.pe.grademanagement.entity.Test();
        test.setId(id);
        test.setName(name);
        test.setCalculationType(calculationType);
        test.setUnitType(unitType);
        test.setCreatedBy(teacher);
        test.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        test.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        return test;
    }
    
    private TestResult createTestResult(Long id, Student student, com.pe.grademanagement.entity.Test test, 
                                        BigDecimal rawResult, BigDecimal calculatedGrade) {
        TestResult result = new TestResult();
        result.setId(id);
        result.setStudent(student);
        result.setTest(test);
        result.setRawResult(rawResult);
        result.setCalculatedGrade(calculatedGrade);
        result.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        result.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        return result;
    }
}
