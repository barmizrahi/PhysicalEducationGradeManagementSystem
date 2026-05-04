import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useGoogleLogin } from '@react-oauth/google'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../contexts/AuthContext'
import { Button } from './ui/Button'
import { ErrorMessage } from './ui/ErrorMessage'

/**
 * Login component for teacher authentication via Google OAuth
 * Provides Google sign-in with mobile-responsive layout
 * 
 * Features:
 * - Google OAuth sign-in button
 * - Error message display in Hebrew
 * - Mobile-responsive layout (min 375px width)
 * - Automatic redirect after successful login
 */
export const Login: React.FC = () => {
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  
  const { googleLogin } = useAuth()
  const navigate = useNavigate()
  const { t } = useTranslation()

  const login = useGoogleLogin({
    onSuccess: async (codeResponse) => {
      setLoading(true)
      setError(null)
      
      try {
        // Send authorization code to backend
        await googleLogin(codeResponse.code)
        // Redirect to dashboard after successful login
        navigate('/dashboard')
      } catch (err: any) {
        console.error('Google login error:', err)
        const errorMessage = err.response?.data?.error || t('auth.googleAuthError')
        setError(errorMessage)
      } finally {
        setLoading(false)
      }
    },
    onError: (error) => {
      console.error('Google OAuth error:', error)
      setError(t('auth.googleAuthError'))
    },
    flow: 'auth-code',
  })

  const handleGoogleLogin = () => {
    setError(null)
    login()
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4 py-12">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-lg shadow-md p-8">
          <h1 className="text-2xl font-bold text-center text-text-primary mb-2">
            {t('auth.loginTitle')}
          </h1>
          
          <h2 className="text-lg text-center text-text-secondary mb-8">
            {t('auth.loginSubtitle')}
          </h2>
          
          {error && (
            <div className="mb-6">
              <ErrorMessage 
                message={error} 
                title={t('auth.loginError')}
                onRetry={() => setError(null)}
              />
            </div>
          )}
          
          <div className="space-y-4">
            <Button
              onClick={handleGoogleLogin}
              variant="primary"
              fullWidth
              loading={loading}
              disabled={loading}
            >
              {loading ? t('auth.loggingIn') : t('auth.loginWithGoogle')}
            </Button>
            
            <div className="text-sm text-center text-text-secondary mt-4">
              {t('auth.loginSubtitle')}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
