# Implementation Plan: Physical Education Grade Management System

## Overview

This implementation plan breaks down the PE Grade Management System into discrete, actionable coding tasks. The system is a Java 23 Spring Boot web application with React frontend that enables PE teachers to import students, configure tests, enter grades on mobile devices, and export to Ministry of Education format.

**Implementation Approach**:
- Start with core domain models and database layer
- Build business logic components (calculators, importers, exporters)
- Implement REST API endpoints
- Create React frontend components
- Add authentication and authorization
- Integrate all components

**Technology Stack**: Java 23, Spring Boot, Spring Data JPA, PostgreSQL, React, Apache POI, Spring Security with JWT

## Tasks

- [x] 1. Set up project structure and dependencies
  - Create Spring Boot project with Java 23
  - Add dependencies: Spring Web, Spring Data JPA, PostgreSQL, Apache POI, Spring Security, JWT
  - Configure application.properties for database connection
  - Set up React frontend project structure
  - Configure CORS for local development
  - _Requirements: All (foundation)_

- [x] 2. Create core domain entities and database schema
  - [x] 2.1 Implement Teacher entity
    - Create Teacher entity with id, username, passwordHash, fullName, createdAt
    - Add unique constraint on username
    - _Requirements: 13.1, 13.2_
  
  - [x] 2.2 Implement Class entity
    - Create Class entity with id, name, gradeLevel, teacherId, createdAt
    - Add relationship to Teacher (many-to-one)
    - Support Hebrew grade levels (י, יא, יב)
    - _Requirements: 2.1, 2.2, 2.3_
  
  - [x] 2.3 Implement Student entity
    - Create Student entity with id, name, studentId (nullable), gradeLevel, classId, createdAt, updatedAt
    - Add relationship to Class (many-to-one)
    - Support Hebrew characters in name field
    - _Requirements: 1.1, 1.3, 1.6_
  
  - [x] 2.4 Implement Test entity
    - Create Test entity with id, name, calculationType, unitType, maxValue, targetValue, penaltyPerUnit, createdBy, createdAt, updatedAt
    - Add enums for CalculationType (RATIO, PENALTY) and UnitType (TIME, COUNT)
    - Add relationship to Teacher (many-to-one)
    - Use BigDecimal for all numeric parameters
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_
  
  - [x] 2.5 Implement TestAssignment entity
    - Create TestAssignment entity with id, testId, classId, assignedAt
    - Add relationships to Test and Class (many-to-one)
    - Add unique constraint on (testId, classId)
    - _Requirements: 15 (test assignment)_
  
  - [x] 2.6 Implement TestResult entity
    - Create TestResult entity with id, studentId, testId, rawResult (nullable), calculatedGrade, notes, createdAt, updatedAt
    - Add relationships to Student and Test (many-to-one)
    - Add unique constraint on (testId, studentId)
    - Use BigDecimal for rawResult and calculatedGrade
    - _Requirements: 7.1, 7.2, 7.4, 8.1, 8.2_

- [x] 3. Create Spring Data JPA repositories
  - Create repository interfaces for all entities: TeacherRepository, ClassRepository, StudentRepository, TestRepository, TestAssignmentRepository, TestResultRepository
  - Add custom query methods for filtering by teacher, class, grade level
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 13.3_

- [x] 4. Implement TimeConverter utility
  - [x] 4.1 Create TimeConverter class with conversion methods
    - Implement convertToDecimalMinutes(String timeString) to parse mm:ss format
    - Implement convertToTimeFormat(BigDecimal decimalMinutes) for display
    - Add validation for time format
    - _Requirements: 15.1, 15.2, 15.3_
  
  - [x] 4.2 Write property test for time format conversion
    - **Property 17: Time Format Conversion**
    - **Validates: Requirements 15.1, 15.2**
    - Test round-trip conversion: mm:ss → decimal → mm:ss produces equivalent value

