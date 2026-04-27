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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.Timestamp;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StudentService.
 * Tests CRUD operations, duplicate detection, and grouping functionality.
 */
@ExtendWith(MockitoExtension.class)
class StudentServiceTest {
    
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
    
    private Teacher teacher;
    private Class class1;
    private Class class2;
    private Student student1;
    private Student student2;
    
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
        
        // Create test classes
        class1 = new Class();
        class1.setId(1L);
        class1.setName("א1");
        class1.setGradeLevel("י");
        class1.setTeacher(teacher);
        
        class2 = new Class();
        class2.setId(2L);
        class2.setName("א2");
        class2.setGradeLevel("י");
        class2.setTeacher(teacher);
        
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
        student2.setClassEntity(class1);
    }
    
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
    
    @Test
    void testSaveStudent_NewStudent_CreatesStudent() {
        // Arrange
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
        assertThat(result.getName()).isEqualTo("יוסף אברהם");
        verify(studentRepository).save(newStudent);
    }
    
    @Test
    void testSaveStudent_ExistingStudentById_UpdatesStudent() {
        // Arrange
        Student existingStudent = new Student();
        existingStudent.setId(1L);
        existingStudent.setName("דוד כהן");
        existingStudent.setStudentId("123456789");
        existingStudent.setGradeLevel("י");
        existingStudent.setClassEntity(class1);
        
        Student updatedStudent = new Student();
        updatedStudent.setName("דוד כהן - מעודכן");
        updatedStudent.setStudentId("123456789");
        updatedStudent.setGradeLevel("יא");
        updatedStudent.setClassEntity(class2);
        
        when(classRepository.findById(2L)).thenReturn(Optional.of(class2));
        when(studentRepository.findByStudentId("123456789")).thenReturn(Optional.of(existingStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(existingStudent);
        
        // Act
        Student result = studentService.saveStudent(updatedStudent);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("דוד כהן - מעודכן");
        assertThat(result.getGradeLevel()).isEqualTo("יא");
        verify(studentRepository).save(existingStudent);
    }
    
    @Test
    void testSaveStudent_ExistingStudentByNameAndClass_UpdatesStudent() {
        // Arrange
        Student existingStudent = new Student();
        existingStudent.setId(1L);
        existingStudent.setName("שרה לevi");
        existingStudent.setStudentId(null);
        existingStudent.setGradeLevel("י");
        existingStudent.setClassEntity(class1);
        
        Student updatedStudent = new Student();
        updatedStudent.setName("שרה לevi");
        updatedStudent.setStudentId(null);
        updatedStudent.setGradeLevel("י");
        updatedStudent.setClassEntity(class1);
        
        when(classRepository.findById(1L)).thenReturn(Optional.of(class1));
        when(studentRepository.findByNameAndClassEntityId("שרה לevi", 1L))
            .thenReturn(Optional.of(existingStudent));
        when(studentRepository.save(any(Student.class))).thenReturn(existingStudent);
        
        // Act
        Student result = studentService.saveStudent(updatedStudent);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(studentRepository).save(existingStudent);
    }
    
    @Test
    void testSaveStudent_NullStudent_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> studentService.saveStudent(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Student cannot be null");
    }
    
    @Test
    void testSaveStudent_NullName_ThrowsException() {
        // Arrange
        Student student = new Student();
        student.setStudentId("123456789");
        student.setGradeLevel("י");
        student.setClassEntity(class1);
        
        // Act & Assert
        assertThatThrownBy(() -> studentService.saveStudent(student))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Student name is required");
    }
    
    @Test
    void testSaveStudent_NullClass_ThrowsException() {
        // Arrange
        Student student = new Student();
        student.setName("דוד כהן");
        student.setStudentId("123456789");
        student.setGradeLevel("י");
        
        // Act & Assert
        assertThatThrownBy(() -> studentService.saveStudent(student))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Student must be assigned to a class");
    }
    
    @Test
    void testFindExistingStudent_ByStudentId_ReturnsStudent() {
        // Arrange
        when(studentRepository.findByStudentId("123456789")).thenReturn(Optional.of(student1));
        
        // Act
        Optional<Student> result = studentService.findExistingStudent("123456789", "דוד כהן", 1L);
        
        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getStudentId()).isEqualTo("123456789");
        verify(studentRepository).findByStudentId("123456789");
        verify(studentRepository, never()).findByNameAndClassEntityId(anyString(), anyLong());
    }
    
    @Test
    void testFindExistingStudent_ByNameAndClass_ReturnsStudent() {
        // Arrange
        when(studentRepository.findByNameAndClassEntityId("שרה לevi", 1L))
            .thenReturn(Optional.of(student2));
        
        // Act
        Optional<Student> result = studentService.findExistingStudent(null, "שרה לevi", 1L);
        
        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("שרה לevi");
        verify(studentRepository, never()).findByStudentId(anyString());
        verify(studentRepository).findByNameAndClassEntityId("שרה לevi", 1L);
    }
    
    @Test
    void testFindExistingStudent_NotFound_ReturnsEmpty() {
        // Arrange
        when(studentRepository.findByStudentId("999999999")).thenReturn(Optional.empty());
        when(studentRepository.findByNameAndClassEntityId("לא קיים", 1L))
            .thenReturn(Optional.empty());
        
        // Act
        Optional<Student> result = studentService.findExistingStudent("999999999", "לא קיים", 1L);
        
        // Assert
        assertThat(result).isEmpty();
    }
    
    @Test
    void testGetStudentsByGradeAndClass_GroupsCorrectly() {
        // Arrange
        Student student3 = new Student();
        student3.setId(3L);
        student3.setName("מיכל דוד");
        student3.setGradeLevel("יא");
        student3.setClassEntity(class2);
        
        List<Student> students = Arrays.asList(student1, student2, student3);
        when(studentRepository.findByTeacherIdOrderByGradeLevelAndClassAndName(1L))
            .thenReturn(students);
        
        // Act
        Map<String, Map<String, List<Student>>> result = studentService.getStudentsByGradeAndClass(1L);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2); // Two grade levels: י and יא
        
        // Check grade level י
        assertThat(result).containsKey("י");
        Map<String, List<Student>> gradeYod = result.get("י");
        assertThat(gradeYod).containsKey("א1");
        assertThat(gradeYod.get("א1")).hasSize(2);
        
        // Check grade level יא
        assertThat(result).containsKey("יא");
        Map<String, List<Student>> gradeYodAlef = result.get("יא");
        assertThat(gradeYodAlef).containsKey("א2");
        assertThat(gradeYodAlef.get("א2")).hasSize(1);
    }
    
    @Test
    void testGetStudentsByGradeAndClass_NullTeacherId_ThrowsException() {
        // Act & Assert
        assertThatThrownBy(() -> studentService.getStudentsByGradeAndClass(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Teacher ID cannot be null");
    }
    
    @Test
    void testGetStudentsByClass_ReturnsStudents() {
        // Arrange
        List<Student> students = Arrays.asList(student1, student2);
        when(classRepository.findById(1L)).thenReturn(Optional.of(class1));
        when(studentRepository.findByClassEntityIdOrderByName(1L)).thenReturn(students);
        
        // Act
        List<Student> result = studentService.getStudentsByClass(1L);
        
        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(student1, student2);
    }
    
    @Test
    void testGetStudentsByTeacherAndGradeLevel_ReturnsFilteredStudents() {
        // Arrange
        List<Student> students = Arrays.asList(student1, student2);
        when(studentRepository.findByTeacherIdAndGradeLevel(1L, "י")).thenReturn(students);
        
        // Act
        List<Student> result = studentService.getStudentsByTeacherAndGradeLevel(1L, "י");
        
        // Assert
        assertThat(result).hasSize(2);
        verify(studentRepository).findByTeacherIdAndGradeLevel(1L, "י");
    }
    
    @Test
    void testGetClassesByTeacherAndGradeLevel_ReturnsClasses() {
        // Arrange
        List<Class> classes = Arrays.asList(class1, class2);
        when(classRepository.findByTeacherIdAndGradeLevel(1L, "י")).thenReturn(classes);
        
        // Act
        List<Class> result = studentService.getClassesByTeacherAndGradeLevel(1L, "י");
        
        // Assert
        assertThat(result).hasSize(2);
        verify(classRepository).findByTeacherIdAndGradeLevel(1L, "י");
    }
    
    @Test
    void testGetStudentById_ReturnsStudent() {
        // Arrange
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        
        // Act
        Optional<Student> result = studentService.getStudentById(1L);
        
        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }
    
    @Test
    void testDeleteStudent_ExistingStudent_DeletesSuccessfully() {
        // Arrange
        when(studentRepository.existsById(1L)).thenReturn(true);
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student1));
        
        // Act
        studentService.deleteStudent(1L);
        
        // Assert
        verify(studentRepository).deleteById(1L);
    }
    
    @Test
    void testDeleteStudent_NonExistingStudent_ThrowsException() {
        // Arrange
        when(studentRepository.existsById(999L)).thenReturn(false);
        
        // Act & Assert
        assertThatThrownBy(() -> studentService.deleteStudent(999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Student with ID 999 not found");
    }
    
    @Test
    void testCountStudentsByClass_ReturnsCount() {
        // Arrange
        when(classRepository.findById(1L)).thenReturn(Optional.of(class1));
        when(studentRepository.countByClassEntityId(1L)).thenReturn(2L);
        
        // Act
        long count = studentService.countStudentsByClass(1L);
        
        // Assert
        assertThat(count).isEqualTo(2L);
    }
    
    @Test
    void testExistsByStudentId_ExistingId_ReturnsTrue() {
        // Arrange
        when(studentRepository.existsByStudentId("123456789")).thenReturn(true);
        
        // Act
        boolean exists = studentService.existsByStudentId("123456789");
        
        // Assert
        assertThat(exists).isTrue();
    }
    
    @Test
    void testExistsByStudentId_NonExistingId_ReturnsFalse() {
        // Arrange
        when(studentRepository.existsByStudentId("999999999")).thenReturn(false);
        
        // Act
        boolean exists = studentService.existsByStudentId("999999999");
        
        // Assert
        assertThat(exists).isFalse();
    }
    
    @Test
    void testExistsByStudentId_NullId_ReturnsFalse() {
        // Act
        boolean exists = studentService.existsByStudentId(null);
        
        // Assert
        assertThat(exists).isFalse();
        verify(studentRepository, never()).existsByStudentId(anyString());
    }
}
