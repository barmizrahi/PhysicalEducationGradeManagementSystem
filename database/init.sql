-- Database initialization script for PE Grade Management System
-- Run this script to set up the PostgreSQL database

-- Create database (run as postgres superuser)
-- CREATE DATABASE pe_grades;

-- Create user (run as postgres superuser)
-- CREATE USER pe_admin WITH PASSWORD 'change_me_in_production';
-- GRANT ALL PRIVILEGES ON DATABASE pe_grades TO pe_admin;

-- Connect to the database
\c pe_grades;

-- Enable UUID extension (optional, for future use)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO pe_admin;

-- Note: Tables will be created automatically by Hibernate/JPA
-- based on the entity definitions when the application starts.

-- The following is for reference only - actual schema is managed by JPA:

/*
Expected tables:
- teacher (id, username, password_hash, full_name, created_at)
- class (id, name, grade_level, teacher_id, created_at)
- student (id, name, student_id, grade_level, class_id, created_at, updated_at)
- test (id, name, calculation_type, unit_type, max_value, target_value, penalty_per_unit, created_by, created_at, updated_at)
- test_assignment (id, test_id, class_id, assigned_at)
- test_result (id, student_id, test_id, raw_result, calculated_grade, notes, created_at, updated_at)
*/

-- Create indexes for performance (will be created by JPA, but listed here for reference)
-- CREATE INDEX idx_student_class_id ON student(class_id);
-- CREATE INDEX idx_student_student_id ON student(student_id);
-- CREATE INDEX idx_class_teacher_id ON class(teacher_id);
-- CREATE INDEX idx_test_created_by ON test(created_by);
-- CREATE INDEX idx_test_assignment_test_id ON test_assignment(test_id);
-- CREATE INDEX idx_test_assignment_class_id ON test_assignment(class_id);
-- CREATE INDEX idx_test_result_student_id ON test_result(student_id);
-- CREATE INDEX idx_test_result_test_id ON test_result(test_id);
-- CREATE INDEX idx_test_result_composite ON test_result(test_id, student_id);

COMMENT ON DATABASE pe_grades IS 'Physical Education Grade Management System database';
