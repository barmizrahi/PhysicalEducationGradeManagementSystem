import apiClient from './client'
import { Test } from '../types'

/**
 * Test management API service
 */
export const testsApi = {
  /**
   * Create a new test configuration
   */
  createTest: async (test: Omit<Test, 'id' | 'createdBy' | 'createdAt' | 'updatedAt'>): Promise<Test> => {
    const response = await apiClient.post<Test>('/tests', test)
    return response.data
  },

  /**
   * Update an existing test configuration
   */
  updateTest: async (testId: number, test: Partial<Test>): Promise<Test> => {
    const response = await apiClient.put<Test>(`/tests/${testId}`, test)
    return response.data
  },

  /**
   * Delete a test configuration
   */
  deleteTest: async (testId: number): Promise<void> => {
    await apiClient.delete(`/tests/${testId}`)
  },

  /**
   * Assign test to classes
   */
  assignTestToClasses: async (testId: number, classIds: number[]): Promise<void> => {
    await apiClient.post(`/tests/${testId}/assign`, { classIds })
  },

  /**
   * Get tests assigned to a specific class
   */
  getTestsForClass: async (classId: number): Promise<Test[]> => {
    const response = await apiClient.get<Test[]>(`/tests/class/${classId}`)
    return response.data
  },

  /**
   * Get all tests created by the current teacher
   */
  getAllTests: async (): Promise<Test[]> => {
    const response = await apiClient.get<Test[]>('/tests')
    return response.data
  },
}
