package com.pe.grademanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pe.grademanagement.dto.TestAssignmentRequest;
import com.pe.grademanagement.dto.TestRequest;
import com.pe.grademanagement.entity.CalculationType;
import com.pe.grademanagement.entity.Teacher;
import com.pe.grademanagement.entity.UnitType;
import com.pe.grademanagement.repository.TeacherRepository;
import com.pe.grademanagement.service.TestService;
import com.pe.grademanagement.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TestController.
 * Tests REST endpoints for test management.
 */
@WebMvcTest(TestController.class)
class TestControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private TestService testService;
    
    @MockBean
    private TeacherRepository teacherRepository;
    
    @MockBean
    private com.pe.grademanagement.util.JwtUtil jwtUtil;
    
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    
    private Teacher mockTeacher;
    private com.pe.grademanagement.entity.Test mockTest;
    
    @BeforeEach
    void setUp() {
        mockTeacher = new Teacher();
        mockTeacher.setId(1L);
        mockTeacher.setUsername("teacher1");
        mockTeacher.setFullName("Test Teacher");
        
        mockTest = new com.pe.grademanagement.entity.Test();
        mockTest.setId(1L);
        mockTest.setName("Push-ups");
        mockTest.setCalculationType(CalculationType.RATIO);
        mockTest.setUnitType(UnitType.COUNT);
        mockTest.setMaxValue(new BigDecimal("20"));
        mockTest.setCreatedBy(mockTeacher);
    }
    
    @org.junit.jupiter.api.Test
    @WithMockUser(username = "teacher1")
    void testCreateTest_ValidRatioTest_ReturnsCreated() throws Exception {
        // Arrange
        TestRequest request = new TestRequest("Push-ups", CalculationType.RATIO, UnitType.COUNT);
        request.setMaxValue(new BigDecimal("20"));
        
        when(teacherRepository.findByUsername("teacher1")).thenReturn(Optional.of(mockTeacher));
        when(testService.createTest(any(com.pe.grademanagement.entity.Test.class))).thenReturn(mockTest);
        
        // Act & Assert
        mockMvc.perform(post("/api/tests")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Push-ups"))
                .andExpect(jsonPath("$.calculationType").value("RATIO"));
        
        verify(testService, times(1)).createTest(any(com.pe.grademanagement.entity.Test.class));
    }
    
    @org.junit.jupiter.api.Test
    @WithMockUser(username = "teacher1")
    void testCreateTest_ValidPenaltyTest_ReturnsCreated() throws Exception {
        // Arrange
        TestRequest request = new TestRequest("1500m Run", CalculationType.PENALTY, UnitType.TIME);
        request.setTargetValue(new BigDecimal("10.0"));
        request.setPenaltyPerUnit(new BigDecimal("5.0"));
        
        com.pe.grademanagement.entity.Test penaltyTest = new com.pe.grademanagement.entity.Test();
        penaltyTest.setId(2L);
        penaltyTest.setName("1500m Run");
        penaltyTest.setCalculationType(CalculationType.PENALTY);
        penaltyTest.setUnitType(UnitType.TIME);
        penaltyTest.setTargetValue(new BigDecimal("10.0"));
        penaltyTest.setPenaltyPerUnit(new BigDecimal("5.0"));
        penaltyTest.setCreatedBy(mockTeacher);
        
        when(teacherRepository.findByUsername("teacher1")).thenReturn(Optional.of(mockTeacher));
        when(testService.createTest(any(com.pe.grademanagement.entity.Test.class))).thenReturn(penaltyTest);
        
        // Act & Assert
        mockMvc.perform(post("/api/tests")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.calculationType").value("PENALTY"));
        
        verify(testService, times(1)).createTest(any(com.pe.grademanagement.entity.Test.class));
    }
    
    @org.junit.jupiter.api.Test
    @WithMockUser(username = "teacher1")
    void testCreateTest_InvalidConfiguration_ReturnsBadRequest() throws Exception {
        // Arrange
        TestRequest request = new TestRequest("Invalid Test", CalculationType.RATIO, UnitType.COUNT);
        // Missing maxValue for RATIO test
        
        when(teacherRepository.findByUsername("teacher1")).thenReturn(Optional.of(mockTeacher));
        when(testService.createTest(any(com.pe.grademanagement.entity.Test.class)))
                .thenThrow(new IllegalArgumentException("RATIO calculation requires maxValue parameter"));
        
        // Act & Assert
        mockMvc.perform(post("/api/tests")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
    
    @org.junit.jupiter.api.Test
    @WithMockUser(username = "teacher1")
    void testUpdateTest_ValidUpdate_ReturnsOk() throws Exception {
        // Arrange
        TestRequest request = new TestRequest("Push-ups Updated", CalculationType.RATIO, UnitType.COUNT);
        request.setMaxValue(new BigDecimal("25"));
        
        com.pe.grademanagement.entity.Test updatedTest = new com.pe.grademanagement.entity.Test();
        updatedTest.setId(1L);
        updatedTest.setName("Push-ups Updated");
        updatedTest.setCalculationType(CalculationType.RATIO);
        updatedTest.setUnitType(UnitType.COUNT);
        updatedTest.setMaxValue(new BigDecimal("25"));
        updatedTest.setCreatedBy(mockTeacher);
        
        when(teacherRepository.findByUsername("teacher1")).thenReturn(Optional.of(mockTeacher));
        when(testService.getTestById(1L)).thenReturn(Optional.of(mockTest));
        when(testService.updateTest(eq(1L), any(com.pe.grademanagement.entity.Test.class))).thenReturn(updatedTest);
        
        // Act & Assert
        mockMvc.perform(put("/api/tests/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Push-ups Updated"))
                .andExpect(jsonPath("$.maxValue").value(25));
        
        verify(testService, times(1)).updateTest(eq(1L), any(com.pe.grademanagement.entity.Test.class));
    }
    
    @org.junit.jupiter.api.Test
    @WithMockUser(username = "teacher1")
    void testUpdateTest_TestNotFound_ReturnsBadRequest() throws Exception {
        // Arrange
        TestRequest request = new TestRequest("Push-ups", CalculationType.RATIO, UnitType.COUNT);
        request.setMaxValue(new BigDecimal("20"));
        
        when(teacherRepository.findByUsername("teacher1")).thenReturn(Optional.of(mockTeacher));
        when(testService.getTestById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(put("/api/tests/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
    
    @org.junit.jupiter.api.Test
    @WithMockUser(username = "teacher2")
    void testUpdateTest_UnauthorizedTeacher_ReturnsForbidden() throws Exception {
        // Arrange
        Teacher otherTeacher = new Teacher();
        otherTeacher.setId(2L);
        otherTeacher.setUsername("teacher2");
        
        TestRequest request = new TestRequest("Push-ups", CalculationType.RATIO, UnitType.COUNT);
        request.setMaxValue(new BigDecimal("20"));
        
        when(teacherRepository.findByUsername("teacher2")).thenReturn(Optional.of(otherTeacher));
        when(testService.getTestById(1L)).thenReturn(Optional.of(mockTest));
        
        // Act & Assert
        mockMvc.perform(put("/api/tests/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }
    
    @org.junit.jupiter.api.Test
    @WithMockUser(username = "teacher1")
    void testAssignTest_ValidAssignment_ReturnsOk() throws Exception {
        // Arrange
        TestAssignmentRequest request = new TestAssignmentRequest(Arrays.asList(1L, 2L, 3L));
        
        when(teacherRepository.findByUsername("teacher1")).thenReturn(Optional.of(mockTeacher));
        when(testService.getTestById(1L)).thenReturn(Optional.of(mockTest));
        doNothing().when(testService).assignTestToClasses(eq(1L), any(List.class));
        
        // Act & Assert
        mockMvc.perform(post("/api/tests/1/assign")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
        
        verify(testService, times(1)).assignTestToClasses(eq(1L), any(List.class));
    }
    
    @org.junit.jupiter.api.Test
    @WithMockUser(username = "teacher1")
    void testGetTestsForClass_ValidRequest_ReturnsTests() throws Exception {
        // Arrange
        List<com.pe.grademanagement.entity.Test> tests = Arrays.asList(mockTest);
        
        when(teacherRepository.findByUsername("teacher1")).thenReturn(Optional.of(mockTeacher));
        when(testService.getTestsForClass(1L)).thenReturn(tests);
        
        // Act & Assert
        mockMvc.perform(get("/api/tests/class/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Push-ups"));
        
        verify(testService, times(1)).getTestsForClass(1L);
    }
    
    @org.junit.jupiter.api.Test
    void testCreateTest_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Arrange
        TestRequest request = new TestRequest("Push-ups", CalculationType.RATIO, UnitType.COUNT);
        request.setMaxValue(new BigDecimal("20"));
        
        // Act & Assert
        mockMvc.perform(post("/api/tests")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
