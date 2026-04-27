# Requirements Document

## Introduction

The Physical Education Grade Management System is a web application designed to streamline the grade management process for high school PE teachers. Currently, teachers manually record test results in notebooks during class and then manually enter the data into the Ministry of Education system, resulting in double data entry and inefficiency. This system will digitize the entire workflow from student import through grade calculation to export, with a focus on fast, mobile-friendly data entry during class.

## Glossary

- **PE_System**: The Physical Education Grade Management System web application
- **Student_Importer**: Component responsible for importing student data from Excel files
- **Test_Manager**: Component responsible for creating and configuring tests
- **Grade_Calculator**: Component responsible for calculating grades based on raw test results
- **Grade_Entry_Interface**: User interface for entering test results during class
- **Excel_Exporter**: Component responsible for exporting grades to Excel format
- **Ministry_Format**: Excel file format compatible with the Ministry of Education system
- **Raw_Result**: The actual measured performance (e.g., 12.5 minutes, 15 repetitions)
- **Calculated_Grade**: The final grade (0-100) computed from the raw result
- **Ratio_Calculation**: Grade calculation method based on percentage of maximum value
- **Penalty_Calculation**: Grade calculation method starting at 100 with deductions for deviation from target
- **Grade_Level**: Student's year in school (י, יא, יב)
- **Teacher_User**: A PE teacher using the system

## Requirements

### Requirement 1: Import Students from Excel

**User Story:** As a PE teacher, I want to import student data from an Excel file, so that I can quickly set up my class roster without manual data entry.

#### Acceptance Criteria

1. WHEN a valid Excel file is uploaded, THE Student_Importer SHALL parse the file and extract student data
2. THE Student_Importer SHALL support mapping between Excel columns and system fields (name, student ID, grade level, class name)
3. WHEN student data is successfully parsed, THE Student_Importer SHALL create Student records in the database
4. IF the Excel file format is invalid, THEN THE Student_Importer SHALL return a descriptive error message
5. THE Student_Importer SHALL support Excel files containing grade levels י, יא, and יב
6. WHEN a student ID is not present in the Excel file, THE Student_Importer SHALL create the student record without a student ID
7. WHEN importing students, THE Student_Importer SHALL detect existing students using student ID if available
8. IF student ID is not available, THE Student_Importer SHALL use a combination of name and class to detect duplicates
9. THE Student_Importer SHALL update existing student records instead of creating duplicates

### Requirement 2: Manage Student Data

**User Story:** As a PE teacher, I want to view and organize my students by grade level and class, so that I can easily find and work with specific groups.

#### Acceptance Criteria

1. THE PE_System SHALL display students grouped by grade level
2. THE PE_System SHALL display students grouped by class name within each grade level
3. WHEN a teacher selects a grade level, THE PE_System SHALL display all classes for that grade level
4. WHEN a teacher selects a class, THE PE_System SHALL display all students in that class

### Requirement 3: Create and Configure Tests

**User Story:** As a PE teacher, I want to create tests with specific calculation methods and parameters, so that grades are automatically calculated according to my grading criteria.

#### Acceptance Criteria

1. THE Test_Manager SHALL allow creation of tests with a name, calculation type, and unit type
2. THE Test_Manager SHALL support two calculation types: RATIO and PENALTY
3. THE Test_Manager SHALL support two unit types: TIME and COUNT
4. WHERE calculation type is RATIO, THE Test_Manager SHALL require a maxValue parameter
5. WHERE calculation type is PENALTY, THE Test_Manager SHALL require targetValue and penaltyPerUnit parameters
6. THE Test_Manager SHALL store test configurations in the database
7. THE Test_Manager SHALL allow editing of existing test configurations

### Requirement 4: Calculate Grades Using Ratio Method

**User Story:** As a PE teacher, I want grades calculated as a percentage of maximum value, so that students are graded on how close they get to the target performance.

#### Acceptance Criteria

