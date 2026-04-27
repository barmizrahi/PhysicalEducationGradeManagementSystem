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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GradeService authorization checks.
 * Tests that teachers can only access test results for their own students and tests.
 * 
 * Requirements:
 * - 13.2: Associate each teacher with their assigned classes
 * - 13.3: Display only classes assigned to authenticated teacher
 * - 13.4: Prevent teachers from accessing or modifying data for classes not assigned to them
 * - 11.2: Isolate data when multiple teachers access different classes
 */
@ExtendWith(MockitoExtension.class)
class GradeServiceAuthorizationTest {
    
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
    
    private Teacher teacher1;
    private Teacher teacher2;
    private com.pe.grademanagement.entity.Class class1;
    private com.pe.grademanagement.entity.Class class2;
    private Student student1;
    private Student student2;
    private com.pe.grademanagement.entity.Test test1;
    private com.pe.grademanagement.entity.Test test2;
    private TestResult testResult1;
    private TestResult testResult2;
    
    @BeforeEach
    void setUp() {
        // Create test teachers
        teacher1 = new Teacher();
        teacher1.setId(1L);
        teacher1.setUsername("teacher1");
        teacher1.setFullName("Teacher One");
        
        teacher2 = new Teacher();
        teacher2.setId(2L);
        teacher2.setUsername("teacher2");
        teacher2.setFullName("Teacher Two");
        
        // Create test classes
        class1 = new com.pe.grademanagement.entity.Class();
        class1.setId(1L);
        class1.setName("א1");
        class1.setGradeLevel("י");
        class1.setTeacher(teacher1);
        
        class2 = new com.pe.grademanagement.entity.Class();
        class2.setId(2L);
        class2.setName("ב1");
        class2.setGradeLevel("י");
        class2.setTeacher(teacher2);
        
        // Create test students
        student1 = new Student();
        student1.setId(1L);
        student1.setName("דוד כהן");
        student1.setStudentId("123456789");
        student1.setGradeLevel("י");
        student1.setClassEntity(class1);
        
        student2 = new Student();
        student2.setId(2L);
        student2.setName("שרה לevi");
        student2.setStudentId("987654321");
        student2.setGradeLevel("י");
        student2.setClassEntity(class2);
        
        // Create tests
        test1 = new com.pe.grademanagement.entity.Test();
        test1.setId(1L);
        test1.setName("1500m Run");
        test1.setCalculationType(CalculationType.PENALTY);
        test1.setUnitType(UnitType.TIME);
        test1.setCreatedBy(teacher1);
        
        test2 = new com.pe.grademanagement.entity.Test();
        test2.setId(2L);
        test2.setName("Push-ups");
        test2.setCalculationType(CalculationType.RATIO);
        test2.setUnitType(UnitType.COUNT);
        test2.setCreatedBy(teacher2);
        
        // Create test results
        testResult1 = new TestResult();
        testResult1.setId(1L);
        testResult1.setStudent(student1);
        testResult1.setTest(test1);
        testResult1.setRawResult(new BigDecimal("10.5"));
        testResult1.setCalculatedGrade(new BigDecimal("85.0"));
        
        testResult2 = new TestResult();
        testResult2.setId(2L);
        testResult2.setStudent(student2);
        testResult2.setTest(test2);
        testResult2.setRawResult(new BigDecimal("20"));
        testResult2.setCalculatedGrade(new BigDecimal("90.0"));
    }
    
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
    
