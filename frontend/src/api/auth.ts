import apiClient from './client'

/**
 * Google OAuth request interface
 */
export interface GoogleAuthRequest {
  code: string
  redirectUri?: string
}

/**
 * Google OAuth response interface
 */
export interface GoogleAuthResponse {
  token: string
  user: {
    id: number
    email: string
    name: string
    picture?: string
  }
}

/**
 * Authentication API service
 */
export const authApi = {
  /**
   * Google OAuth callback - exchange authorization code for JWT token
   */
  googleCallback: async (request: GoogleAuthRequest): Promise<GoogleAuthResponse> => {
    const response = await apiClient.post<GoogleAuthResponse>('/auth/google/callback', request)
    return response.data
  },

  /**
   * Logout current user
   */
  logout: async (): Promise<void> => {
    await apiClient.post('/auth/logout')
  },
}
