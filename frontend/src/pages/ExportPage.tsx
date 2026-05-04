import React from 'react'
import { useNavigate } from 'react-router-dom'
import { ExportForm } from '../components/ExportForm'
import { Button } from '../components/ui/Button'

export const ExportPage: React.FC = () => {
  const navigate = useNavigate()

  return (
    <div className="min-h-screen bg-bg-secondary">
      {/* Header */}
      <header className="bg-white shadow-sm">
        <div className="container mx-auto px-4 py-4">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold text-text-primary">
                Export Results
              </h1>
              <p className="text-sm text-text-secondary mt-1">
                Export student grades to Excel
              </p>
            </div>
            <Button variant="secondary" onClick={() => navigate('/dashboard')}>
              Back to Dashboard
            </Button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container mx-auto px-4 py-8">
        <ExportForm />
      </main>
    </div>
  )
}