- [x] 5. Implement GradeCalculator component
  - [x] 5.1 Create GradeCalculator class with calculation methods
    - Implement calculateGrade(BigDecimal rawResult, Test test) dispatcher method
    - Implement calculateRatioGrade(BigDecimal rawResult, BigDecimal maxValue)
    - Implement calculatePenaltyGrade(BigDecimal rawResult, BigDecimal targetValue, BigDecimal penaltyPerUnit)
    - Use BigDecimal for all calculations with proper rounding (2 decimal places)
    - Handle zero and null raw results
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 5.1, 5.2, 5.3, 5.4, 5.5, 5.6_
  
  - [x]* 5.2 Write property test for RATIO grade calculation formula
    - **Property 11: RATIO Grade Calculation Formula**
    - **Validates: Requirements 4.1, 4.5**
    - Test: grade = (rawResult / maxValue) * 100, rounded to 2 decimals
  
  - [x]* 5.3 Write property test for RATIO grade upper bound
    - **Property 12: RATIO Grade Upper Bound**
    - **Validates: Requirements 4.3**
    - Test: rawResult >= maxValue returns grade of 100
  
  - [x]* 5.4 Write property test for PENALTY grade calculation formula
    - **Property 13: PENALTY Grade Calculation Formula**
    - **Validates: Requirements 5.1, 5.6**
    - Test: grade = 100 - ((rawResult - targetValue) * penaltyPerUnit), rounded to 2 decimals
  
  - [x]* 5.5 Write property test for PENALTY grade upper bound
    - **Property 14: PENALTY Grade Upper Bound**
    - **Validates: Requirements 5.3**
    - Test: rawResult better than targetValue returns grade of 100
  
  - [x]* 5.6 Write property test for PENALTY grade lower bound
    - **Property 15: PENALTY Grade Lower Bound**
    - **Validates: Requirements 5.4**
    - Test: negative calculated grades return 0
  
  - [x]* 5.7 Write property test for PENALTY calculation linearity
    - **Property 16: PENALTY Calculation Linearity**
    - **Validates: Requirements 5.5**
    - Test: doubling deviation from target doubles the penalty

- [x] 6. Checkpoint - Ensure core calculation logic works
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. Implement ExcelImporter component
  - [x] 7.1 Create ExcelImporter class with import methods
    - Implement importStudents(MultipartFile file, ColumnMapping columnMapping)
    - Implement validateExcelFormat(MultipartFile file)
    - Use Apache POI to parse Excel files
    - Support flexible column mapping for name, studentId, gradeLevel, className
    - Handle Hebrew characters and Hebrew grade levels (י, יא, יב)
    - Return descriptive error messages for invalid formats
    - _Requirements: 1.1, 1.2, 1.4, 1.5, 10.1, 10.2_
  
  - [ ]* 7.2 Write property test for Excel import parsing
    - **Property 1: Excel Import Parsing**
    - **Validates: Requirements 1.1, 1.2, 10.1**
    - Test: valid Excel files are successfully parsed with correct field mapping
  
  - [ ]* 7.3 Write property test for Excel import error handling
    - **Property 2: Excel Import Error Handling**
    - **Validates: Requirements 1.4, 10.2**
    - Test: invalid Excel files return descriptive errors without creating records
  
  - [ ]* 7.4 Write property test for optional student ID handling
    - **Property 3: Optional Student ID Handling**
    - **Validates: Requirements 1.6**
    - Test: missing student IDs result in null values, not errors

- [x] 8. Implement StudentService component
  - [x] 8.1 Create StudentService class with CRUD and duplicate detection
    - Implement saveStudent(Student student)
    - Implement findExistingStudent(String studentId, String name, String className)
    - Implement getStudentsByGradeAndClass(Long teacherId)
    - Detect duplicates by student ID (if available) or name+class combination
    - Update existing records instead of creating duplicates
    - Filter students by teacher authorization
    - _Requirements: 1.7, 1.8, 1.9, 2.1, 2.2, 2.3, 2.4, 13.3_
  
  - [ ]* 8.2 Write property test for duplicate detection and update
    - **Property 4: Duplicate Detection and Update**
    - **Validates: Requirements 1.7, 1.8, 1.9**
    - Test: existing students are updated, not duplicated
  
  - [ ]* 8.3 Write property test for student grouping by grade and class
    - **Property 5: Student Grouping by Grade and Class**
    - **Validates: Requirements 2.1, 2.2**
    - Test: students organized by grade level then class, each in exactly one group
  
  - [ ]* 8.4 Write property test for class filtering by grade level
    - **Property 6: Class Filtering by Grade Level**
    - **Validates: Requirements 2.3**
    - Test: filtering returns all and only classes for selected grade level
  
  - [ ]* 8.5 Write property test for student filtering by class
    - **Property 7: Student Filtering by Class**
    - **Validates: Requirements 2.4**
    - Test: filtering returns all and only students for selected class

