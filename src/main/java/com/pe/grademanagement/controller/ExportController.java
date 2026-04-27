package com.pe.grademanagement.controller;

import com.pe.grademanagement.dto.ExportRequest;
import com.pe.grademanagement.service.ExportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST controller for grade export endpoints.
 * Handles exporting grades to Excel format.
 * 
 * Requirements:
 * - 9.1, 9.2, 9.3, 9.4, 9.5, 9.6: Excel export functionality
 */
@RestController
@RequestMapping("/api/export")
@CrossOrigin
public class ExportController {
    
    private final ExportService exportService;
    
    @Autowired
    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }
    
    /**
     * Export grades to Excel file.
     * Requires authentication - only exports data for classes taught by the authenticated teacher.
     * Returns a downloadable Excel file in Ministry of Education format.
     * 
     * @param exportRequest Export configuration (class IDs, test IDs, include notes option)
     * @param authentication Spring Security authentication object
     * @return Excel file as byte array with appropriate headers for download
     */
    @PostMapping("/excel")
    public ResponseEntity<?> exportToExcel(@Valid @RequestBody ExportRequest exportRequest,
                                          Authentication authentication) {
        try {
            // Export grades (authorization handled by ExportService)
            byte[] excelFile = exportService.exportGrades(
                    exportRequest.getClassIds(),
                    exportRequest.getTestIds(),
                    exportRequest.isIncludeNotes()
            );
            
            // Generate filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "grades_export_" + timestamp + ".xlsx";
            
            // Set response headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(excelFile.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelFile);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid export request: " + e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to export grades: " + e.getMessage()));
        }
    }
    
    /**
     * Simple error response DTO.
     */
    private static class ErrorResponse {
        private final String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() {
            return error;
        }
    }
}
