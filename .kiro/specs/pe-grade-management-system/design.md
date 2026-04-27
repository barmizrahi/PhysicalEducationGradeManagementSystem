# Design Document: Physical Education Grade Management System

## Overview

The Physical Education Grade Management System is a web application designed to streamline grade management for high school PE teachers. The system eliminates double data entry by providing a digital workflow from student import through grade calculation to Ministry of Education-compatible export.

### Core Objectives

1. **Eliminate Double Entry**: Replace notebook-then-system workflow with direct digital entry
2. **Mobile-First Data Entry**: Enable fast, touch-optimized grade entry during class on mobile devices
3. **Automated Calculation**: Apply configurable grading formulas (ratio and penalty methods) automatically
4. **Ministry Integration**: Export grades in Ministry of Education-compatible Excel format

### Key Features

- Excel-based student roster import with duplicate detection
- Configurable test creation with two calculation methods (RATIO and PENALTY)
- Mobile-optimized bulk grade entry interface with auto-save
- Automatic grade calculation from raw test results
- Ministry-compatible Excel export with selective test/class inclusion
- Multi-teacher support with data isolation

### Technology Stack

- **Backend**: Java 23 with Spring Boot framework
- **Frontend**: React with responsive/mobile-first design
- **Database**: PostgreSQL for relational data storage
- **Excel Processing**: Apache POI for import/export
- **Authentication**: Spring Security with JWT tokens

## Architecture

### System Architecture

The system follows a three-tier architecture:

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Student Mgmt │  │ Grade Entry  │  │ Export UI    │      │
│  │   UI (React) │  │  UI (React)  │  │   (React)    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
                    REST API (JSON)
                            │
┌─────────────────────────────────────────────────────────────┐
│                      Business Logic Layer                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Student    │  │     Test     │  │    Grade     │      │
│  │   Service    │  │   Service    │  │   Service    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │    Excel     │  │     Grade    │  │     Auth     │      │
│  │   Importer   │  │  Calculator  │  │   Service    │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                       Data Access Layer                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Student    │  │     Test     │  │  TestResult  │      │
│  │  Repository  │  │  Repository  │  │  Repository  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
                            │
                    ┌───────────────┐
                    │  PostgreSQL   │
                    │   Database    │
                    └───────────────┘
```

### Component Responsibilities

**Presentation Layer (React)**
- Responsive UI components optimized for mobile and desktop
- Form validation and user feedback
- Real-time grade calculation display
- Auto-save functionality for grade entry

**Business Logic Layer (Spring Boot)**
- `StudentService`: Student CRUD operations, duplicate detection
- `TestService`: Test configuration management, test-class assignment
- `GradeService`: Test result storage and retrieval
- `GradeCalculator`: Grade calculation algorithms (RATIO and PENALTY methods)
- `ExcelImporter`: Parse Excel files and create student records
- `ExcelExporter`: Generate Ministry-compatible Excel exports
- `AuthService`: User authentication and authorization

**Data Access Layer (Spring Data JPA)**
- Repository interfaces for database operations
- Entity mapping and relationship management
- Query optimization for bulk operations

### Data Flow

**Student Import Flow**:
```
Excel File → ExcelImporter → StudentService → Student Repository → Database
```

**Grade Entry Flow**:
```
UI Input → GradeService → GradeCalculator → TestResult Repository → Database
                                    ↓
                            Calculated Grade → UI Display
```

**Export Flow**:
```
Database → GradeService → ExcelExporter → Ministry-Format Excel File → Download
```

## Components and Interfaces

### Core Components

#### 1. ExcelImporter

**Purpose**: Parse Excel files and extract student data

**Public Interface**:
```java
public class ExcelImporter {
    /**
     * Import students from Excel file
     * @param file Excel file containing student data
     * @param columnMapping Map of Excel columns to Student fields
     * @return ImportResult containing created/updated students and errors
     * @throws InvalidExcelFormatException if file format is invalid
     */
    public ImportResult importStudents(MultipartFile file, ColumnMapping columnMapping);
    
