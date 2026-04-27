package com.pe.grademanagement.service;

import com.pe.grademanagement.entity.*;
import com.pe.grademanagement.repository.StudentRepository;
import com.pe.grademanagement.repository.TeacherRepository;
import com.pe.grademanagement.repository.TestRepository;
import com.pe.grademanagement.repository.TestResultRepository;
import com.pe.grademanagement.util.GradeCalculator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GradeService.
 * 
 * Tests cover:
 * - Saving test results with automatic grade calculation
 * - Updating existing test results with grade recalculation
 * - Handling null raw results (grade = 0)
 * - Bulk save operations
 * - Retrieving test results for classes and tests
 * - Validation and error handling
 */
@ExtendWith(MockitoExtension.class)
class GradeServiceTest {
    
    @Mock
    private TestResultRepository testResultRepository;
    
    @Mock
    private StudentRepository studentRepository;
    
    @Mock
    private TestRepository testRepository;
    
    @Mock
    private TeacherRepository teacherRepository;
    
    @Mock
    private GradeCalculator gradeCalculator;
    
    @Mock
    private SecurityContext securityContext;
    
    @InjectMocks
    private GradeService gradeService;
    
    private Teacher teacher;
    private com.pe.grademanagement.entity.Class classEntity;
    private Student student;
    private com.pe.grademanagement.entity.Test test;
    private TestResult testResult;
    
    @BeforeEach
    void setUp() {
        // Create test teacher
        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setUsername("teacher1");
        teacher.setFullName("Test Teacher");
        
        // Set up authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            teacher.getUsername(),
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        lenient().when(teacherRepository.findByUsername(teacher.getUsername())).thenReturn(Optional.of(teacher));
        
        // Create test class
        classEntity = new com.pe.grademanagement.entity.Class();
        classEntity.setId(1L);
        classEntity.setName("א1");
        classEntity.setGradeLevel("י");
        classEntity.setTeacher(teacher);
        
        // Create test student
        student = new Student();
        student.setId(1L);
        student.setName("Test Student");
        student.setGradeLevel("י");
        student.setClassEntity(classEntity);
        
        // Create test configuration (RATIO type)
        test = new com.pe.grademanagement.entity.Test();
        test.setId(1L);
        test.setName("100m Sprint");
        test.setCalculationType(CalculationType.RATIO);
        test.setUnitType(UnitType.COUNT);
        test.setMaxValue(BigDecimal.valueOf(20));
        test.setCreatedBy(teacher);
        
        // Create test result
        testResult = new TestResult();
        testResult.setStudent(student);
        testResult.setTest(test);
        testResult.setRawResult(BigDecimal.valueOf(15));
    }
    
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
    
    @Test
    void saveTestResult_NewResult_CalculatesGradeAndSaves() {
        // Arrange
        BigDecimal expectedGrade = BigDecimal.valueOf(75.00);
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(gradeCalculator.calculateGrade(BigDecimal.valueOf(15), test)).thenReturn(expectedGrade);
        when(testResultRepository.findByStudentIdAndTestId(1L, 1L)).thenReturn(Optional.empty());
        when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> {
            TestResult saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        
        // Act
        TestResult result = gradeService.saveTestResult(testResult);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCalculatedGrade()).isEqualByComparingTo(expectedGrade);
        assertThat(result.getRawResult()).isEqualByComparingTo(BigDecimal.valueOf(15));
        
        verify(gradeCalculator).calculateGrade(BigDecimal.valueOf(15), test);
        verify(testResultRepository).save(any(TestResult.class));
    }
    
    @Test
    void saveTestResult_ExistingResult_UpdatesAndRecalculatesGrade() {
        // Arrange
        TestResult existingResult = new TestResult();
        existingResult.setId(1L);
        existingResult.setStudent(student);
        existingResult.setTest(test);
        existingResult.setRawResult(BigDecimal.valueOf(10));
        existingResult.setCalculatedGrade(BigDecimal.valueOf(50.00));
        
        BigDecimal newGrade = BigDecimal.valueOf(75.00);
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(gradeCalculator.calculateGrade(BigDecimal.valueOf(15), test)).thenReturn(newGrade);
        when(testResultRepository.findByStudentIdAndTestId(1L, 1L)).thenReturn(Optional.of(existingResult));
        when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        TestResult result = gradeService.saveTestResult(testResult);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCalculatedGrade()).isEqualByComparingTo(newGrade);
        assertThat(result.getRawResult()).isEqualByComparingTo(BigDecimal.valueOf(15));
        
