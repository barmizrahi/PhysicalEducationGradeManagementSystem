import apiClient from './client'
import { Student, ImportResult, ColumnMapping } from '../types'

/**
 * Student management API service
 */
export const studentsApi = {
  /**
   * Import students from Excel file
   * Backend now expects fixed 4-column format (studentId, name, gradeLevel, className)
   * columnMapping parameter is optional for backward compatibility but ignored by backend
   */
  importStudents: async (file: File, columnMapping?: ColumnMapping): Promise<ImportResult> => {
    const formData = new FormData()
    formData.append('file', file)
    
    // Include columnMapping only if provided (for backward compatibility)
    if (columnMapping) {
      formData.append('columnMapping', JSON.stringify(columnMapping))
    }

    const response = await apiClient.post<ImportResult>('/students/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  /**
   * Get students grouped by grade level and class
   */
  getStudentsByGradeAndClass: async (): Promise<Record<string, Record<string, Student[]>>> => {
    const response = await apiClient.get<Record<string, Record<string, Student[]>>>('/students/by-grade-and-class')
    return response.data
  },

  /**
   * Get students in a specific class
   */
  getStudentsByClass: async (classId: number): Promise<Student[]> => {
    const response = await apiClient.get<Student[]>(`/students/class/${classId}`)
    return response.data
  },

  /**
   * Delete a single student
   */
  deleteStudent: async (studentId: number): Promise<void> => {
    await apiClient.delete(`/students/${studentId}`)
  },

  /**
   * Delete an entire class and all its students
   */
  deleteClass: async (classId: number): Promise<{ message: string; studentsDeleted: number }> => {
    const response = await apiClient.delete<{ message: string; studentsDeleted: number }>(`/students/class/${classId}`)
    return response.data
  },
}