    /**
     * Validate Excel file format
     * @param file Excel file to validate
     * @return ValidationResult with errors if any
     */
    public ValidationResult validateExcelFormat(MultipartFile file);
}
```

**Key Behaviors**:
- Parse Excel files using Apache POI
- Support flexible column mapping (name, studentId, gradeLevel, className)
- Handle Hebrew grade levels (י, יא, יב)
- Return descriptive errors for invalid formats
- Support optional student ID field

#### 2. StudentService

**Purpose**: Manage student data and duplicate detection

**Public Interface**:
```java
public class StudentService {
    /**
     * Create or update student record
     * @param student Student data
     * @return Created or updated Student entity
     */
    public Student saveStudent(Student student);
    
    /**
     * Find existing student by ID or name+class
     * @param studentId Optional student ID
     * @param name Student name
     * @param className Class name
     * @return Optional existing student
     */
    public Optional<Student> findExistingStudent(String studentId, String name, String className);
    
    /**
     * Get students grouped by grade level and class
     * @param teacherId Teacher ID for authorization
     * @return Map of grade level → class → students
     */
    public Map<String, Map<String, List<Student>>> getStudentsByGradeAndClass(Long teacherId);
}
```

**Key Behaviors**:
- Detect duplicates using student ID (if available) or name+class combination
- Update existing records instead of creating duplicates
- Filter students by teacher authorization
- Group students by grade level and class

#### 3. TestService

**Purpose**: Manage test configurations and assignments

**Public Interface**:
```java
public class TestService {
    /**
     * Create new test configuration
     * @param test Test configuration
     * @return Created Test entity
     * @throws ValidationException if configuration is invalid
     */
    public Test createTest(Test test);
    
    /**
     * Update existing test configuration
     * @param testId Test ID
     * @param test Updated configuration
     * @return Updated Test entity
     */
    public Test updateTest(Long testId, Test test);
    
    /**
     * Assign test to classes
     * @param testId Test ID
     * @param classIds List of class IDs
     */
    public void assignTestToClasses(Long testId, List<Long> classIds);
    
    /**
     * Get tests for a specific class
     * @param classId Class ID
     * @return List of assigned tests
     */
    public List<Test> getTestsForClass(Long classId);
}
```

**Key Behaviors**:
- Validate test configuration based on calculation type
- Require maxValue for RATIO tests
- Require targetValue and penaltyPerUnit for PENALTY tests
- Support test-to-class assignment at class or grade level

#### 4. GradeCalculator

**Purpose**: Calculate grades from raw test results

**Public Interface**:
```java
public class GradeCalculator {
    /**
     * Calculate grade based on test configuration
     * @param rawResult Raw test result (decimal)
     * @param test Test configuration
     * @return Calculated grade (0-100, rounded to 2 decimal places)
     */
    public BigDecimal calculateGrade(BigDecimal rawResult, Test test);
    
    /**
     * Calculate grade using RATIO method
     * @param rawResult Raw result
     * @param maxValue Maximum value for 100%
     * @return Calculated grade
     */
    public BigDecimal calculateRatioGrade(BigDecimal rawResult, BigDecimal maxValue);
    
    /**
     * Calculate grade using PENALTY method
     * @param rawResult Raw result
     * @param targetValue Target value for 100%
     * @param penaltyPerUnit Penalty per unit deviation
     * @return Calculated grade
     */
    public BigDecimal calculatePenaltyGrade(BigDecimal rawResult, BigDecimal targetValue, BigDecimal penaltyPerUnit);
}
```

**Key Behaviors**:
- Support non-integer raw results (e.g., 15.5 repetitions, 10.5 minutes)
- RATIO: grade = (rawResult / maxValue) * 100, capped at 100
- PENALTY: grade = 100 - ((rawResult - targetValue) * penaltyPerUnit), capped at 0-100
- Round all grades to 2 decimal places
- Return 0 for zero raw results

#### 5. GradeService

**Purpose**: Manage test results and grade storage

**Public Interface**:
```java
public class GradeService {
    /**
     * Save or update test result
     * @param result Test result with raw value and optional notes
     * @return Saved TestResult with calculated grade
     */
    public TestResult saveTestResult(TestResult result);
    
    /**
     * Get test results for a class and test
     * @param classId Class ID
     * @param testId Test ID
     * @return List of test results for all students
     */
    public List<TestResult> getTestResultsForClass(Long classId, Long testId);
    