        verify(gradeCalculator).calculateGrade(BigDecimal.valueOf(15), test);
        verify(testResultRepository).save(existingResult);
    }
    
    @Test
    void saveTestResult_NullRawResult_SetsGradeToZero() {
        // Arrange
        testResult.setRawResult(null);
        testResult.setNotes("Student was absent");
        
        BigDecimal expectedGrade = BigDecimal.ZERO.setScale(2);
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(gradeCalculator.calculateGrade(null, test)).thenReturn(expectedGrade);
        when(testResultRepository.findByStudentIdAndTestId(1L, 1L)).thenReturn(Optional.empty());
        when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> {
            TestResult saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        
        // Act
        TestResult result = gradeService.saveTestResult(testResult);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRawResult()).isNull();
        assertThat(result.getCalculatedGrade()).isEqualByComparingTo(expectedGrade);
        assertThat(result.getNotes()).isEqualTo("Student was absent");
        
        verify(gradeCalculator).calculateGrade(null, test);
        verify(testResultRepository).save(any(TestResult.class));
    }
    
    @Test
    void saveTestResult_NullResult_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> gradeService.saveTestResult(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Test result cannot be null");
    }
    
    @Test
    void saveTestResult_NullStudent_ThrowsException() {
        // Arrange
        testResult.setStudent(null);
        
        // Act & Assert
        assertThatThrownBy(() -> gradeService.saveTestResult(testResult))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Student is required");
    }
    
    @Test
    void saveTestResult_NullTest_ThrowsException() {
        // Arrange
        testResult.setTest(null);
        
        // Act & Assert
        assertThatThrownBy(() -> gradeService.saveTestResult(testResult))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Test is required");
    }
    
    @Test
    void saveTestResult_StudentNotFound_ThrowsException() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> gradeService.saveTestResult(testResult))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Student not found");
    }
    
    @Test
    void saveTestResult_TestNotFound_ThrowsException() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(testRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> gradeService.saveTestResult(testResult))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Test not found");
    }
    
    @Test
    void getTestResultsForClass_ReturnsResults() {
        // Arrange
        TestResult result1 = new TestResult();
        result1.setId(1L);
        result1.setStudent(student);
        result1.setTest(test);
        result1.setRawResult(BigDecimal.valueOf(15));
        result1.setCalculatedGrade(BigDecimal.valueOf(75.00));
        
        TestResult result2 = new TestResult();
        result2.setId(2L);
        result2.setStudent(student);
        result2.setTest(test);
        result2.setRawResult(BigDecimal.valueOf(18));
        result2.setCalculatedGrade(BigDecimal.valueOf(90.00));
        
        List<TestResult> expectedResults = Arrays.asList(result1, result2);
        
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(testResultRepository.findByClassIdAndTestIdOrderByStudentName(1L, 1L))
                .thenReturn(expectedResults);
        
        // Act
        List<TestResult> results = gradeService.getTestResultsForClass(1L, 1L);
        
        // Assert
        assertThat(results).hasSize(2);
        assertThat(results).containsExactly(result1, result2);
        
        verify(testResultRepository).findByClassIdAndTestIdOrderByStudentName(1L, 1L);
    }
    
    @Test
    void getTestResultsForClass_NullClassId_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> gradeService.getTestResultsForClass(null, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Class ID cannot be null");
    }
    
    @Test
    void getTestResultsForClass_NullTestId_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> gradeService.getTestResultsForClass(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Test ID cannot be null");
    }
    
    @Test
    void bulkSaveTestResults_SavesAllResults() {
        // Arrange
        Student student2 = new Student();
        student2.setId(2L);
        student2.setName("Student 2");
        student2.setGradeLevel("י");
        student2.setClassEntity(classEntity);
        
        TestResult result1 = new TestResult();
        result1.setStudent(student);
        result1.setTest(test);
        result1.setRawResult(BigDecimal.valueOf(15));
        
        TestResult result2 = new TestResult();
        result2.setStudent(student2);
        result2.setTest(test);
        result2.setRawResult(BigDecimal.valueOf(18));
        
        List<TestResult> results = Arrays.asList(result1, result2);
        
        BigDecimal grade1 = BigDecimal.valueOf(75.00);
        BigDecimal grade2 = BigDecimal.valueOf(90.00);
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student2));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(gradeCalculator.calculateGrade(BigDecimal.valueOf(15), test)).thenReturn(grade1);
        when(gradeCalculator.calculateGrade(BigDecimal.valueOf(18), test)).thenReturn(grade2);
        when(testResultRepository.findByStudentIdAndTestId(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> {
            TestResult saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(saved.getStudent().getId());
            }
            return saved;
        });
        
        // Act
        List<TestResult> savedResults = gradeService.bulkSaveTestResults(results);
        
        // Assert
        assertThat(savedResults).hasSize(2);
        assertThat(savedResults.get(0).getCalculatedGrade()).isEqualByComparingTo(grade1);
        assertThat(savedResults.get(1).getCalculatedGrade()).isEqualByComparingTo(grade2);
        
        verify(testResultRepository, times(2)).save(any(TestResult.class));
    }
    
    @Test
    void bulkSaveTestResults_NullList_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> gradeService.bulkSaveTestResults(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Results list cannot be null");
    }
    
    @Test
    void bulkSaveTestResults_InvalidResult_ThrowsExceptionWithContext() {
        // Arrange
        TestResult invalidResult = new TestResult();
        invalidResult.setStudent(student);
        invalidResult.setTest(test);
        invalidResult.setRawResult(BigDecimal.valueOf(15));
        
        List<TestResult> results = Arrays.asList(invalidResult);
        
        when(studentRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> gradeService.bulkSaveTestResults(results))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to save result for student ID 1");
    }
    
    @Test
    void getTestResultByStudentAndTest_ReturnsResult() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(testResultRepository.findByStudentIdAndTestId(1L, 1L))
                .thenReturn(Optional.of(testResult));
        
        // Act
        Optional<TestResult> result = gradeService.getTestResultByStudentAndTest(1L, 1L);
        
        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testResult);
        
        verify(testResultRepository).findByStudentIdAndTestId(1L, 1L);
    }
    
    @Test
    void existsTestResult_ReturnsTrue_WhenResultExists() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(testResultRepository.existsByStudentIdAndTestId(1L, 1L)).thenReturn(true);
        
        // Act
        boolean exists = gradeService.existsTestResult(1L, 1L);
        
        // Assert
        assertThat(exists).isTrue();
        
        verify(testResultRepository).existsByStudentIdAndTestId(1L, 1L);
    }
    
    @Test
    void existsTestResult_ReturnsFalse_WhenResultDoesNotExist() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(testResultRepository.existsByStudentIdAndTestId(1L, 1L)).thenReturn(false);
        
        // Act
        boolean exists = gradeService.existsTestResult(1L, 1L);
        
        // Assert
        assertThat(exists).isFalse();
        
        verify(testResultRepository).existsByStudentIdAndTestId(1L, 1L);
    }
    
    @Test
    void countTestResultsByTest_ReturnsCount() {
        // Arrange
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(testResultRepository.countByTestId(1L)).thenReturn(25L);
        
        // Act
        long count = gradeService.countTestResultsByTest(1L);
        
        // Assert
        assertThat(count).isEqualTo(25L);
        
        verify(testResultRepository).countByTestId(1L);
    }
    
    @Test
    void countCompletedTestResults_ReturnsCount() {
        // Arrange
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(testResultRepository.countByTestIdAndRawResultIsNotNull(1L)).thenReturn(20L);
        
        // Act
        long count = gradeService.countCompletedTestResults(1L);
        
        // Assert
        assertThat(count).isEqualTo(20L);
        
        verify(testResultRepository).countByTestIdAndRawResultIsNotNull(1L);
    }
    
    @Test
    void deleteTestResult_DeletesResult() {
        // Arrange
        testResult.setId(1L);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(testResultRepository.findById(1L)).thenReturn(Optional.of(testResult));
        
        // Act
        gradeService.deleteTestResult(1L);
        
        // Assert
        verify(testResultRepository).deleteById(1L);
    }
    
    @Test
    void deleteTestResult_NotFound_ThrowsException() {
        // Arrange
        when(testResultRepository.findById(1L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> gradeService.deleteTestResult(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Test result not found");
    }
}
