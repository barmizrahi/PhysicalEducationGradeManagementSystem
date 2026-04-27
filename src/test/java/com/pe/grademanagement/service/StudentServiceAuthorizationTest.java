package com.pe.grademanagement.service;

import com.pe.grademanagement.entity.Class;
import com.pe.grademanagement.entity.Student;
import com.pe.grademanagement.entity.Teacher;
import com.pe.grademanagement.repository.ClassRepository;
import com.pe.grademanagement.repository.StudentRepository;
import com.pe.grademanagement.repository.TeacherRepository;
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

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StudentService authorization checks.
 * Tests that teachers can only access their own students and classes.
 * 
 * Requirements:
 * - 13.2: Associate each teacher with their assigned classes
 * - 13.3: Display only classes assigned to authenticated teacher
 * - 13.4: Prevent teachers from accessing or modifying data for classes not assigned to them
 * - 11.2: Isolate data when multiple teachers access different classes
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceAuthorizationTest {
    
    @Mock
    private StudentRepository studentRepository;
    
    @Mock
    private ClassRepository classRepository;
    
    @Mock
    private TeacherRepository teacherRepository;
    
    @Mock
    private SecurityContext securityContext;
    
    @InjectMocks
    private StudentService studentService;
    
    private Teacher teacher1;
    private Teacher teacher2;
    private Class class1;
    private Class class2;
    private Student student1;
    private Student student2;
    
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
        class1 = new Class();
        class1.setId(1L);
        class1.setName("א1");
        class1.setGradeLevel("י");
        class1.setTeacher(teacher1);
        
        class2 = new Class();
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
    void testSaveStudent_OwnClass_Success() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        Student newStudent = new Student();
        newStudent.setName("יוסף אברהם");
        newStudent.setStudentId("111222333");
        newStudent.setGradeLevel("י");
        newStudent.setClassEntity(class1);
        
        when(classRepository.findById(1L)).thenReturn(Optional.of(class1));
        when(studentRepository.findByStudentId("111222333")).thenReturn(Optional.empty());
        when(studentRepository.save(any(Student.class))).thenReturn(newStudent);
        
        // Act
        Student result = studentService.saveStudent(newStudent);
        
        // Assert
        assertThat(result).isNotNull();
        verify(studentRepository).save(newStudent);
    }
    
    @Test
    void testSaveStudent_OtherTeachersClass_ThrowsAccessDeniedException() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        Student newStudent = new Student();
        newStudent.setName("יוסף אברהם");
        newStudent.setStudentId("111222333");
        newStudent.setGradeLevel("י");
        newStudent.setClassEntity(class2); // class2 belongs to teacher2
        
        when(classRepository.findById(2L)).thenReturn(Optional.of(class2));
        
        // Act & Assert
        assertThatThrownBy(() -> studentService.saveStudent(newStudent))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Teacher does not have access to class with ID: 2");
        
        verify(studentRepository, never()).save(any(Student.class));
    }
    
    @Test
    void testGetStudentsByClass_OwnClass_Success() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(classRepository.findById(1L)).thenReturn(Optional.of(class1));
        when(studentRepository.findByClassEntityIdOrderByName(1L))
            .thenReturn(Collections.singletonList(student1));
        
        // Act
        var result = studentService.getStudentsByClass(1L);
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }
    
    @Test
    void testGetStudentsByClass_OtherTeachersClass_ThrowsAccessDeniedException() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(classRepository.findById(2L)).thenReturn(Optional.of(class2));
        
        // Act & Assert
        assertThatThrownBy(() -> studentService.getStudentsByClass(2L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Teacher does not have access to class with ID: 2");
    }
    
    @Test
    void testGetStudentById_OwnStudent_Success() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        
        // Act
        Optional<Student> result = studentService.getStudentById(1L);
        
        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }
    
    @Test
    void testGetStudentById_OtherTeachersStudent_ThrowsAccessDeniedException() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student2));
        
        // Act & Assert
        assertThatThrownBy(() -> studentService.getStudentById(2L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Teacher does not have access to student with ID: 2");
    }
    
    @Test
    void testDeleteStudent_OwnStudent_Success() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        
        // Act
        studentService.deleteStudent(1L);
        
        // Assert
        verify(studentRepository).deleteById(1L);
    }
    
    @Test
    void testDeleteStudent_OtherTeachersStudent_ThrowsAccessDeniedException() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(studentRepository.existsById(2L)).thenReturn(true);
        when(studentRepository.findById(2L)).thenReturn(Optional.of(student2));
        
        // Act & Assert
        assertThatThrownBy(() -> studentService.deleteStudent(2L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Teacher does not have access to student with ID: 2");
        
        verify(studentRepository, never()).deleteById(2L);
    }
    
    @Test
    void testCountStudentsByClass_OwnClass_Success() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(classRepository.findById(1L)).thenReturn(Optional.of(class1));
        when(studentRepository.countByClassEntityId(1L)).thenReturn(5L);
        
        // Act
        long count = studentService.countStudentsByClass(1L);
        
        // Assert
        assertThat(count).isEqualTo(5L);
    }
    
    @Test
    void testCountStudentsByClass_OtherTeachersClass_ThrowsAccessDeniedException() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(classRepository.findById(2L)).thenReturn(Optional.of(class2));
        
        // Act & Assert
        assertThatThrownBy(() -> studentService.countStudentsByClass(2L))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Teacher does not have access to class with ID: 2");
    }
    
    @Test
    void testGetStudentsByGradeAndClass_UsesAuthenticatedTeacher() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(studentRepository.findByTeacherIdOrderByGradeLevelAndClassAndName(1L))
            .thenReturn(Collections.singletonList(student1));
        
        // Act
        var result = studentService.getStudentsByGradeAndClass();
        
        // Assert
        assertThat(result).isNotNull();
        verify(studentRepository).findByTeacherIdOrderByGradeLevelAndClassAndName(1L);
    }
    
    @Test
    void testGetStudentsByTeacherAndGradeLevel_UsesAuthenticatedTeacher() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(studentRepository.findByTeacherIdAndGradeLevel(1L, "י"))
            .thenReturn(Collections.singletonList(student1));
        
        // Act
        var result = studentService.getStudentsByTeacherAndGradeLevel("י");
        
        // Assert
        assertThat(result).hasSize(1);
        verify(studentRepository).findByTeacherIdAndGradeLevel(1L, "י");
    }
    
    @Test
    void testGetClassesByTeacherAndGradeLevel_UsesAuthenticatedTeacher() {
        // Arrange
        authenticateAsTeacher(teacher1);
        
        when(classRepository.findByTeacherIdAndGradeLevel(1L, "י"))
            .thenReturn(Collections.singletonList(class1));
        
        // Act
        var result = studentService.getClassesByTeacherAndGradeLevel("י");
        
        // Assert
        assertThat(result).hasSize(1);
        verify(classRepository).findByTeacherIdAndGradeLevel(1L, "י");
    }
    
    @Test
    void testNoAuthentication_ThrowsAccessDeniedException() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);
        
        // Act & Assert
        assertThatThrownBy(() -> studentService.getStudentsByGradeAndClass())
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("No authenticated user found");
    }
    
    @Test
    void testTeacherNotFound_ThrowsAccessDeniedException() {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            "unknown_teacher",
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_TEACHER"))
        );
        
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(teacherRepository.findByUsername("unknown_teacher")).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThatThrownBy(() -> studentService.getStudentsByGradeAndClass())
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Teacher not found for username: unknown_teacher");
    }
}
