-- Create tables for PE Grade Management System
-- This script creates the schema and a test user

-- Create teachers table
CREATE TABLE IF NOT EXISTS teachers (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create classes table
CREATE TABLE IF NOT EXISTS classes (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    grade_level INTEGER NOT NULL,
    teacher_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE
);

-- Create students table
CREATE TABLE IF NOT EXISTS students (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    student_id VARCHAR(50) UNIQUE NOT NULL,
    grade_level INTEGER NOT NULL,
    class_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE SET NULL
);

-- Create tests table
CREATE TABLE IF NOT EXISTS tests (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    calculation_type VARCHAR(50) NOT NULL,
    unit_type VARCHAR(50) NOT NULL,
    max_value DOUBLE PRECISION,
    target_value DOUBLE PRECISION,
    penalty_per_unit DOUBLE PRECISION,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES teachers(id) ON DELETE CASCADE
);

-- Create test_assignments table
CREATE TABLE IF NOT EXISTS test_assignments (
    id BIGSERIAL PRIMARY KEY,
    test_id BIGINT NOT NULL,
    class_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES classes(id) ON DELETE CASCADE,
    UNIQUE(test_id, class_id)
);

-- Create test_results table
CREATE TABLE IF NOT EXISTS test_results (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    test_id BIGINT NOT NULL,
    raw_result DOUBLE PRECISION NOT NULL,
    calculated_grade DOUBLE PRECISION NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (test_id) REFERENCES tests(id) ON DELETE CASCADE,
    UNIQUE(student_id, test_id)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_student_class_id ON students(class_id);
CREATE INDEX IF NOT EXISTS idx_student_student_id ON students(student_id);
CREATE INDEX IF NOT EXISTS idx_class_teacher_id ON classes(teacher_id);
CREATE INDEX IF NOT EXISTS idx_test_created_by ON tests(created_by);
CREATE INDEX IF NOT EXISTS idx_test_assignment_test_id ON test_assignments(test_id);
CREATE INDEX IF NOT EXISTS idx_test_assignment_class_id ON test_assignments(class_id);
CREATE INDEX IF NOT EXISTS idx_test_result_student_id ON test_results(student_id);
CREATE INDEX IF NOT EXISTS idx_test_result_test_id ON test_results(test_id);
CREATE INDEX IF NOT EXISTS idx_test_result_composite ON test_results(test_id, student_id);

-- Insert a test teacher
-- Password is BCrypt hash of "password123"
-- Generated using: BCrypt.hashpw("password123", BCrypt.gensalt())
INSERT INTO teachers (username, password_hash, full_name, created_at)
VALUES ('teacher1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Test Teacher', CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- Verify the teacher was created
SELECT id, username, full_name, created_at FROM teachers;