1. WHERE a test uses RATIO calculation, THE Grade_Calculator SHALL compute the grade as (rawResult / maxValue) * 100
2. THE Grade_Calculator SHALL support non-integer raw results (e.g., 15.5 repetitions)
3. WHEN the raw result equals or bigger maxValue, THE Grade_Calculator SHALL return a grade of 100
4. WHEN the raw result is zero, THE Grade_Calculator SHALL return a grade of 0
5. THE Grade_Calculator SHALL round the calculated grade to two decimal places


### Requirement 5: Calculate Grades Using Penalty Method

**User Story:** As a PE teacher, I want grades calculated with penalties for deviation from a target value, so that students are graded on how close they get to an ideal performance.

#### Acceptance Criteria

1. WHERE a test uses PENALTY calculation, THE Grade_Calculator SHALL compute the grade as 100 - ((rawResult - targetValue) * penaltyPerUnit)
2. THE Grade_Calculator SHALL support non-integer raw results (e.g., 10.5 minutes)
3. WHEN the raw result is better than targetValue, THE Grade_Calculator SHALL return a grade of 100 (no bonus above 100)
4. WHEN the calculated grade is less than zero, THE Grade_Calculator SHALL return a grade of 0
5. THE Grade_Calculator SHALL apply linear calculation for all deviations from target
6. THE Grade_Calculator SHALL round the calculated grade to two decimal places

### Requirement 15: Handle Time Input Format
**User Story:** As a PE teacher, I want to enter time values in a natural format, so that I can quickly input results during class.

#### Acceptance Criteria

1. THE Grade_Entry_Interface SHALL accept time input in mm:ss format (e.g., 10:30)
2. THE PE_System SHALL convert mm:ss format into decimal minutes internally (e.g., 10:30 → 10.5)
3. THE PE_System SHALL store all TIME values as decimal numbers
4. THE Grade_Calculator SHALL operate on decimal time values

### Requirement 6: Enter Test Results for Entire Class

**User Story:** As a PE teacher, I want to enter test results for all students in a class on one screen, so that I can quickly record grades during or immediately after class.

#### Acceptance Criteria

1. WHEN a teacher selects a class and test, THE Grade_Entry_Interface SHALL display all students in that class
2. THE Grade_Entry_Interface SHALL provide input fields for raw results for each student
3. WHEN a raw result is entered, THE Grade_Calculator SHALL automatically calculate and display the grade
4. THE Grade_Entry_Interface SHALL allow entry of optional notes for each student
5. THE Grade_Entry_Interface SHALL support bulk save of all entered results
6. THE Grade_Entry_Interface SHALL be optimized for mobile device use
7. THE Grade_Entry_Interface SHALL support fast keyboard navigation between input fields
8. THE Grade_Entry_Interface SHALL automatically save data periodically (auto-save) or warn users before leaving without saving
9. THE Grade_Entry_Interface SHALL support applying the same raw result to multiple students (bulk action)

### Requirement 7: Store Test Results

**User Story:** As a PE teacher, I want test results saved with both raw values and calculated grades, so that I can review and export the data later.

#### Acceptance Criteria

1. WHEN a test result is saved, THE PE_System SHALL store the student ID, test ID, raw result, calculated grade, and notes
2. THE PE_System SHALL allow updating of existing test results
3. WHEN a test result is updated, THE Grade_Calculator SHALL recalculate the grade
4. THE PE_System SHALL preserve the timestamp of when each result was created and last modified

### Requirement 8: Handle Students Without Test Results

**User Story:** As a PE teacher, I want to record when a student did not take a test, so that I can distinguish between zero performance and absence.

#### Acceptance Criteria

1. WHERE a student has no test result, THE PE_System SHALL assign a grade of 0 for that test
2. THE Grade_Entry_Interface SHALL allow entry of notes without a raw result (e.g., "not tested", "was injured"). it can be from a dropDown list of options or free write note
3. WHEN exporting grades, THE Excel_Exporter SHALL include students without test results with a grade of 0
4. WHERE a student has no raw result, THE PE_System SHALL store rawResult as null and calculatedGrade as 0

### Requirement 9: Export Grades to Excel

