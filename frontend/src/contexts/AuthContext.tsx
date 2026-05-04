import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import { authApi } from '../api'
import apiClient from '../api/client'

interface User {
  id: number
  email: string
  name: string
  picture?: string
}

interface AuthContextType {
  user: User | null
  token: string | null
  googleLogin: (authorizationCode: string) => Promise<void>
  logout: () => void
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

interface AuthProviderProps {
  children: ReactNode
}

export const AuthProvider = ({ children }: AuthProviderProps) => {
  const [user, setUser] = useState<User | null>(null)
  const [token, setToken] = useState<string | null>(null)

  useEffect(() => {
    // Load token from localStorage on mount
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    
    if (storedToken && storedUser && storedUser !== 'undefined' && storedUser !== 'null') {
      try {
        setToken(storedToken)
        setUser(JSON.parse(storedUser))
        
        // Set default authorization header
        apiClient.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`
      } catch (error) {
        // If parsing fails, clear invalid data
        console.error('Failed to parse stored user data:', error)
        localStorage.removeItem('token')
        localStorage.removeItem('user')
      }
    }
  }, [])

  const googleLogin = async (authorizationCode: string) => {
    try {
      // Send the frontend redirect URI (where Google redirected after auth)
      const redirectUri = window.location.origin
      const response = await authApi.googleCallback({ 
        code: authorizationCode,
        redirectUri: redirectUri
      })
      
      // Backend returns: { token, user: { id, email, name, picture } }
      const newToken = response.token
      const newUser: User = {
        id: response.user.id,
        email: response.user.email,
        name: response.user.name,
        picture: response.user.picture,
      }
      
      setToken(newToken)
      setUser(newUser)
      
      // Store in localStorage
      localStorage.setItem('token', newToken)
      localStorage.setItem('user', JSON.stringify(newUser))
      
      // Set default authorization header
      apiClient.defaults.headers.common['Authorization'] = `Bearer ${newToken}`
    } catch (error) {
      console.error('Google login failed:', error)
      throw error
    }
  }

  const logout = () => {
    setToken(null)
    setUser(null)
    
    // Clear localStorage
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    
    // Remove authorization header
    delete apiClient.defaults.headers.common['Authorization']
  }

  const value = {
    user,
    token,
    googleLogin,
    logout,
    isAuthenticated: !!token && !!user,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
