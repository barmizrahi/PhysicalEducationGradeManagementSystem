package com.pe.grademanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main application class for the Physical Education Grade Management System.
 * 
 * This Spring Boot application provides a web-based platform for PE teachers to:
 * - Import student rosters from Excel files
 * - Configure tests with custom grading formulas
 * - Enter grades on mobile devices during class
 * - Export grades in Ministry of Education format
 */
@SpringBootApplication
@EnableJpaAuditing
public class GradeManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(GradeManagementApplication.class, args);
    }
}