- [x] 9. Implement TestService component
  - [x] 9.1 Create TestService class with test management methods
    - Implement createTest(Test test)
    - Implement updateTest(Long testId, Test test)
    - Implement assignTestToClasses(Long testId, List<Long> classIds)
    - Implement getTestsForClass(Long classId)
    - Validate test configuration based on calculation type
    - Require maxValue for RATIO tests
    - Require targetValue and penaltyPerUnit for PENALTY tests
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 15_
  
  - [ ]* 9.2 Write property test for RATIO test configuration validation
    - **Property 8: Test Configuration Validation for RATIO**
    - **Validates: Requirements 3.4**
    - Test: RATIO tests require non-null maxValue
  
  - [ ]* 9.3 Write property test for PENALTY test configuration validation
    - **Property 9: Test Configuration Validation for PENALTY**
    - **Validates: Requirements 3.5**
    - Test: PENALTY tests require non-null targetValue and penaltyPerUnit
  
  - [ ]* 9.4 Write property test for test configuration updates
    - **Property 10: Test Configuration Updates**
    - **Validates: Requirements 3.7**
    - Test: updates are persisted and returned correctly

- [x] 10. Implement GradeService component
  - [ ] 10.1 Create GradeService class with result management methods
    - Implement saveTestResult(TestResult result)
    - Implement getTestResultsForClass(Long classId, Long testId)
    - Implement bulkSaveTestResults(List<TestResult> results)
    - Automatically calculate grade when saving result using GradeCalculator
    - Recalculate grade when updating result
    - Store timestamps for creation and modification
    - Handle null rawResult with calculatedGrade = 0
    - _Requirements: 6.3, 6.5, 7.1, 7.2, 7.3, 7.4, 8.1, 8.2, 8.4_
  
  - [ ]* 10.2 Write property test for bulk result save
    - **Property 18: Bulk Result Save**
    - **Validates: Requirements 6.5**
    - Test: bulk save saves all results with calculated grades
  
  - [ ]* 10.3 Write property test for test result update and recalculation
    - **Property 20: Test Result Update and Recalculation**
    - **Validates: Requirements 7.2, 7.3**
    - Test: updating raw result recalculates grade
  
  - [ ]* 10.4 Write property test for missing result default grade
    - **Property 21: Missing Result Default Grade**
    - **Validates: Requirements 8.1, 8.4**
    - Test: null rawResult results in grade of 0
  
  - [ ]* 10.5 Write property test for notes without raw result
    - **Property 22: Notes Without Raw Result**
    - **Validates: Requirements 8.2**
    - Test: notes can be stored with null rawResult and grade 0

- [x] 11. Checkpoint - Ensure business logic layer works
  - Ensure all tests pass, ask the user if questions arise.

- [x] 12. Implement ExcelExporter component
  - [x] 12.1 Create ExcelExporter class with export methods
    - Implement exportGrades(ExportConfig exportConfig)
    - Implement generateMinistryFormatExcel(List<Student> students, Map<Student, Map<Test, TestResult>> testResults, boolean includeNotes)
    - Use Apache POI to generate Excel files
    - Include columns: name, student ID, grade level, class name, test grades
    - Optionally include notes column
    - Support selective inclusion of tests and classes
    - Include students without test results (grade = 0)
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 8.3_
  
  - [ ]* 12.2 Write property test for export field completeness
    - **Property 24: Export Field Completeness**
    - **Validates: Requirements 9.2**
    - Test: exported Excel includes all required fields
  
  - [ ]* 12.3 Write property test for export notes inclusion
    - **Property 25: Export Notes Inclusion**
    - **Validates: Requirements 9.3**
    - Test: notes column present when enabled, absent when disabled
  
  - [ ]* 12.4 Write property test for export test selection
    - **Property 26: Export Test Selection**
    - **Validates: Requirements 9.4**
    - Test: only selected tests included in export
  
  - [ ]* 12.5 Write property test for export class selection
    - **Property 27: Export Class Selection**
    - **Validates: Requirements 9.5**
    - Test: only students from selected classes included
  
  - [ ]* 12.6 Write property test for export includes all students
    - **Property 23: Export Includes All Students**
    - **Validates: Requirements 8.3**
    - Test: all students in selected classes included, even without results
  
  - [ ]* 12.7 Write property test for export validity
    - **Property 28: Export Validity**
    - **Validates: Requirements 10.3**
    - Test: exported files are valid Excel files
  
  - [ ]* 12.8 Write property test for export-import round trip
    - **Property 29: Export-Import Round Trip**
    - **Validates: Requirements 10.4**
    - Test: export → import → export produces equivalent student data

