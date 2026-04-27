package com.pe.grademanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pe.grademanagement.dto.BulkTestResultRequest;
import com.pe.grademanagement.dto.TestResultRequest;
import com.pe.grademanagement.entity.*;
import com.pe.grademanagement.repository.StudentRepository;
import com.pe.grademanagement.repository.TestRepository;
import com.pe.grademanagement.service.GradeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for GradeController.
 * Tests REST endpoints for grade entry.
 */
@WebMvcTest(GradeController.class)
class GradeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private GradeService gradeService;
    
    @MockBean
    private StudentRepository studentRepository;
    
    @MockBean
    private TestRepository testRepository;
    
    @MockBean
    private com.pe.grademanagement.util.JwtUtil jwtUtil;
    
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    
    private Student mockStudent;
    private com.pe.grademanagement.entity.Test mockTest;
    private TestResult mockTestResult;
    
    @BeforeEach
    void setUp() {
        Teacher mockTeacher = new Teacher();
        mockTeacher.setId(1L);
        mockTeacher.setUsername("teacher1");
        
        com.pe.grademanagement.entity.Class mockClass = new com.pe.grademanagement.entity.Class();
        mockClass.setId(1L);
        mockClass.setName("10A");
        mockClass.setGradeLevel("י");
        mockClass.setTeacher(mockTeacher);
        
        mockStudent = new Student();
        mockStudent.setId(1L);
        mockStudent.setName("John Doe");
        mockStudent.setStudentId("123456789");
        mockStudent.setGradeLevel("י");
        mockStudent.setClassEntity(mockClass);
        
        mockTest = new com.pe.grademanagement.entity.Test();
        mockTest.setId(1L);
        mockTest.setName("Push-ups");
        mockTest.setCalculationType(CalculationType.RATIO);
        mockTest.setUnitType(UnitType.COUNT);
        mockTest.setMaxValue(new BigDecimal("20"));
        mockTest.setCreatedBy(mockTeacher);
        
        mockTestResult = new TestResult();
        mockTestResult.setId(1L);
        mockTestResult.setStudent(mockStudent);
        mockTestResult.setTest(mockTest);
        mockTestResult.setRawResult(new BigDecimal("15"));
        mockTestResult.setCalculatedGrade(new BigDecimal("75.00"));
    }
    
    @Test
    @WithMockUser(username = "teacher1")
    void testGetTestResultsForClass_ValidRequest_ReturnsResults() throws Exception {
        // Arrange
        List<TestResult> results = Arrays.asList(mockTestResult);
        when(gradeService.getTestResultsForClass(1L, 1L)).thenReturn(results);
        
        // Act & Assert
        mockMvc.perform(get("/api/grades/class/1/test/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].rawResult").value(15))
                .andExpect(jsonPath("$[0].calculatedGrade").value(75.00));
        
        verify(gradeService, times(1)).getTestResultsForClass(1L, 1L);
    }
    
    @Test
    @WithMockUser(username = "teacher1")
    void testSaveTestResult_ValidResult_ReturnsCreated() throws Exception {
        // Arrange
        TestResultRequest request = new TestResultRequest(1L, 1L, new BigDecimal("15"));
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(testRepository.findById(1L)).thenReturn(Optional.of(mockTest));
        when(gradeService.saveTestResult(any(TestResult.class))).thenReturn(mockTestResult);
        
        // Act & Assert
        mockMvc.perform(post("/api/grades")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rawResult").value(15))
                .andExpect(jsonPath("$.calculatedGrade").value(75.00));
        
        verify(gradeService, times(1)).saveTestResult(any(TestResult.class));
    }
    
    @Test
    @WithMockUser(username = "teacher1")
    void testSaveTestResult_WithNotes_ReturnsCreated() throws Exception {
        // Arrange
        TestResultRequest request = new TestResultRequest(1L, 1L, new BigDecimal("15"), "Good performance");
        
        TestResult resultWithNotes = new TestResult();
        resultWithNotes.setId(1L);
        resultWithNotes.setStudent(mockStudent);
        resultWithNotes.setTest(mockTest);
        resultWithNotes.setRawResult(new BigDecimal("15"));
        resultWithNotes.setCalculatedGrade(new BigDecimal("75.00"));
        resultWithNotes.setNotes("Good performance");
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(testRepository.findById(1L)).thenReturn(Optional.of(mockTest));
        when(gradeService.saveTestResult(any(TestResult.class))).thenReturn(resultWithNotes);
        
        // Act & Assert
        mockMvc.perform(post("/api/grades")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notes").value("Good performance"));
        
        verify(gradeService, times(1)).saveTestResult(any(TestResult.class));
    }
    
    @Test
    @WithMockUser(username = "teacher1")
    void testSaveTestResult_NullRawResult_ReturnsCreated() throws Exception {
        // Arrange
        TestResultRequest request = new TestResultRequest(1L, 1L, null, "Not tested");
        
        TestResult resultWithoutRaw = new TestResult();
        resultWithoutRaw.setId(1L);
        resultWithoutRaw.setStudent(mockStudent);
        resultWithoutRaw.setTest(mockTest);
        resultWithoutRaw.setRawResult(null);
        resultWithoutRaw.setCalculatedGrade(BigDecimal.ZERO);
        resultWithoutRaw.setNotes("Not tested");
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(testRepository.findById(1L)).thenReturn(Optional.of(mockTest));
        when(gradeService.saveTestResult(any(TestResult.class))).thenReturn(resultWithoutRaw);
        
        // Act & Assert
        mockMvc.perform(post("/api/grades")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calculatedGrade").value(0));
        
        verify(gradeService, times(1)).saveTestResult(any(TestResult.class));
    }
    
    @Test
    @WithMockUser(username = "teacher1")
    void testSaveTestResult_StudentNotFound_ReturnsBadRequest() throws Exception {
        // Arrange
        TestResultRequest request = new TestResultRequest(999L, 1L, new BigDecimal("15"));
        
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(post("/api/grades")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
    
    @Test
    @WithMockUser(username = "teacher1")
    void testSaveTestResult_TestNotFound_ReturnsBadRequest() throws Exception {
        // Arrange
        TestResultRequest request = new TestResultRequest(1L, 999L, new BigDecimal("15"));
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(testRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(post("/api/grades")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
    
    @Test
    @WithMockUser(username = "teacher1")
    void testBulkSaveTestResults_ValidResults_ReturnsCreated() throws Exception {
        // Arrange
        TestResultRequest request1 = new TestResultRequest(1L, 1L, new BigDecimal("15"));
        TestResultRequest request2 = new TestResultRequest(2L, 1L, new BigDecimal("18"));
        BulkTestResultRequest bulkRequest = new BulkTestResultRequest(Arrays.asList(request1, request2));
        
        Student student2 = new Student();
        student2.setId(2L);
        student2.setName("Jane Doe");
        student2.setClassEntity(mockStudent.getClassEntity());
        
        TestResult result2 = new TestResult();
        result2.setId(2L);
        result2.setStudent(student2);
        result2.setTest(mockTest);
        result2.setRawResult(new BigDecimal("18"));
        result2.setCalculatedGrade(new BigDecimal("90.00"));
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(mockStudent));
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student2));
        when(testRepository.findById(1L)).thenReturn(Optional.of(mockTest));
        when(gradeService.bulkSaveTestResults(any(List.class)))
                .thenReturn(Arrays.asList(mockTestResult, result2));
        
        // Act & Assert
        mockMvc.perform(post("/api/grades/bulk")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
        
        verify(gradeService, times(1)).bulkSaveTestResults(any(List.class));
    }
    
    @Test
    @WithMockUser(username = "teacher1")
    void testBulkSaveTestResults_EmptyList_ReturnsBadRequest() throws Exception {
        // Arrange
        BulkTestResultRequest bulkRequest = new BulkTestResultRequest(Arrays.asList());
        
        // Act & Assert
        mockMvc.perform(post("/api/grades/bulk")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bulkRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testSaveTestResult_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Arrange
        TestResultRequest request = new TestResultRequest(1L, 1L, new BigDecimal("15"));
        
        // Act & Assert
        mockMvc.perform(post("/api/grades")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
