package com.pe.grademanagement.service;

import com.pe.grademanagement.entity.*;
import com.pe.grademanagement.repository.ClassRepository;
import com.pe.grademanagement.repository.TestAssignmentRepository;
import com.pe.grademanagement.repository.TestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TestService.
 * Tests test configuration management and assignment functionality.
 */
@ExtendWith(MockitoExtension.class)
class TestServiceTest {
    
    @Mock
    private TestRepository testRepository;
    
    @Mock
    private TestAssignmentRepository testAssignmentRepository;
    
    @Mock
    private ClassRepository classRepository;
    
    @InjectMocks
    private TestService testService;
    
    private Teacher teacher;
    private com.pe.grademanagement.entity.Class class1;
    private com.pe.grademanagement.entity.Class class2;
    
    @BeforeEach
    void setUp() {
        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setUsername("teacher1");
        teacher.setFullName("Test Teacher");
        
        class1 = new com.pe.grademanagement.entity.Class();
        class1.setId(1L);
        class1.setName("Class A");
        class1.setGradeLevel("י");
        class1.setTeacher(teacher);
        
        class2 = new com.pe.grademanagement.entity.Class();
        class2.setId(2L);
        class2.setName("Class B");
        class2.setGradeLevel("י");
        class2.setTeacher(teacher);
    }
    
    @Test
    void testCreateTest_RatioType_Valid() {
        // Arrange
        com.pe.grademanagement.entity.Test test = new com.pe.grademanagement.entity.Test();
        test.setName("Push-ups");
        test.setCalculationType(CalculationType.RATIO);
        test.setUnitType(UnitType.COUNT);
        test.setMaxValue(new BigDecimal("20"));
        test.setCreatedBy(teacher);
        
        when(testRepository.save(any(com.pe.grademanagement.entity.Test.class))).thenReturn(test);
        
        // Act
        com.pe.grademanagement.entity.Test result = testService.createTest(test);
        
        // Assert
        assertNotNull(result);
        assertEquals("Push-ups", result.getName());
        assertEquals(CalculationType.RATIO, result.getCalculationType());
        verify(testRepository, times(1)).save(test);
    }
    