    /**
     * Bulk save test results
     * @param results List of test results
     * @return List of saved results with calculated grades
     */
    public List<TestResult> bulkSaveTestResults(List<TestResult> results);
}
```

**Key Behaviors**:
- Automatically calculate grade when saving result
- Recalculate grade when updating result
- Store timestamps for creation and modification
- Support null rawResult with calculatedGrade = 0
- Enable bulk operations for class-wide entry

#### 6. ExcelExporter

**Purpose**: Generate Ministry-compatible Excel exports

**Public Interface**:
```java
public class ExcelExporter {
    /**
     * Export grades to Excel file
     * @param exportConfig Configuration specifying classes, tests, and options
     * @return Excel file as byte array
     */
    public byte[] exportGrades(ExportConfig exportConfig);
    
    /**
     * Generate Ministry-format Excel with specified data
     * @param students List of students
     * @param testResults Map of student → test → result
     * @param includeNotes Whether to include notes column
     * @return Excel file in Ministry format
     */
    public byte[] generateMinistryFormatExcel(List<Student> students, 
                                               Map<Student, Map<Test, TestResult>> testResults,
                                               boolean includeNotes);
}
```

**Key Behaviors**:
- Generate Excel files using Apache POI
- Include columns: name, student ID, grade level, class name, test grades
- Optionally include notes column
- Allow selective inclusion of tests and classes
- Format compatible with Ministry of Education system
- Include students without test results (grade = 0)

#### 7. TimeConverter

**Purpose**: Convert time input formats

**Public Interface**:
```java
public class TimeConverter {
    /**
     * Convert mm:ss format to decimal minutes
     * @param timeString Time in mm:ss format (e.g., "10:30")
     * @return Decimal minutes (e.g., 10.5)
     * @throws InvalidTimeFormatException if format is invalid
     */
    public BigDecimal convertToDecimalMinutes(String timeString);
    
