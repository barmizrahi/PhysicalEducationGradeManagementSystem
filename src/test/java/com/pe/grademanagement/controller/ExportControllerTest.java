package com.pe.grademanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pe.grademanagement.dto.ExportRequest;
import com.pe.grademanagement.service.ExportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ExportController.
 * Tests REST endpoints for grade export.
 */
@WebMvcTest(ExportController.class)
class ExportControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ExportService exportService;
    
    @MockBean
    private com.pe.grademanagement.util.JwtUtil jwtUtil;
    
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    
    @Test
    @WithMockUser(username = "teacher1")
    void testExportToExcel_ValidRequest_ReturnsExcelFile() throws Exception {
        // Arrange
        List<Long> classIds = Arrays.asList(1L, 2L);
        List<Long> testIds = Arrays.asList(1L, 2L, 3L);
        ExportRequest request = new ExportRequest(classIds, testIds, false);
        
        byte[] mockExcelData = "Mock Excel Data".getBytes();
        when(exportService.exportGrades(any(List.class), any(List.class), anyBoolean()))
                .thenReturn(mockExcelData);
        
        // Act & Assert
        mockMvc.perform(post("/api/export/excel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Type", "application/octet-stream"))
                .andExpect(content().bytes(mockExcelData));
        
        verify(exportService, times(1)).exportGrades(classIds, testIds, false);
    }
    
    @Test
    @WithMockUser(username = "teacher1")
    void testExportToExcel_WithNotes_ReturnsExcelFile() throws Exception {
        // Arrange
        List<Long> classIds = Arrays.asList(1L);
        List<Long> testIds = Arrays.asList(1L);
        ExportRequest request = new ExportRequest(classIds, testIds, true);
        
        byte[] mockExcelData = "Mock Excel Data with Notes".getBytes();
        when(exportService.exportGrades(any(List.class), any(List.class), anyBoolean()))
                .thenReturn(mockExcelData);
        
        // Act & Assert
        mockMvc.perform(post("/api/export/excel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().bytes(mockExcelData));
        
        verify(exportService, times(1)).exportGrades(classIds, testIds, true);
    }
    
    @Test
    @WithMockUser(username = "teacher1")
    void testExportToExcel_EmptyClassIds_ReturnsBadRequest() throws Exception {
        // Arrange
        List<Long> classIds = Arrays.asList();
        List<Long> testIds = Arrays.asList(1L);
        ExportRequest request = new ExportRequest(classIds, testIds, false);
        
        // Act & Assert
        mockMvc.perform(post("/api/export/excel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "teacher1")
    void testExportToExcel_EmptyTestIds_ReturnsBadRequest() throws Exception {
        // Arrange
        List<Long> classIds = Arrays.asList(1L);
        List<Long> testIds = Arrays.asList();
        ExportRequest request = new ExportRequest(classIds, testIds, false);
        
        // Act & Assert
        mockMvc.perform(post("/api/export/excel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "teacher1")
    void testExportToExcel_InvalidClassId_ReturnsBadRequest() throws Exception {
        // Arrange
        List<Long> classIds = Arrays.asList(999L);
        List<Long> testIds = Arrays.asList(1L);
        ExportRequest request = new ExportRequest(classIds, testIds, false);
        
        when(exportService.exportGrades(any(List.class), any(List.class), anyBoolean()))
                .thenThrow(new IllegalArgumentException("Class not found with ID: 999"));
        
        // Act & Assert
        mockMvc.perform(post("/api/export/excel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
    
    @Test
    @WithMockUser(username = "teacher2")
    void testExportToExcel_UnauthorizedAccess_ReturnsForbidden() throws Exception {
        // Arrange
        List<Long> classIds = Arrays.asList(1L);
        List<Long> testIds = Arrays.asList(1L);
        ExportRequest request = new ExportRequest(classIds, testIds, false);
        
        when(exportService.exportGrades(any(List.class), any(List.class), anyBoolean()))
                .thenThrow(new AccessDeniedException("You do not have access to class with ID: 1"));
        
        // Act & Assert
        mockMvc.perform(post("/api/export/excel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").exists());
    }
    
    @Test
    @WithMockUser(username = "teacher1")
    void testExportToExcel_FilenameContainsTimestamp() throws Exception {
        // Arrange
        List<Long> classIds = Arrays.asList(1L);
        List<Long> testIds = Arrays.asList(1L);
        ExportRequest request = new ExportRequest(classIds, testIds, false);
        
        byte[] mockExcelData = "Mock Excel Data".getBytes();
        when(exportService.exportGrades(any(List.class), any(List.class), anyBoolean()))
                .thenReturn(mockExcelData);
        
        // Act & Assert
        mockMvc.perform(post("/api/export/excel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", 
                        org.hamcrest.Matchers.containsString("grades_export_")))
                .andExpect(header().string("Content-Disposition", 
                        org.hamcrest.Matchers.containsString(".xlsx")));
    }
    
    @Test
    void testExportToExcel_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Arrange
        List<Long> classIds = Arrays.asList(1L);
        List<Long> testIds = Arrays.asList(1L);
        ExportRequest request = new ExportRequest(classIds, testIds, false);
        
        // Act & Assert
        mockMvc.perform(post("/api/export/excel")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
