import React from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { StudentImport } from '../components/StudentImport'
import { ImportResult } from '../types'
import { Button } from '../components/ui/Button'

export const StudentImportPage: React.FC = () => {
  const navigate = useNavigate()
  const { t } = useTranslation()

  const handleImportComplete = (result: ImportResult) => {
    console.log('Import completed:', result)
    // Optionally navigate to student list after successful import
    if (result.studentsCreated > 0 || result.studentsUpdated > 0) {
      setTimeout(() => {
        navigate('/students')
      }, 2000)
    }
  }

  return (
    <div className="min-h-screen bg-bg-secondary">
      {/* Header */}
      <header className="bg-white shadow-sm">
        <div className="container mx-auto px-4 py-4">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold text-text-primary">
                {t('studentImport.title')}
              </h1>
              <p className="text-sm text-text-secondary mt-1">
                {t('studentImport.subtitle')}
              </p>
            </div>
            <Button variant="secondary" onClick={() => navigate('/dashboard')}>
              חזרה ללוח הבקרה
            </Button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container mx-auto px-4 py-8">
        <StudentImport onImportComplete={handleImportComplete} />
      </main>
    </div>
  )
}