    @Test
    void testCreateTest_RatioType_MissingMaxValue() {
        // Arrange
        com.pe.grademanagement.entity.Test test = new com.pe.grademanagement.entity.Test();
        test.setName("Push-ups");
        test.setCalculationType(CalculationType.RATIO);
        test.setUnitType(UnitType.COUNT);
        test.setCreatedBy(teacher);
        // maxValue is null
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> testService.createTest(test)
        );
        assertTrue(exception.getMessage().contains("RATIO calculation requires maxValue"));
        verify(testRepository, never()).save(any());
    }
    
    @Test
    void testCreateTest_PenaltyType_Valid() {
        // Arrange
        com.pe.grademanagement.entity.Test test = new com.pe.grademanagement.entity.Test();
        test.setName("1500m Run");
        test.setCalculationType(CalculationType.PENALTY);
        test.setUnitType(UnitType.TIME);
        test.setTargetValue(new BigDecimal("10.0"));
        test.setPenaltyPerUnit(new BigDecimal("5.0"));
        test.setCreatedBy(teacher);
        
        when(testRepository.save(any(com.pe.grademanagement.entity.Test.class))).thenReturn(test);
        
        // Act
        com.pe.grademanagement.entity.Test result = testService.createTest(test);
        
        // Assert
        assertNotNull(result);
        assertEquals("1500m Run", result.getName());
        assertEquals(CalculationType.PENALTY, result.getCalculationType());
        verify(testRepository, times(1)).save(test);
    }
    
    @Test
    void testCreateTest_PenaltyType_MissingTargetValue() {
        // Arrange
        com.pe.grademanagement.entity.Test test = new com.pe.grademanagement.entity.Test();
        test.setName("1500m Run");
        test.setCalculationType(CalculationType.PENALTY);
        test.setUnitType(UnitType.TIME);
        test.setPenaltyPerUnit(new BigDecimal("5.0"));
        test.setCreatedBy(teacher);
        // targetValue is null
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> testService.createTest(test)
        );
        assertTrue(exception.getMessage().contains("PENALTY calculation requires targetValue"));
        verify(testRepository, never()).save(any());
    }
    
    @Test
    void testCreateTest_PenaltyType_MissingPenaltyPerUnit() {
        // Arrange
        com.pe.grademanagement.entity.Test test = new com.pe.grademanagement.entity.Test();
        test.setName("1500m Run");
        test.setCalculationType(CalculationType.PENALTY);
        test.setUnitType(UnitType.TIME);
        test.setTargetValue(new BigDecimal("10.0"));
        test.setCreatedBy(teacher);
        // penaltyPerUnit is null
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> testService.createTest(test)
        );
        assertTrue(exception.getMessage().contains("PENALTY calculation requires penaltyPerUnit"));
        verify(testRepository, never()).save(any());
    }
    
    @Test
    void testUpdateTest_Valid() {
        // Arrange
        com.pe.grademanagement.entity.Test existingTest = new com.pe.grademanagement.entity.Test();
        existingTest.setId(1L);
        existingTest.setName("Push-ups");
        existingTest.setCalculationType(CalculationType.RATIO);
        existingTest.setUnitType(UnitType.COUNT);
        existingTest.setMaxValue(new BigDecimal("20"));
        existingTest.setCreatedBy(teacher);
        
        com.pe.grademanagement.entity.Test updatedTest = new com.pe.grademanagement.entity.Test();
        updatedTest.setName("Push-ups Updated");
        updatedTest.setCalculationType(CalculationType.RATIO);
        updatedTest.setUnitType(UnitType.COUNT);
        updatedTest.setMaxValue(new BigDecimal("25"));
        updatedTest.setCreatedBy(teacher);
        
        when(testRepository.findById(1L)).thenReturn(Optional.of(existingTest));
        when(testRepository.save(any(com.pe.grademanagement.entity.Test.class))).thenReturn(existingTest);
        
        // Act
        com.pe.grademanagement.entity.Test result = testService.updateTest(1L, updatedTest);
        
        // Assert
        assertNotNull(result);
        assertEquals("Push-ups Updated", result.getName());
        assertEquals(new BigDecimal("25"), result.getMaxValue());
        verify(testRepository, times(1)).save(existingTest);
    }
    
    @Test
    void testUpdateTest_NotFound() {
        // Arrange
        com.pe.grademanagement.entity.Test updatedTest = new com.pe.grademanagement.entity.Test();
        updatedTest.setName("Push-ups");
        updatedTest.setCalculationType(CalculationType.RATIO);
        updatedTest.setUnitType(UnitType.COUNT);
        updatedTest.setMaxValue(new BigDecimal("20"));
        updatedTest.setCreatedBy(teacher);
        
        when(testRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> testService.updateTest(999L, updatedTest)
        );
        assertTrue(exception.getMessage().contains("Test not found"));
        verify(testRepository, never()).save(any());
    }
    
    @Test
    void testAssignTestToClasses_Valid() {
        // Arrange
        com.pe.grademanagement.entity.Test test = new com.pe.grademanagement.entity.Test();
        test.setId(1L);
        test.setName("Push-ups");
        test.setCalculationType(CalculationType.RATIO);
        test.setUnitType(UnitType.COUNT);
        test.setMaxValue(new BigDecimal("20"));
        test.setCreatedBy(teacher);
        
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(classRepository.findById(1L)).thenReturn(Optional.of(class1));
        when(classRepository.findById(2L)).thenReturn(Optional.of(class2));
        when(testAssignmentRepository.existsByTestIdAndClassEntityId(1L, 1L)).thenReturn(false);
        when(testAssignmentRepository.existsByTestIdAndClassEntityId(1L, 2L)).thenReturn(false);
        
        // Act
        testService.assignTestToClasses(1L, Arrays.asList(1L, 2L));
        
        // Assert
        verify(testAssignmentRepository, times(2)).save(any(TestAssignment.class));
    }
    
    @Test
    void testAssignTestToClasses_SkipExisting() {
        // Arrange
        com.pe.grademanagement.entity.Test test = new com.pe.grademanagement.entity.Test();
        test.setId(1L);
        test.setName("Push-ups");
        test.setCalculationType(CalculationType.RATIO);
        test.setUnitType(UnitType.COUNT);
        test.setMaxValue(new BigDecimal("20"));
        test.setCreatedBy(teacher);
        
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(classRepository.findById(1L)).thenReturn(Optional.of(class1));
        when(classRepository.findById(2L)).thenReturn(Optional.of(class2));
        when(testAssignmentRepository.existsByTestIdAndClassEntityId(1L, 1L)).thenReturn(true); // Already assigned
        when(testAssignmentRepository.existsByTestIdAndClassEntityId(1L, 2L)).thenReturn(false);
        
        // Act
        testService.assignTestToClasses(1L, Arrays.asList(1L, 2L));
        
        // Assert
        verify(testAssignmentRepository, times(1)).save(any(TestAssignment.class)); // Only one new assignment
    }
    
    @Test
    void testAssignTestToClasses_TestNotFound() {
        // Arrange
        when(testRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> testService.assignTestToClasses(999L, Arrays.asList(1L, 2L))
        );
        assertTrue(exception.getMessage().contains("Test not found"));
        verify(testAssignmentRepository, never()).save(any());
    }
    
    @Test
    void testAssignTestToClasses_ClassNotFound() {
        // Arrange
        com.pe.grademanagement.entity.Test test = new com.pe.grademanagement.entity.Test();
        test.setId(1L);
        test.setName("Push-ups");
        test.setCalculationType(CalculationType.RATIO);
        test.setUnitType(UnitType.COUNT);
        test.setMaxValue(new BigDecimal("20"));
        test.setCreatedBy(teacher);
        
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(classRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> testService.assignTestToClasses(1L, Arrays.asList(999L))
        );
        assertTrue(exception.getMessage().contains("Class not found"));
    }
    
    @Test
    void testGetTestsForClass() {
        // Arrange
        com.pe.grademanagement.entity.Test test1 = new com.pe.grademanagement.entity.Test();
        test1.setId(1L);
        test1.setName("Push-ups");
        
        com.pe.grademanagement.entity.Test test2 = new com.pe.grademanagement.entity.Test();
        test2.setId(2L);
        test2.setName("Sit-ups");
        
        when(testRepository.findByClassIdOrderByName(1L)).thenReturn(Arrays.asList(test1, test2));
        
        // Act
        List<com.pe.grademanagement.entity.Test> result = testService.getTestsForClass(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Push-ups", result.get(0).getName());
        assertEquals("Sit-ups", result.get(1).getName());
    }
    
    @Test
    void testGetTestsByTeacher() {
        // Arrange
        com.pe.grademanagement.entity.Test test1 = new com.pe.grademanagement.entity.Test();
        test1.setId(1L);
        test1.setName("Push-ups");
        
        com.pe.grademanagement.entity.Test test2 = new com.pe.grademanagement.entity.Test();
        test2.setId(2L);
        test2.setName("Sit-ups");
        
        when(testRepository.findByCreatedByIdOrderByName(1L)).thenReturn(Arrays.asList(test1, test2));
        
        // Act
        List<com.pe.grademanagement.entity.Test> result = testService.getTestsByTeacher(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }
    
    @Test
    void testAssignTestToGradeLevel() {
        // Arrange
        com.pe.grademanagement.entity.Test test = new com.pe.grademanagement.entity.Test();
        test.setId(1L);
        test.setName("Push-ups");
        test.setCalculationType(CalculationType.RATIO);
        test.setUnitType(UnitType.COUNT);
        test.setMaxValue(new BigDecimal("20"));
        test.setCreatedBy(teacher);
        
        when(testRepository.existsById(1L)).thenReturn(true);
        when(testRepository.findById(1L)).thenReturn(Optional.of(test));
        when(classRepository.findByTeacherIdAndGradeLevel(1L, "י")).thenReturn(Arrays.asList(class1, class2));
        when(classRepository.findById(1L)).thenReturn(Optional.of(class1));
        when(classRepository.findById(2L)).thenReturn(Optional.of(class2));
        when(testAssignmentRepository.existsByTestIdAndClassEntityId(1L, 1L)).thenReturn(false);
        when(testAssignmentRepository.existsByTestIdAndClassEntityId(1L, 2L)).thenReturn(false);
        
        // Act
        testService.assignTestToGradeLevel(1L, "י", 1L);
        
        // Assert
        verify(testAssignmentRepository, times(2)).save(any(TestAssignment.class));
    }
    
    @Test
    void testDeleteTest() {
        // Arrange
        when(testRepository.existsById(1L)).thenReturn(true);
        
        // Act
        testService.deleteTest(1L);
        
        // Assert
        verify(testRepository, times(1)).deleteById(1L);
    }
    
    @Test
    void testDeleteTest_NotFound() {
        // Arrange
        when(testRepository.existsById(999L)).thenReturn(false);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> testService.deleteTest(999L)
        );
        assertTrue(exception.getMessage().contains("Test not found"));
        verify(testRepository, never()).deleteById(any());
    }
    
    @Test
    void testRemoveTestAssignment() {
        // Arrange
        TestAssignment assignment = new TestAssignment();
        assignment.setId(1L);
        
        when(testAssignmentRepository.findByTestIdAndClassEntityId(1L, 1L))
                .thenReturn(Optional.of(assignment));
        
        // Act
        testService.removeTestAssignment(1L, 1L);
        
        // Assert
        verify(testAssignmentRepository, times(1)).delete(assignment);
    }
    
    @Test
    void testGetClassesForTest() {
        // Arrange
        com.pe.grademanagement.entity.Test test = new com.pe.grademanagement.entity.Test();
        test.setId(1L);
        
        TestAssignment assignment1 = new TestAssignment(test, class1);
        TestAssignment assignment2 = new TestAssignment(test, class2);
        
        when(testAssignmentRepository.findByTestId(1L)).thenReturn(Arrays.asList(assignment1, assignment2));
        
        // Act
        List<com.pe.grademanagement.entity.Class> result = testService.getClassesForTest(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }
}
