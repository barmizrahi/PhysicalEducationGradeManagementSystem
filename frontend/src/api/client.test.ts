import { describe, it, expect, beforeEach, vi } from 'vitest'
import apiClient from './client'

// Mock axios to intercept requests
vi.mock('axios')

describe('API Client - JWT Token Management', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  describe('Configuration', () => {
    it('should have correct base URL', () => {
      expect(apiClient.defaults.baseURL).toBe('/api')
    })

    it('should have correct timeout', () => {
      expect(apiClient.defaults.timeout).toBe(10000)
    })

    it('should have correct default headers', () => {
      expect(apiClient.defaults.headers['Content-Type']).toBe('application/json')
    })
  })

  describe('Request Interceptor - Token Injection', () => {
    it('should have request interceptor that adds token from localStorage', () => {
      const mockToken = 'test-jwt-token'
      localStorage.setItem('token', mockToken)

      // Get the request interceptor
      const interceptors = apiClient.interceptors.request as any
      expect(interceptors.handlers).toBeDefined()
      expect(interceptors.handlers.length).toBeGreaterThan(0)

      // Test the interceptor function
      const config = { headers: {} }
      const fulfilled = interceptors.handlers[0].fulfilled
      const result = fulfilled(config)

      expect(result.headers.Authorization).toBe(`Bearer ${mockToken}`)
    })

    it('should not add Authorization header when token does not exist', () => {
      // Get the request interceptor
      const interceptors = apiClient.interceptors.request as any
      const config = { headers: {} }
      const fulfilled = interceptors.handlers[0].fulfilled
      const result = fulfilled(config)

      expect(result.headers.Authorization).toBeUndefined()
    })

    it('should handle empty token string', () => {
      localStorage.setItem('token', '')

      const interceptors = apiClient.interceptors.request as any
      const config = { headers: {} }
      const fulfilled = interceptors.handlers[0].fulfilled
      const result = fulfilled(config)

      expect(result.headers.Authorization).toBeUndefined()
    })
  })

  describe('Response Interceptor - 401 Handling', () => {
    it('should have response interceptor for error handling', () => {
      const interceptors = apiClient.interceptors.response as any
      expect(interceptors.handlers).toBeDefined()
      expect(interceptors.handlers.length).toBeGreaterThan(0)
    })

    it('should clear localStorage and redirect on 401 error', () => {
      const mockToken = 'test-jwt-token'
      const mockUser = JSON.stringify({ id: 1, username: 'teacher1' })
      
      localStorage.setItem('token', mockToken)
      localStorage.setItem('user', mockUser)

      // Mock window.location
      const originalLocation = window.location
      delete (window as any).location
      window.location = { ...originalLocation, href: '' } as Location

      // Get the response interceptor
      const interceptors = apiClient.interceptors.response as any
      const rejected = interceptors.handlers[0].rejected

      // Create a 401 error
      const error = {
        response: {
          status: 401,
        },
      }

      // Call the error handler
      try {
        rejected(error)
      } catch (e) {
        // Expected to throw
      }

      expect(localStorage.getItem('token')).toBeNull()
      expect(localStorage.getItem('user')).toBeNull()
      expect(window.location.href).toBe('/login')

      // Restore window.location
      window.location = originalLocation
    })

    it('should not clear localStorage on non-401 errors', () => {
      const mockToken = 'test-jwt-token'
      const mockUser = JSON.stringify({ id: 1, username: 'teacher1' })
      
      localStorage.setItem('token', mockToken)
      localStorage.setItem('user', mockUser)

      // Get the response interceptor
      const interceptors = apiClient.interceptors.response as any
      const rejected = interceptors.handlers[0].rejected

      // Create a non-401 error
      const error = {
        response: {
          status: 500,
        },
      }

      // Call the error handler
      try {
        rejected(error)
      } catch (e) {
        // Expected to throw
      }

      expect(localStorage.getItem('token')).toBe(mockToken)
      expect(localStorage.getItem('user')).toBe(mockUser)
    })
  })
})

