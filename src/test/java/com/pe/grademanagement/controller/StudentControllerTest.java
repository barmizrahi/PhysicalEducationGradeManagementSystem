package com.pe.grademanagement.controller;

import com.pe.grademanagement.entity.Class;
import com.pe.grademanagement.entity.Student;
import com.pe.grademanagement.entity.Teacher;
import com.pe.grademanagement.service.StudentService;
import com.pe.grademanagement.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for StudentController.
 * Tests REST endpoints for student management.
 */
@WebMvcTest(StudentController.class)
class StudentControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private StudentService studentService;
    
    @MockBean
    private JwtUtil jwtUtil;
    
    private Teacher mockTeacher;
    private Class mockClass;
    private Student mockStudent;
    
    @BeforeEach
    void setUp() {
        mockTeacher = new Teacher();
        mockTeacher.setId(1L);
        mockTeacher.setUsername("teacher1");
        mockTeacher.setFullName("Test Teacher");
        
        mockClass = new Class();
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
    }
    
    @Test
    @WithMockUser(username = "teacher1")
    void testGetStudentsByGradeAndClass_Authenticated_ReturnsOk() throws Exception {
        // Arrange
        Map<String, Map<String, List<Student>>> groupedStudents = new HashMap<>();
        Map<String, List<Student>> classMap = new HashMap<>();
        classMap.put("10A", Arrays.asList(mockStudent));
        groupedStudents.put("י", classMap);
        
        when(studentService.getStudentsByGradeAndClass()).thenReturn(groupedStudents);
        
        // Act & Assert
        mockMvc.perform(get("/api/students/by-grade-and-class")
                .with(csrf()))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "teacher1")
    void testGetStudentsByClass_ValidClassId_ReturnsStudents() throws Exception {
        // Arrange
        List<Student> students = Arrays.asList(mockStudent);
        when(studentService.getStudentsByClass(1L)).thenReturn(students);
        
        // Act & Assert
        mockMvc.perform(get("/api/students/class/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "teacher1")
    void testGetStudentsByClass_EmptyClass_ReturnsEmptyList() throws Exception {
        // Arrange
        when(studentService.getStudentsByClass(1L)).thenReturn(Collections.emptyList());
        
        // Act & Assert
        mockMvc.perform(get("/api/students/class/1")
                .with(csrf()))
                .andExpect(status().isOk());
    }
    
    @Test
    void testGetStudentsByGradeAndClass_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/students/by-grade-and-class")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void testGetStudentsByClass_Unauthenticated_ReturnsUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/students/class/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
