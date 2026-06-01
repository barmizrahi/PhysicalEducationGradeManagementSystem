import React from 'react'
import { useNavigate } from 'react-router-dom'
import { StudentList } from '../components/StudentList'
import { Button } from '../components/ui/Button'

export const StudentListPage: React.FC = () => {
  const navigate = useNavigate()

  return (
    <div className="min-h-screen bg-bg-secondary">
      {/* Header */}
      <header className="bg-white shadow-sm">
        <div className="container mx-auto px-4 py-4">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold text-text-primary">
                רשימת תלמידים
              </h1>
              <p className="text-sm text-text-secondary mt-1">
                צפייה וניהול תלמידים
              </p>
            </div>
            <div className="flex gap-2">
              <Button variant="primary" onClick={() => navigate('/students/import')}>
                ייבוא תלמידים
              </Button>
              <Button variant="secondary" onClick={() => navigate('/dashboard')}>
                חזרה ללוח הבקרה
              </Button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container mx-auto px-4 py-8">
        <StudentList />
      </main>
    </div>
  )
}