- [x] 13. Implement input validation utilities
  - [x] 13.1 Create InputValidator class with validation methods
    - Implement validateRawResult(String input, UnitType unitType)
    - Validate TIME format (mm:ss) for TIME tests
    - Validate numeric format for COUNT tests
    - Reject negative values
    - Reject non-numeric input for numeric fields
    - Return descriptive error messages
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5_
  
  - [ ]* 13.2 Write property test for input validation for unit type
    - **Property 33: Input Validation for Unit Type**
    - **Validates: Requirements 14.1**
    - Test: validation verifies input matches expected unit type
  
  - [ ]* 13.3 Write property test for negative value rejection
    - **Property 34: Negative Value Rejection**
    - **Validates: Requirements 14.2**
    - Test: negative values are rejected with error message
  
  - [ ]* 13.4 Write property test for non-numeric input rejection
    - **Property 35: Non-Numeric Input Rejection**
    - **Validates: Requirements 14.3**
    - Test: non-numeric input rejected with error message
  
  - [ ]* 13.5 Write property test for invalid data save prevention
    - **Property 36: Invalid Data Save Prevention**
    - **Validates: Requirements 14.4**
    - Test: invalid data cannot be saved, previous state maintained

- [-] 14. Implement authentication and authorization
  - [x] 14.1 Create AuthService with JWT token generation
    - Implement login(String username, String password)
    - Implement logout()
    - Generate JWT tokens for authenticated teachers
    - Hash passwords using BCrypt
    - _Requirements: 13.1_
  
  - [x] 14.2 Configure Spring Security with JWT authentication
    - Create JWT authentication filter
    - Configure security rules for API endpoints
    - Require authentication for all student data endpoints
    - _Requirements: 13.1_
  
  - [x] 14.3 Implement authorization filters in services
    - Add teacher authorization checks in StudentService
    - Add teacher authorization checks in GradeService
    - Filter classes by teacher assignment
    - Prevent access to other teachers' data
    - _Requirements: 13.2, 13.3, 13.4, 11.2_
  
  - [ ]* 14.4 Write property test for teacher data isolation
    - **Property 30: Teacher Data Isolation**
    - **Validates: Requirements 11.2**
    - Test: teachers can only access their own assigned classes
  
  - [ ]* 14.5 Write property test for teacher authorization filtering
    - **Property 31: Teacher Authorization Filtering**
    - **Validates: Requirements 13.3**
    - Test: system displays only assigned classes for authenticated teacher
  
  - [ ]* 14.6 Write property test for unauthorized access prevention
    - **Property 32: Unauthorized Access Prevention**
    - **Validates: Requirements 13.4**
    - Test: access to unassigned classes is rejected

- [x] 15. Checkpoint - Ensure security layer works
  - Ensure all tests pass, ask the user if questions arise.

- [x] 16. Implement REST API controllers
  - [x] 16.1 Create StudentController with student management endpoints
    - POST /api/students/import - Import students from Excel
    - GET /api/students/by-grade-and-class - Get students grouped
    - GET /api/students/class/{classId} - Get students in class
    - Add request validation and error handling
    - _Requirements: 1.1, 1.2, 1.4, 2.1, 2.2, 2.3, 2.4_
  
  - [x] 16.2 Create TestController with test management endpoints
    - POST /api/tests - Create test configuration
    - PUT /api/tests/{id} - Update test configuration
    - POST /api/tests/{id}/assign - Assign test to classes
    - GET /api/tests/class/{classId} - Get tests for class
    - Add request validation and error handling
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 15_
  
  - [x] 16.3 Create GradeController with grade entry endpoints
    - GET /api/grades/class/{classId}/test/{testId} - Get results for class/test
    - POST /api/grades - Save single test result
    - POST /api/grades/bulk - Bulk save test results
    - Add request validation and error handling
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 7.1, 7.2_
  
  - [x] 16.4 Create ExportController with export endpoint
    - POST /api/export/excel - Export grades to Excel
    - Return downloadable file
    - Add request validation and error handling
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6_
  
  - [x] 16.5 Create AuthController with authentication endpoints
    - POST /api/auth/login - Teacher login
    - POST /api/auth/logout - Teacher logout
    - Add request validation and error handling
    - _Requirements: 13.1_
  
  - [ ]* 16.6 Write integration tests for all REST endpoints
    - Test all endpoints with valid and invalid inputs
    - Test authentication and authorization
    - Test error responses
    - Use MockMvc for controller testing