    private void authenticateAsTeacher(Teacher teacher) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            teacher.getUsername(),
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(teacherRepository.findByUsername(teacher.getUsername())).thenReturn(Optional.of(teacher));
    }
    
    @Test
    void testSaveTestResult_OwnStudentAndTest_Success() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        TestResult newResult = new TestResult();
        newResult.setStudent(student1);
        newResult.setTest(test1);
        newResult.setRawResult(new BigDecimal("11.0"));
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test1));
        when(gradeCalculator.calculateGrade(any(), any())).thenReturn(new BigDecimal("80.0"));
        when(testResultRepository.findByStudentIdAndTestId(1L, 1L)).thenReturn(Optional.empty());
        when(testResultRepository.save(any(TestResult.class))).thenReturn(newResult);
        
        // Act
        TestResult result = gradeService.saveTestResult(newResult);
        
        // Assert
        assertThat(result).isNotNull();
        verify(testResultRepository).save(any(TestResult.class));
    }
    
    @Test
    void testSaveTestResult_OtherTeachersStudent_ThrowsAccessDeniedException() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        TestResult newResult = new TestResult();
        newResult.setStudent(student2); // student2 belongs to teacher2
        newResult.setTest(test1);
        newResult.setRawResult(new BigDecimal("11.0"));
        
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student2));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test1));
        
        // Act & Assert
        assertThatThrownBy(() -> gradeService.saveTestResult(newResult))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Teacher does not have access to student with ID: 2");
        
        verify(testResultRepository, never()).save(any(TestResult.class));
    }
    
    @Test
    void testSaveTestResult_OtherTeachersTest_ThrowsAccessDeniedException() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        TestResult newResult = new TestResult();
        newResult.setStudent(student1);
        newResult.setTest(test2); // test2 belongs to teacher2
        newResult.setRawResult(new BigDecimal("20"));
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(testRepository.findById(2L)).thenReturn(Optional.of(test2));
        
        // Act & Assert
        assertThatThrownBy(() -> gradeService.saveTestResult(newResult))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Teacher does not have access to test with ID: 2");
        
        verify(testResultRepository, never()).save(any(TestResult.class));
    }
    
    @Test
    void testGetTestResultsForClass_OwnTest_Success() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(testRepository.findById(1L)).thenReturn(Optional.of(test1));
        when(testResultRepository.findByClassIdAndTestIdOrderByStudentName(1L, 1L))
            .thenReturn(Collections.singletonList(testResult1));
        
        // Act
        var result = gradeService.getTestResultsForClass(1L, 1L);
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }
    
    @Test
    void testGetTestResultsForClass_OtherTeachersTest_ThrowsAccessDeniedException() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(testRepository.findById(2L)).thenReturn(Optional.of(test2));
        
        // Act & Assert
        assertThatThrownBy(() -> gradeService.getTestResultsForClass(1L, 2L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Teacher does not have access to test with ID: 2");
    }
    
    @Test
    void testGetTestResultById_OwnResult_Success() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(testResultRepository.findById(1L)).thenReturn(Optional.of(testResult1));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test1));
        
        // Act
        Optional<TestResult> result = gradeService.getTestResultById(1L);
        
        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }
    
    @Test
    void testGetTestResultById_OtherTeachersResult_ThrowsAccessDeniedException() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(testResultRepository.findById(2L)).thenReturn(Optional.of(testResult2));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student2));
        
        // Act & Assert
        assertThatThrownBy(() -> gradeService.getTestResultById(2L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Teacher does not have access to student with ID: 2");
    }
    
    @Test
    void testGetTestResultByStudentAndTest_OwnStudentAndTest_Success() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test1));
        when(testResultRepository.findByStudentIdAndTestId(1L, 1L))
            .thenReturn(Optional.of(testResult1));
        
        // Act
        Optional<TestResult> result = gradeService.getTestResultByStudentAndTest(1L, 1L);
        
        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }
    
    @Test
    void testGetTestResultByStudentAndTest_OtherTeachersStudent_ThrowsAccessDeniedException() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student2));
        
        // Act & Assert
        assertThatThrownBy(() -> gradeService.getTestResultByStudentAndTest(2L, 1L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Teacher does not have access to student with ID: 2");
    }
    
    @Test
    void testGetTestResultsByStudent_OwnStudent_Success() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(testResultRepository.findByStudentId(1L))
            .thenReturn(Collections.singletonList(testResult1));
        
        // Act
        var result = gradeService.getTestResultsByStudent(1L);
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }
    
    @Test
    void testGetTestResultsByStudent_OtherTeachersStudent_ThrowsAccessDeniedException() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student2));
        
        // Act & Assert
        assertThatThrownBy(() -> gradeService.getTestResultsByStudent(2L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Teacher does not have access to student with ID: 2");
    }
    
    @Test
    void testGetTestResultsByTest_OwnTest_Success() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(testRepository.findById(1L)).thenReturn(Optional.of(test1));
        when(testResultRepository.findByTestId(1L))
            .thenReturn(Collections.singletonList(testResult1));
        
        // Act
        var result = gradeService.getTestResultsByTest(1L);
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }
    
    @Test
    void testGetTestResultsByTest_OtherTeachersTest_ThrowsAccessDeniedException() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(testRepository.findById(2L)).thenReturn(Optional.of(test2));
        
        // Act & Assert
        assertThatThrownBy(() -> gradeService.getTestResultsByTest(2L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Teacher does not have access to test with ID: 2");
    }
    
    @Test
    void testDeleteTestResult_OwnResult_Success() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(testResultRepository.findById(1L)).thenReturn(Optional.of(testResult1));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test1));
        
        // Act
        gradeService.deleteTestResult(1L);
        
        // Assert
        verify(testResultRepository).deleteById(1L);
    }
    
    @Test
    void testDeleteTestResult_OtherTeachersResult_ThrowsAccessDeniedException() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(testResultRepository.findById(2L)).thenReturn(Optional.of(testResult2));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student2));
        
        // Act & Assert
        assertThatThrownBy(() -> gradeService.deleteTestResult(2L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Teacher does not have access to student with ID: 2");
        
        verify(testResultRepository, never()).deleteById(2L);
    }
    
    @Test
    void testDeleteTestResultsByStudent_OwnStudent_Success() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        
        // Act
        gradeService.deleteTestResultsByStudent(1L);
        
        // Assert
        verify(testResultRepository).deleteByStudentId(1L);
    }
    
    @Test
    void testDeleteTestResultsByStudent_OtherTeachersStudent_ThrowsAccessDeniedException() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student2));
        
        // Act & Assert
        assertThatThrownBy(() -> gradeService.deleteTestResultsByStudent(2L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Teacher does not have access to student with ID: 2");
        
        verify(testResultRepository, never()).deleteByStudentId(2L);
    }
    
    @Test
    void testDeleteTestResultsByTest_OwnTest_Success() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(testRepository.findById(1L)).thenReturn(Optional.of(test1));
        
        // Act
        gradeService.deleteTestResultsByTest(1L);
        
        // Assert
        verify(testResultRepository).deleteByTestId(1L);
    }
    
    @Test
    void testDeleteTestResultsByTest_OtherTeachersTest_ThrowsAccessDeniedException() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(testRepository.findById(2L)).thenReturn(Optional.of(test2));
        
        // Act & Assert
        assertThatThrownBy(() -> gradeService.deleteTestResultsByTest(2L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Teacher does not have access to test with ID: 2");
        
        verify(testResultRepository, never()).deleteByTestId(2L);
    }
    
    @Test
    void testExistsTestResult_OwnStudentAndTest_Success() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(testRepository.findById(1L)).thenReturn(Optional.of(test1));
        when(testResultRepository.existsByStudentIdAndTestId(1L, 1L)).thenReturn(true);
        
        // Act
        boolean exists = gradeService.existsTestResult(1L, 1L);
        
        // Assert
        assertThat(exists).isTrue();
    }
    
    @Test
    void testCountTestResultsByTest_OwnTest_Success() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(testRepository.findById(1L)).thenReturn(Optional.of(test1));
        when(testResultRepository.countByTestId(1L)).thenReturn(10L);
        
        // Act
        long count = gradeService.countTestResultsByTest(1L);
        
        // Assert
        assertThat(count).isEqualTo(10L);
    }
    
    @Test
    void testCountCompletedTestResults_OwnTest_Success() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(testRepository.findById(1L)).thenReturn(Optional.of(test1));
        when(testResultRepository.countByTestIdAndRawResultIsNotNull(1L)).thenReturn(8L);
        
        // Act
        long count = gradeService.countCompletedTestResults(1L);
        
        // Assert
        assertThat(count).isEqualTo(8L);
    }
}