    /**
     * Convert decimal minutes to mm:ss format
     * @param decimalMinutes Decimal minutes (e.g., 10.5)
     * @return Time in mm:ss format (e.g., "10:30")
     */
    public String convertToTimeFormat(BigDecimal decimalMinutes);
}
```

**Key Behaviors**:
- Parse mm:ss format (e.g., "10:30" → 10.5)
- Validate time format
- Store all TIME values as decimal numbers internally
- Support conversion for display purposes

### REST API Endpoints

**Student Management**:
- `POST /api/students/import` - Import students from Excel
- `GET /api/students/by-grade-and-class` - Get students grouped by grade/class
- `GET /api/students/class/{classId}` - Get students in a class

**Test Management**:
- `POST /api/tests` - Create test configuration
- `PUT /api/tests/{id}` - Update test configuration
- `POST /api/tests/{id}/assign` - Assign test to classes
- `GET /api/tests/class/{classId}` - Get tests for a class

**Grade Entry**:
- `GET /api/grades/class/{classId}/test/{testId}` - Get results for class/test
- `POST /api/grades` - Save single test result
- `POST /api/grades/bulk` - Bulk save test results

**Export**:
- `POST /api/export/excel` - Export grades to Excel

**Authentication**:
- `POST /api/auth/login` - Teacher login
- `POST /api/auth/logout` - Teacher logout

## Data Models

### Entity Relationship Diagram

```mermaid
erDiagram
    Teacher ||--o{ Class : teaches
    Class ||--o{ Student : contains
    Class ||--o{ TestAssignment : "assigned to"
    Test ||--o{ TestAssignment : "assigned to"
    Student ||--o{ TestResult : "has results"
    Test ||--o{ TestResult : "measures"
    
    Teacher {
        Long id PK
        String username
        String passwordHash
        String fullName
        Timestamp createdAt
    }
    
    Class {
        Long id PK
        String name
        String gradeLevel
        Long teacherId FK
        Timestamp createdAt
    }
    
    Student {
        Long id PK
        String name
        String studentId "nullable"
        String gradeLevel
        Long classId FK
        Timestamp createdAt
        Timestamp updatedAt
    }
    
    Test {
        Long id PK
        String name
        String calculationType "RATIO or PENALTY"
        String unitType "TIME or COUNT"
        BigDecimal maxValue "nullable, for RATIO"
        BigDecimal targetValue "nullable, for PENALTY"
        BigDecimal penaltyPerUnit "nullable, for PENALTY"
        Long createdBy FK
        Timestamp createdAt
        Timestamp updatedAt
    }
    
    TestAssignment {
        Long id PK
        Long testId FK
        Long classId FK
        Timestamp assignedAt
    }
    
    TestResult {
        Long id PK
        Long studentId FK
        Long testId FK
        BigDecimal rawResult "nullable"
        BigDecimal calculatedGrade
        String notes "nullable"
        Timestamp createdAt
        Timestamp updatedAt
    }
```

### Entity Definitions

#### Teacher
```java
@Entity
public class Teacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @Column(nullable = false)
    private String fullName;
    
    @Column(nullable = false)
    private Timestamp createdAt;
    
    @OneToMany(mappedBy = "teacher")
    private List<Class> classes;
}
```

#### Class
```java
@Entity
public class Class {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String gradeLevel; // י, יא, יב
    
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;
    
    @Column(nullable = false)
    private Timestamp createdAt;
    
    @OneToMany(mappedBy = "classEntity")
    private List<Student> students;
}
```

#### Student
```java
@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = true)
    private String studentId; // Optional, for duplicate detection
    
    @Column(nullable = false)
    private String gradeLevel; // י, יא, יב
    
    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private Class classEntity;
    
    @Column(nullable = false)
    private Timestamp createdAt;
    
    @Column(nullable = false)
    private Timestamp updatedAt;
    
    @OneToMany(mappedBy = "student")
    private List<TestResult> testResults;
}
```

#### Test
```java
@Entity
public class Test {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CalculationType calculationType; // RATIO, PENALTY
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnitType unitType; // TIME, COUNT
    
    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal maxValue; // For RATIO calculation
    
    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal targetValue; // For PENALTY calculation
    
    @Column(nullable = true, precision = 10, scale = 4)
    private BigDecimal penaltyPerUnit; // For PENALTY calculation
    
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private Teacher createdBy;
    
    @Column(nullable = false)
    private Timestamp createdAt;
    
    @Column(nullable = false)
    private Timestamp updatedAt;
    
    @OneToMany(mappedBy = "test")
    private List<TestAssignment> assignments;
    
    @OneToMany(mappedBy = "test")
    private List<TestResult> results;
}
```

#### TestAssignment
```java
@Entity
public class TestAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;
    
    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private Class classEntity;
    
    @Column(nullable = false)
    private Timestamp assignedAt;
}
```

#### TestResult
```java
@Entity
public class TestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;
    
    @Column(nullable = true, precision = 10, scale = 2)
    private BigDecimal rawResult; // Null if student didn't take test
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal calculatedGrade; // 0-100, 0 if rawResult is null
    
    @Column(nullable = true, length = 500)
    private String notes;
    
    @Column(nullable = false)
    private Timestamp createdAt;
    
    @Column(nullable = false)
    private Timestamp updatedAt;
}
```

### Enumerations

```java
public enum CalculationType {
    RATIO,    // grade = (rawResult / maxValue) * 100
    PENALTY   // grade = 100 - ((rawResult - targetValue) * penaltyPerUnit)
}

public enum UnitType {
    TIME,     // Decimal minutes (e.g., 10.5)
    COUNT     // Repetitions or other counts
}
```

### Data Constraints

- Student names and IDs support Hebrew characters
- Grade levels limited to: י, יא, יב
- All decimal values use appropriate precision (BigDecimal)
- Calculated grades always in range [0, 100]
- Timestamps automatically managed by JPA
- Unique constraint on (studentId) when not null
- Unique constraint on (testId, studentId) for TestResult
- Unique constraint on (testId, classId) for TestAssignment


## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property Reflection

After analyzing all acceptance criteria, I identified the following areas with potential redundancy:

1. **Excel Import Properties (1.1, 1.2, 10.1)**: Properties 1.1 and 10.1 are identical - both test parsing valid Excel files. These should be combined.

2. **Error Handling (1.4, 10.2)**: Properties 1.4 and 10.2 are identical - both test error messages for invalid Excel files. These should be combined.

3. **Rounding Properties (4.5, 5.6)**: Both test that grades are rounded to 2 decimal places. Since both RATIO and PENALTY calculations use the same rounding logic, these can be combined into one comprehensive property.

4. **Duplicate Detection (1.7, 1.8, 1.9)**: These three properties test different aspects of duplicate detection but can be combined into a single comprehensive property that covers all duplicate detection scenarios.

5. **Grouping Properties (2.1, 2.2)**: These test nested grouping and can be combined into one property that verifies the complete grouping structure.

6. **Grade Calculation with Decimals (4.2, 5.2, 15.4)**: These are edge cases that should be covered by generators rather than separate properties.

7. **Test Result Storage (7.2, 7.3)**: Update and recalculation can be combined into one property that verifies updates trigger recalculation.

8. **Missing Result Handling (8.1, 8.4)**: These both test the same behavior - null rawResult results in grade 0. Can be combined.

After reflection, the following properties provide unique validation value:

### Property 1: Excel Import Parsing

*For any* valid Excel file containing student data with valid column mappings, the Student_Importer SHALL successfully parse the file and extract all student records with correctly mapped fields.

**Validates: Requirements 1.1, 1.2, 10.1**

### Property 2: Excel Import Error Handling

*For any* invalid Excel file format, the Student_Importer SHALL return a descriptive error message and not create any student records.

**Validates: Requirements 1.4, 10.2**

### Property 3: Optional Student ID Handling

*For any* Excel file where student ID column is absent or empty, the Student_Importer SHALL create student records with null student ID values.

**Validates: Requirements 1.6**

### Property 4: Duplicate Detection and Update

*For any* student import operation, if a student already exists (detected by student ID when available, or by name+class combination when student ID is absent), the Student_Importer SHALL update the existing record instead of creating a duplicate.

**Validates: Requirements 1.7, 1.8, 1.9**

### Property 5: Student Grouping by Grade and Class

*For any* list of students, the grouping function SHALL return students organized first by grade level, then by class name within each grade level, with all students appearing in exactly one group.

**Validates: Requirements 2.1, 2.2**

### Property 6: Class Filtering by Grade Level

*For any* grade level selection, the filtering function SHALL return all and only the classes that belong to that grade level.

**Validates: Requirements 2.3**

### Property 7: Student Filtering by Class

*For any* class selection, the filtering function SHALL return all and only the students that belong to that class.

**Validates: Requirements 2.4**

### Property 8: Test Configuration Validation for RATIO

*For any* test with RATIO calculation type, the Test_Manager SHALL require a non-null maxValue parameter and reject tests without it.

**Validates: Requirements 3.4**

### Property 9: Test Configuration Validation for PENALTY

*For any* test with PENALTY calculation type, the Test_Manager SHALL require non-null targetValue and penaltyPerUnit parameters and reject tests missing either.

**Validates: Requirements 3.5**

### Property 10: Test Configuration Updates

*For any* existing test configuration and any valid update, the Test_Manager SHALL persist the changes and return the updated configuration.

**Validates: Requirements 3.7**

### Property 11: RATIO Grade Calculation Formula

*For any* non-negative rawResult and positive maxValue, the Grade_Calculator SHALL compute the grade as (rawResult / maxValue) * 100, rounded to 2 decimal places.

**Validates: Requirements 4.1, 4.5, 5.6**

### Property 12: RATIO Grade Upper Bound

*For any* rawResult greater than or equal to maxValue, the Grade_Calculator SHALL return a grade of exactly 100.

**Validates: Requirements 4.3**

### Property 13: PENALTY Grade Calculation Formula

*For any* rawResult, targetValue, and penaltyPerUnit, the Grade_Calculator SHALL compute the grade as 100 - ((rawResult - targetValue) * penaltyPerUnit), rounded to 2 decimal places.

**Validates: Requirements 5.1, 5.6**

### Property 14: PENALTY Grade Upper Bound

*For any* rawResult better than (less than for TIME, greater than for COUNT) targetValue, the Grade_Calculator SHALL return a grade of exactly 100 with no bonus above 100.

**Validates: Requirements 5.3**

### Property 15: PENALTY Grade Lower Bound

*For any* calculation where the penalty formula would produce a negative grade, the Grade_Calculator SHALL return a grade of exactly 0.

**Validates: Requirements 5.4**

### Property 16: PENALTY Calculation Linearity

*For any* two raw results with the same targetValue and penaltyPerUnit, if the deviation from target doubles, the penalty SHALL double (maintaining linear relationship).

**Validates: Requirements 5.5**

### Property 17: Time Format Conversion

*For any* valid mm:ss time string, converting to decimal minutes and back to mm:ss format SHALL produce an equivalent time value (round-trip property).

**Validates: Requirements 15.1, 15.2**

### Property 18: Bulk Result Save

*For any* list of test results, the bulk save operation SHALL save all results and return the complete list with calculated grades.

**Validates: Requirements 6.5**

### Property 19: Bulk Value Application

*For any* selection of students and any raw result value, applying the value to all selected students SHALL create or update test results for all students with the same raw result and correctly calculated grades.

**Validates: Requirements 6.9**

### Property 20: Test Result Update and Recalculation

*For any* existing test result and any new raw result value, updating the test result SHALL recalculate the grade based on the new raw result and the test's calculation method.

**Validates: Requirements 7.2, 7.3**

### Property 21: Missing Result Default Grade

*For any* student and test where no test result exists or rawResult is null, the system SHALL assign a calculated grade of 0.

**Validates: Requirements 8.1, 8.4**

### Property 22: Notes Without Raw Result

*For any* test result with notes but null rawResult, the system SHALL store the notes and set calculatedGrade to 0.

**Validates: Requirements 8.2**

### Property 23: Export Includes All Students

*For any* export operation, the Excel_Exporter SHALL include all students in the selected classes, including those without test results (with grade 0).

**Validates: Requirements 8.3**

### Property 24: Export Field Completeness

*For any* export operation, the generated Excel file SHALL include all required fields: student name, student ID, grade level, class name, and calculated grades for all selected tests.

**Validates: Requirements 9.2**

### Property 25: Export Notes Inclusion

*For any* export operation with includeNotes option enabled, the Excel file SHALL contain a notes column with all student notes; when disabled, no notes column SHALL be present.

**Validates: Requirements 9.3**

### Property 26: Export Test Selection

*For any* export operation with selected tests, the Excel file SHALL include grades only for the selected tests and exclude all other tests.

**Validates: Requirements 9.4**

### Property 27: Export Class Selection

*For any* export operation with selected classes, the Excel file SHALL include students only from the selected classes and exclude all other students.

**Validates: Requirements 9.5**

### Property 28: Export Validity

*For any* valid grade data exported to Excel, the resulting file SHALL be parseable as a valid Excel file.

**Validates: Requirements 10.3**

### Property 29: Export-Import Round Trip

*For any* valid grade data, exporting to Excel then importing the student data then exporting again SHALL produce an Excel file with equivalent student information (round-trip property for data integrity).

**Validates: Requirements 10.4**

### Property 30: Teacher Data Isolation

*For any* two different teachers with different assigned classes, each teacher SHALL only be able to access and view their own assigned classes and not the other teacher's classes.

**Validates: Requirements 11.2**

### Property 31: Teacher Authorization Filtering

*For any* authenticated teacher, the system SHALL display only classes assigned to that teacher and filter out all other classes.

**Validates: Requirements 13.3**

### Property 32: Unauthorized Access Prevention

*For any* teacher attempting to access or modify data for a class not assigned to them, the system SHALL reject the operation and prevent any data access or modification.

**Validates: Requirements 13.4**

### Property 33: Input Validation for Unit Type

*For any* raw result input, the validation SHALL verify the input matches the expected unit type (TIME format for TIME tests, numeric for COUNT tests) and reject mismatched inputs.

**Validates: Requirements 14.1**

### Property 34: Negative Value Rejection

*For any* negative raw result value, the validation SHALL reject the input and display an error message.

**Validates: Requirements 14.2**

### Property 35: Non-Numeric Input Rejection

*For any* non-numeric input for a numeric field, the validation SHALL reject the input and display an error message.

**Validates: Requirements 14.3**

### Property 36: Invalid Data Save Prevention

*For any* invalid data (failing validation), the system SHALL prevent saving and maintain the previous valid state.

**Validates: Requirements 14.4**

## Error Handling

### Error Categories

**1. Validation Errors**
- Invalid Excel file format
- Missing required test configuration parameters
- Invalid raw result values (negative, non-numeric, wrong format)
- Unauthorized access attempts

**2. Business Logic Errors**
- Duplicate student detection conflicts
- Test assignment to non-existent classes
- Grade calculation with invalid parameters

**3. System Errors**
- Database connection failures
- File upload/download failures
- Concurrent modification conflicts

### Error Handling Strategy

**Validation Errors**:
- Return descriptive error messages to the user
- Display errors inline next to relevant input fields
- Prevent invalid data from being saved
- Maintain previous valid state

**Business Logic Errors**:
- Log errors with context for debugging
- Return user-friendly error messages
- Provide suggestions for resolution when possible
- Roll back partial operations to maintain consistency

**System Errors**:
- Log full error details including stack traces
- Return generic error messages to users (avoid exposing internals)
- Implement retry logic for transient failures
- Provide fallback mechanisms (e.g., local storage for auto-save)

### Error Response Format

All API errors return consistent JSON structure:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid Excel file format",
    "details": [
      {
        "field": "file",
        "message": "Expected .xlsx file, received .csv"
      }
    ],
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

### Specific Error Scenarios

**Excel Import Errors**:
- Invalid file format → "Invalid Excel file format. Please upload a .xlsx file."
- Missing required columns → "Missing required column: [column name]. Please ensure your Excel file contains all required fields."
- Invalid grade level → "Invalid grade level '[value]'. Supported values: י, יא, יב"

**Test Configuration Errors**:
- Missing maxValue for RATIO → "RATIO calculation requires maxValue parameter"
- Missing targetValue/penaltyPerUnit for PENALTY → "PENALTY calculation requires targetValue and penaltyPerUnit parameters"

**Grade Entry Errors**:
- Negative raw result → "Raw result cannot be negative"
- Invalid time format → "Invalid time format. Please use mm:ss format (e.g., 10:30)"
- Non-numeric input → "Please enter a valid number"

**Authorization Errors**:
- Unauthorized access → "You do not have permission to access this class"
- Unauthenticated request → "Please log in to access this resource"

### Concurrent Modification Handling

Use optimistic locking with version numbers:
- Each entity has a version field
- On update, check version matches
- If version mismatch, return conflict error with latest data
- User can review changes and retry

## Testing Strategy

### Testing Approach

This system requires a **dual testing approach** combining property-based testing for core business logic with example-based unit tests and integration tests for UI, database, and external integrations.

### Property-Based Testing

**Applicable Components**:
- **GradeCalculator**: Pure mathematical functions with clear properties
- **TimeConverter**: Format conversion with round-trip properties
- **ExcelImporter/Exporter**: Data parsing and generation with round-trip properties
- **Validation Logic**: Input validation across wide input ranges
- **Filtering and Grouping**: Data organization functions
- **Authorization Logic**: Access control across various scenarios

**Property Testing Library**: Use **jqwik** for Java property-based testing

**Configuration**:
- Minimum **100 iterations** per property test
- Each test tagged with: `@Tag("Feature: pe-grade-management-system, Property {number}: {property_text}")`
- Custom generators for domain objects (Student, Test, TestResult)
- Edge case generators for Hebrew characters, decimal values, boundary conditions

**Example Property Test Structure**:

```java
@Property
@Tag("Feature: pe-grade-management-system, Property 11: RATIO Grade Calculation Formula")
void ratioGradeCalculationFormula(@ForAll @Positive BigDecimal rawResult,
                                   @ForAll @Positive BigDecimal maxValue) {
    // For any non-negative rawResult and positive maxValue
    Test test = new Test();
    test.setCalculationType(CalculationType.RATIO);
    test.setMaxValue(maxValue);
    
    BigDecimal grade = gradeCalculator.calculateGrade(rawResult, test);
    BigDecimal expected = rawResult.divide(maxValue, 4, RoundingMode.HALF_UP)
                                   .multiply(BigDecimal.valueOf(100))
                                   .setScale(2, RoundingMode.HALF_UP);
    
    // Grade should equal (rawResult / maxValue) * 100, rounded to 2 decimals
    assertThat(grade).isEqualByComparingTo(expected);
    
    // Grade should be in valid range
    assertThat(grade).isBetween(BigDecimal.ZERO, BigDecimal.valueOf(100));
}
```

**Custom Generators**:

```java
@Provide
Arbitrary<Student> students() {
    return Combinators.combine(
        Arbitraries.strings().alpha().ofMinLength(2).ofMaxLength(50),
        Arbitraries.strings().numeric().ofLength(9).injectNull(0.3),
        Arbitraries.of("י", "יא", "יב"),
        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10)
    ).as((name, studentId, gradeLevel, className) -> {
        Student student = new Student();
        student.setName(name);
        student.setStudentId(studentId);
        student.setGradeLevel(gradeLevel);
        student.setClassName(className);
        return student;
    });
}