- [x] 17. Create React frontend structure
  - [x] 17.1 Set up React project with routing
    - Create React app with TypeScript
    - Set up React Router for navigation
    - Configure Axios for API calls
    - Set up authentication context
    - _Requirements: All (frontend foundation)_
  
  - [x] 17.2 Create shared UI components
    - Create Button, Input, Select, Table components
    - Create ErrorMessage, LoadingSpinner components
    - Ensure mobile-responsive design (min 375px width)
    - Use font sizes >= 16px for inputs (prevent mobile zoom)
    - _Requirements: 12.1, 12.2, 12.3_

- [x] 18. Implement student management UI
  - [x] 18.1 Create StudentImport component
    - File upload interface for Excel files
    - Column mapping configuration
    - Display import results and errors
    - Mobile-responsive layout
    - _Requirements: 1.1, 1.2, 1.4_
  
  - [x] 18.2 Create StudentList component
    - Display students grouped by grade level and class
    - Grade level selector
    - Class selector within grade level
    - Student list display
    - Mobile-responsive layout
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [x] 19. Implement test management UI
  - [x] 19.1 Create TestForm component
    - Form for creating/editing tests
    - Calculation type selector (RATIO/PENALTY)
    - Unit type selector (TIME/COUNT)
    - Conditional fields based on calculation type
    - Validation and error display
    - Mobile-responsive layout
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.7_
  
  - [x] 19.2 Create TestAssignment component
    - Interface for assigning tests to classes
    - Class selection (multiple)
    - Grade level or class level assignment
    - Mobile-responsive layout
    - _Requirements: 15_

- [x] 20. Implement grade entry UI
  - [x] 20.1 Create GradeEntry component
    - Class and test selector
    - Display all students in selected class
    - Input fields for raw results (one per student)
    - Real-time grade calculation display
    - Optional notes field per student
    - Time format input (mm:ss) for TIME tests
    - Input validation with inline error messages
    - Mobile-optimized touch input
    - Fast keyboard navigation between fields
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 14.1, 14.2, 14.3, 14.5, 15.1, 12.1, 12.2, 12.3, 6.7_
  
  - [x] 20.2 Add auto-save functionality to GradeEntry
    - Periodic auto-save (every 30 seconds)
    - Warning before leaving without saving
    - Visual indicator of save status
    - _Requirements: 6.8_
  
  - [x] 20.3 Add bulk actions to GradeEntry
    - Interface to select multiple students
    - Apply same raw result to selected students
    - Bulk save operation
    - _Requirements: 6.9, 6.5_
  
  - [ ]* 20.4 Write property test for bulk value application
    - **Property 19: Bulk Value Application**
    - **Validates: Requirements 6.9**
    - Test: applying value to multiple students creates/updates all results correctly

- [x] 21. Implement export UI
  - [x] 21.1 Create ExportForm component
    - Class selection (multiple)
    - Test selection (multiple)
    - Include notes checkbox
    - Export button triggering download
    - Mobile-responsive layout
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6_

- [x] 22. Implement authentication UI
  - [x] 22.1 Create Login component
    - Username and password inputs
    - Login button
    - Error message display
    - Mobile-responsive layout
    - _Requirements: 13.1_
  
  - [x] 22.2 Create authentication routing
    - Protected routes requiring authentication
    - Redirect to login if not authenticated
    - Store JWT token in local storage
    - Add token to all API requests
    - _Requirements: 13.1_

- [x] 23. Checkpoint - Ensure frontend works end-to-end
  - Ensure all tests pass, ask the user if questions arise.