**User Story:** As a PE teacher, I want to export grades to an Excel file compatible with the Ministry of Education system, so that I can submit grades without manual re-entry.

#### Acceptance Criteria

1. WHEN a teacher requests grade export, THE Excel_Exporter SHALL generate an Excel file in Ministry_Format
2. THE Excel_Exporter SHALL include student name, student ID, grade level, class name, and calculated grades
3. WHERE notes are present, THE Excel_Exporter SHALL optionally include notes in the export
4. THE Excel_Exporter SHALL allow selection of which tests to include in the export
5. THE Excel_Exporter SHALL allow selection of which classes to include in the export
6. THE Excel_Exporter SHALL generate a downloadable file that the teacher can save locally

### Requirement 10: Parse Excel Files

**User Story:** As a developer, I want to parse Excel files reliably, so that student import and grade export work correctly.

#### Acceptance Criteria

1. WHEN a valid Excel file is provided, THE Student_Importer SHALL parse it into Student objects
2. WHEN an invalid Excel file is provided, THE Student_Importer SHALL return a descriptive error message
3. THE Excel_Exporter SHALL format grade data into valid Excel files
4. FOR ALL valid grade data, exporting then importing then exporting SHALL produce an equivalent Excel file (round-trip property for data integrity)

### Requirement 11: Support Concurrent Teacher Access

**User Story:** As a system administrator, I want the system to support dozens of teachers using it simultaneously, so that all teachers can work without performance degradation.

#### Acceptance Criteria

1. THE PE_System SHALL support at least 50 concurrent Teacher_Users
2. WHEN multiple teachers access different classes, THE PE_System SHALL isolate their data
3. THE PE_System SHALL prevent data conflicts when multiple teachers work with the same data
4. THE PE_System SHALL maintain response times under 2 seconds for grade entry operations under normal load

### Requirement 12: Provide Mobile-Optimized Interface

**User Story:** As a PE teacher, I want to use the system on my phone during class, so that I can enter grades in real-time on the field.

#### Acceptance Criteria

1. THE Grade_Entry_Interface SHALL render correctly on mobile devices with screen widths of 375px or greater
2. THE Grade_Entry_Interface SHALL support touch input for all interactive elements
3. THE Grade_Entry_Interface SHALL use font sizes of at least 16px for input fields to prevent automatic zoom on mobile browsers
4. THE Grade_Entry_Interface SHALL minimize scrolling required to enter results for a full class

### Requirement 13: Authenticate and Authorize Users

**User Story:** As a PE teacher, I want to log in securely and only access my own classes, so that student data remains private.

#### Acceptance Criteria

1. THE PE_System SHALL require Teacher_Users to authenticate before accessing any student data
2. THE PE_System SHALL associate each Teacher_User with their assigned classes
3. WHEN a Teacher_User is authenticated, THE PE_System SHALL only display classes assigned to that teacher
4. THE PE_System SHALL prevent Teacher_Users from accessing or modifying data for classes not assigned to them

### Requirement 14: Validate Input Data

**User Story:** As a PE teacher, I want the system to validate my input, so that I catch errors before saving grades.

#### Acceptance Criteria

1. WHEN a raw result is entered, THE Grade_Entry_Interface SHALL validate that it matches the expected unit type (TIME or COUNT)
2. IF a raw result is negative, THEN THE Grade_Entry_Interface SHALL display an error message
3. IF a raw result is non-numeric for a numeric field, THEN THE Grade_Entry_Interface SHALL display an error message
4. THE Grade_Entry_Interface SHALL prevent saving of invalid data
5. THE Grade_Entry_Interface SHALL display validation errors inline next to the relevant input field

**User Story:** As a PE teacher, I want to assign specific tests to specific classes, so that only relevant tests are shown when entering grades.

#### Acceptance Criteria

1. THE Test_Manager SHALL allow assigning tests to one or more classes
2. WHEN a teacher selects a class, THE PE_System SHALL display only tests assigned to that class
3. THE PE_System SHALL allow assigning tests at the grade level (י/יא/יב) or class level