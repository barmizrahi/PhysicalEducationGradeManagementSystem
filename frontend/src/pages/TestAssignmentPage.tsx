import React, { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { TestAssignment } from '../components/TestAssignment'
import { Button, LoadingSpinner, ErrorMessage } from '../components/ui'
import { testsApi } from '../api/tests'
import type { Test } from '../types'

export const TestAssignmentPage: React.FC = () => {
  const navigate = useNavigate()
  const { testId } = useParams<{ testId: string }>()
  const { t } = useTranslation()
  const [test, setTest] = useState<Test | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string>('')

  useEffect(() => {
    const fetchTest = async () => {
      if (!testId) {
        setError('Test ID is missing')
        setLoading(false)
        return
      }

      try {
        setLoading(true)
        const tests = await testsApi.getAllTests()
        const foundTest = tests.find(t => t.id === parseInt(testId))
        
        if (!foundTest) {
          setError('Test not found')
        } else {
          setTest(foundTest)
        }
      } catch (err: any) {
        setError(err.response?.data?.message || t('errors.generic'))
      } finally {
        setLoading(false)
      }
    }

    fetchTest()
  }, [testId, t])

  const handleAssignmentComplete = () => {
    navigate('/grades')
  }

  const handleCancel = () => {
    navigate('/dashboard')
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-bg-secondary flex items-center justify-center">
        <LoadingSpinner size="md" />
      </div>
    )
  }

  if (error || !test) {
    return (
      <div className="min-h-screen bg-bg-secondary">
        <header className="bg-white shadow-sm">
          <div className="container mx-auto px-4 py-4">
            <div className="flex justify-between items-center">
              <h1 className="text-2xl font-bold text-text-primary">
                {t('testAssignment.title')}
              </h1>
              <Button variant="secondary" onClick={() => navigate('/dashboard')}>
                {t('common.back')}
              </Button>
            </div>
          </div>
        </header>
        <main className="container mx-auto px-4 py-8">
          <ErrorMessage message={error || 'Test not found'} />
        </main>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-bg-secondary">
      <header className="bg-white shadow-sm">
        <div className="container mx-auto px-4 py-4">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold text-text-primary">
                {t('testAssignment.title')}
              </h1>
              <p className="text-sm text-text-secondary mt-1">
                {test.name}
              </p>
            </div>
            <Button variant="secondary" onClick={handleCancel}>
              {t('common.back')}
            </Button>
          </div>
        </div>
      </header>

      <main className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto bg-white rounded-lg shadow-sm p-6">
          <TestAssignment
            testId={test.id}
            testName={test.name}
            onAssignmentComplete={handleAssignmentComplete}
            onCancel={handleCancel}
          />
        </div>
      </main>
    </div>
  )
}