- [x] 24. Implement error handling across the application
  - [x] 24.1 Add global error handling in backend
    - Create @ControllerAdvice for exception handling
    - Return consistent error JSON format
    - Log errors with context
    - Handle validation errors, business logic errors, system errors
    - _Requirements: All (error handling)_
  
  - [x] 24.2 Add error handling in frontend
    - Create error boundary component
    - Display user-friendly error messages
    - Handle API errors gracefully
    - Show inline validation errors
    - _Requirements: 14.5, All (error handling)_

- [x] 25. Implement concurrent access handling
  - [x] 25.1 Add optimistic locking to entities
    - Add @Version field to all entities
    - Handle version conflicts in services
    - Return conflict errors with latest data
    - _Requirements: 11.3_
  
  - [ ]* 25.2 Write integration tests for concurrent modifications
    - Test concurrent updates to same test result
    - Test concurrent updates to same test configuration
    - Verify optimistic locking prevents conflicts

- [x] 26. Optimize for mobile performance
  - [x] 26.1 Minimize scrolling in GradeEntry component
    - Use sticky headers for class/test info
    - Optimize layout for 375px width
    - Test on actual mobile devices
    - _Requirements: 12.4_
  
  - [ ]* 26.2 Write performance tests for grade entry operations
    - Test response times under normal load
    - Verify < 2 second response times
    - Test with 50 concurrent teachers
    - _Requirements: 11.1, 11.4_

- [x] 27. Add database indexes for performance
  - Create indexes on foreign keys (classId, teacherId, testId, studentId)
  - Create index on Student.studentId for duplicate detection
  - Create composite index on (testId, studentId) for TestResult queries
  - _Requirements: 11.4_

- [x] 28. Create sample data and seed scripts
  - Create SQL scripts to seed test teachers, classes, students
  - Create sample tests with both RATIO and PENALTY calculations
  - Create sample test results
  - Use realistic Hebrew names and Israeli student IDs
  - _Requirements: All (testing and demo)_

- [ ] 29. Write end-to-end tests for critical workflows
  - [ ]* 29.1 Write E2E test for complete workflow
    - Test: Import students → Create test → Enter grades → Export
    - Use Selenium or Playwright
    - Test on desktop and mobile viewports
  
  - [ ]* 29.2 Write E2E test for mobile grade entry
    - Test grade entry workflow on mobile device
    - Verify touch input works correctly
    - Verify auto-save functionality
  
  - [ ]* 29.3 Write E2E test for multi-teacher access
    - Test concurrent access by multiple teachers
    - Verify data isolation between teachers

- [x] 30. Final integration and testing
  - [x] 30.1 Integration testing across all layers
    - Test complete workflows from UI to database
    - Verify all REST endpoints work correctly
    - Test authentication and authorization flows
    - Test Excel import and export with real files
  
  - [x] 30.2 Cross-browser testing
    - Test on Chrome, Firefox, Safari
    - Test on mobile browsers (iOS Safari, Chrome Mobile)
    - Fix any browser-specific issues
  
  - [x] 30.3 Accessibility testing
    - Verify keyboard navigation works
    - Test with screen readers
    - Ensure WCAG compliance for basic accessibility
  
  - [x] 30.4 Performance testing
    - Load test with 50 concurrent users
    - Verify response times under load
    - Optimize slow queries if needed

- [x] 31. Final checkpoint - Complete system verification
  - Ensure all tests pass, ask the user if questions arise.
  - Verify all requirements are implemented
  - Verify all correctness properties are tested
  - Review code for security issues
  - Review code for performance issues

## Notes

- Tasks marked with `*` are optional testing tasks and can be skipped for faster MVP delivery
- Each task references specific requirements for traceability
- Property tests validate universal correctness properties from the design document
- Use jqwik for property-based testing with minimum 100 iterations per property
- All property tests should be tagged with: `@Tag("Feature: pe-grade-management-system, Property {number}: {property_text}")`
- Integration tests should use H2 in-memory database
- E2E tests should use Selenium or Playwright
- Checkpoints ensure incremental validation and provide opportunities for user feedback
- The system uses Java 23 with Spring Boot, React, PostgreSQL, and Apache POI
- All numeric calculations use BigDecimal for precision
- Hebrew character support is required throughout (names, grade levels)
- Mobile-first design is critical for the grade entry interface