@Provide
Arbitrary<BigDecimal> timeValues() {
    return Arbitraries.bigDecimals()
        .between(BigDecimal.ZERO, BigDecimal.valueOf(60))
        .ofScale(2);
}
```

### Unit Testing

**Focus Areas**:
- Specific examples demonstrating correct behavior
- Edge cases not covered by property tests
- Error message content verification
- UI component rendering (React Testing Library)
- Service layer business logic with mocked dependencies

**Example Unit Tests**:
- Test configuration creation with specific valid values
- UI rendering with sample student lists
- Error message content for specific validation failures
- Auto-save trigger timing
- Keyboard navigation behavior

### Integration Testing

**Focus Areas**:
- Database operations (Spring Data JPA repositories)
- Excel file I/O (Apache POI)
- REST API endpoints (Spring MockMvc)
- Authentication and authorization (Spring Security)
- Multi-layer interactions (Controller → Service → Repository)

**Test Database**: Use H2 in-memory database for integration tests

**Example Integration Tests**:
- Import Excel file and verify database records created
- Save test results and verify database storage with timestamps
- Export grades and verify Excel file structure
- Authenticate teacher and verify class filtering
- Concurrent update scenarios with optimistic locking

### End-to-End Testing

**Focus Areas**:
- Complete user workflows
- Mobile responsiveness
- Cross-browser compatibility
- Performance under load

**Tools**: Selenium or Playwright for browser automation

**Key Scenarios**:
- Complete workflow: Import students → Create test → Enter grades → Export
- Mobile grade entry workflow on actual mobile devices
- Multi-teacher concurrent access
- Auto-save and recovery after browser refresh

### Performance Testing

**Requirements**:
- Support 50+ concurrent teachers (Requirement 11.1)
- Response times under 2 seconds for grade entry (Requirement 11.4)

**Tools**: JMeter or Gatling for load testing

**Test Scenarios**:
- 50 concurrent teachers entering grades simultaneously
- Bulk save operations with full class (30-40 students)
- Export operations with multiple classes and tests
- Database query performance with large datasets

### Test Coverage Goals

- **Property Tests**: 100% coverage of GradeCalculator, TimeConverter, validation logic
- **Unit Tests**: 80%+ coverage of service layer business logic
- **Integration Tests**: All REST endpoints, all repository operations
- **E2E Tests**: All critical user workflows

### Continuous Integration

- Run all property tests (100 iterations each) on every commit
- Run unit and integration tests on every commit
- Run E2E tests on pull requests
- Run performance tests weekly or before releases
- Fail build if any property test fails (indicates correctness violation)

### Test Data Management

**Property Test Data**:
- Generated randomly by jqwik
- Include edge cases: empty strings, null values, boundary values, Hebrew characters
- Seed-based reproducibility for failed tests

**Integration Test Data**:
- Use test fixtures for consistent scenarios
- Reset database between tests
- Use realistic sample data (Hebrew names, Israeli student IDs)

**E2E Test Data**:
- Dedicated test teacher accounts
- Sample student rosters from realistic scenarios
- Test data cleanup after test runs

### Testing Anti-Patterns to Avoid

- ❌ Writing too many unit tests for behavior covered by property tests
- ❌ Testing framework behavior (Spring, React) instead of our code
- ❌ Mocking everything (prefer integration tests for database operations)
- ❌ Brittle UI tests that break on minor styling changes
- ❌ Property tests with too few iterations (<100)
- ❌ Ignoring failed property tests (they indicate real correctness issues)

### Testing Best Practices

- ✅ Property tests for pure functions and business logic
- ✅ Integration tests for database and external dependencies
- ✅ Example-based tests for specific scenarios and edge cases
- ✅ Tag all property tests with feature name and property number
- ✅ Use descriptive test names that explain what is being verified
- ✅ Keep tests independent and isolated
- ✅ Use test fixtures and builders for readable test setup
- ✅ Assert on behavior, not implementation details
