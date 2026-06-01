import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { TestForm } from '../components/TestForm'
import { Button, LoadingSpinner, ErrorMessage } from '../components/ui'
import { testsApi } from '../api/tests'
import type { Test } from '../types'

export const TestManagementPage: React.FC = () => {
  const navigate = useNavigate()
  const { t } = useTranslation()
  const [loading, setLoading] = useState(false)
  const [loadingTests, setLoadingTests] = useState(true)
  const [error, setError] = useState<string>('')
  const [tests, setTests] = useState<Test[]>([])
  const [showForm, setShowForm] = useState(false)
  const [editingTest, setEditingTest] = useState<Test | null>(null)
  const [deleteConfirm, setDeleteConfirm] = useState<number | null>(null)

  // Fetch all tests on mount
  useEffect(() => {
    fetchTests()
  }, [])

  const fetchTests = async () => {
    try {
      setLoadingTests(true)
      const allTests = await testsApi.getAllTests()
      setTests(allTests)
    } catch (err: any) {
      console.error('Error fetching tests:', err)
    } finally {
      setLoadingTests(false)
    }
  }

  const handleSubmit = async (testData: Omit<Test, 'id' | 'createdBy' | 'createdAt' | 'updatedAt'>) => {
    try {
      setLoading(true)
      setError('')
      
      if (editingTest) {
        // Update existing test
        await testsApi.updateTest(editingTest.id, testData)
        setShowForm(false)
        setEditingTest(null)
        await fetchTests()
      } else {
        // Create new test
        const createdTest = await testsApi.createTest(testData)
        // Navigate to test assignment page with the new test ID
        navigate(`/test-assignment/${createdTest.id}`)
      }
    } catch (err: any) {
      setError(err.response?.data?.message || t('errors.generic'))
    } finally {
      setLoading(false)
    }
  }

  const handleEdit = (test: Test) => {
    setEditingTest(test)
    setShowForm(true)
  }

  const handleDelete = async (testId: number) => {
    try {
      setLoading(true)
      await testsApi.deleteTest(testId)
      setDeleteConfirm(null)
      await fetchTests()
    } catch (err: any) {
      setError(err.response?.data?.error || 'שגיאה במחיקת מבחן')
    } finally {
      setLoading(false)
    }
  }

  const handleCancelForm = () => {
    setShowForm(false)
    setEditingTest(null)
    setError('')
  }

  const formatCalculationType = (type: string) => {
    return type === 'RATIO' ? 'יחס (יותר = טוב יותר)' : 'קנס (פחות = טוב יותר)'
  }

  const formatUnitType = (type: string) => {
    return type === 'TIME' ? 'זמן' : 'ספירה'
  }

  return (
    <div className="min-h-screen bg-bg-secondary">
      {/* Header */}
      <header className="bg-white shadow-sm">
        <div className="container mx-auto px-4 py-4">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold text-text-primary">
                {t('tests.title')}
              </h1>
              <p className="text-sm text-text-secondary mt-1">
                {showForm ? t('tests.addTest') : 'צפייה וניהול מבחנים'}
              </p>
            </div>
            <div className="flex gap-2">
              {!showForm && (
                <Button variant="primary" onClick={() => setShowForm(true)}>
                  {t('tests.addTest')}
                </Button>
              )}
              <Button variant="secondary" onClick={() => navigate('/dashboard')}>
                {t('common.back')}
              </Button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container mx-auto px-4 py-8">
        {error && (
          <div className="max-w-2xl mx-auto mb-4">
            <ErrorMessage message={error} />
          </div>
        )}
        
        {showForm ? (
          /* Test Form */
          <div className="max-w-2xl mx-auto bg-white rounded-lg shadow-sm p-6">
            <TestForm 
              onSubmit={handleSubmit}
              onCancel={handleCancelForm}
              loading={loading}
              error={error}
              initialTest={editingTest || undefined}
            />
          </div>
        ) : (
          /* Tests List */
          <div className="bg-white rounded-lg shadow-sm">
            {loadingTests ? (
              <div className="p-8 flex justify-center">
                <LoadingSpinner size="md" />
              </div>
            ) : tests.length === 0 ? (
              <div className="p-8 text-center text-text-secondary">
                <p className="text-lg mb-2">{t('tests.noTests')}</p>
                <p className="text-sm mb-4">צור מבחן ראשון כדי להתחיל</p>
                <Button variant="primary" onClick={() => setShowForm(true)}>
                  {t('tests.addTest')}
                </Button>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-bg-secondary border-b border-border-color">
                    <tr>
                      <th className="px-6 py-3 text-right text-sm font-semibold text-text-primary">
                        שם המבחן
                      </th>
                      <th className="px-6 py-3 text-right text-sm font-semibold text-text-primary">
                        סוג חישוב
                      </th>
                      <th className="px-6 py-3 text-right text-sm font-semibold text-text-primary">
                        סוג יחידה
                      </th>
                      <th className="px-6 py-3 text-right text-sm font-semibold text-text-primary">
                        ערכים
                      </th>
                      <th className="px-6 py-3 text-right text-sm font-semibold text-text-primary">
                        תאריך יצירה
                      </th>
                      <th className="px-6 py-3 text-right text-sm font-semibold text-text-primary">
                        פעולות
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-border-color">
                    {tests.map((test) => (
                      <tr key={test.id} className="hover:bg-bg-secondary">
                        <td className="px-6 py-4 text-sm text-text-primary font-medium">
                          {test.name}
                        </td>
                        <td className="px-6 py-4 text-sm text-text-secondary">
                          {formatCalculationType(test.calculationType)}
                        </td>
                        <td className="px-6 py-4 text-sm text-text-secondary">
                          {formatUnitType(test.unitType)}
                        </td>
                        <td className="px-6 py-4 text-sm text-text-secondary">
                          {test.calculationType === 'RATIO' ? (
                            <span>מקסימום: {test.maxValue}</span>
                          ) : (
                            <span>יעד: {test.targetValue}, קנס: {test.penaltyPerUnit}</span>
                          )}
                        </td>
                        <td className="px-6 py-4 text-sm text-text-secondary">
                          {new Date(test.createdAt).toLocaleDateString('he-IL')}
                        </td>
                        <td className="px-6 py-4 text-sm">
                          <div className="flex gap-2">
                            <Button
                              variant="primary"
                              size="sm"
                              onClick={() => navigate(`/test-assignment/${test.id}`)}
                            >
                              הקצה לכיתות
                            </Button>
                            <Button
                              variant="secondary"
                              size="sm"
                              onClick={() => handleEdit(test)}
                            >
                              ערוך
                            </Button>
                            {deleteConfirm === test.id ? (
                              <div className="flex gap-1">
                                <Button
                                  variant="danger"
                                  size="sm"
                                  onClick={() => handleDelete(test.id)}
                                  disabled={loading}
                                >
                                  אישור
                                </Button>
                                <Button
                                  variant="secondary"
                                  size="sm"
                                  onClick={() => setDeleteConfirm(null)}
                                  disabled={loading}
                                >
                                  ביטול
                                </Button>
                              </div>
                            ) : (
                              <Button
                                variant="danger"
                                size="sm"
                                onClick={() => setDeleteConfirm(test.id)}
                              >
                                מחק
                              </Button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </main>
    </div>
  )
}

