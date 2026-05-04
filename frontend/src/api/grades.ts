import apiClient from './client'
import { TestResult } from '../types'

/**
 * Grade entry and management API service
 */
export const gradesApi = {
  /**
   * Get test results for a specific class and test
   */
  getTestResultsForClass: async (classId: number, testId: number): Promise<TestResult[]> => {
    const response = await apiClient.get<TestResult[]>(`/grades/class/${classId}/test/${testId}`)
    return response.data
  },

  /**
   * Save a single test result
   */
  saveTestResult: async (result: Omit<TestResult, 'id' | 'calculatedGrade' | 'createdAt' | 'updatedAt'>): Promise<TestResult> => {
    const response = await apiClient.post<TestResult>('/grades', result)
    return response.data
  },

  /**
   * Bulk save test results for multiple students
   */
  bulkSaveTestResults: async (results: Omit<TestResult, 'id' | 'calculatedGrade' | 'createdAt' | 'updatedAt'>[]): Promise<TestResult[]> => {
    const response = await apiClient.post<TestResult[]>('/grades/bulk', { results })
    return response.data
  },
}
