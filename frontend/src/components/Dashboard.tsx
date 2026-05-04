import React from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { useAuth } from '../contexts/AuthContext'
import { Button } from './ui/Button'

/**
 * Dashboard component - Main landing page after login
 * Provides navigation to all major features
 */
export const Dashboard: React.FC = () => {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const { t } = useTranslation()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const navigationCards = [
    {
      title: 'טעינת קובץ תלמידים',
      description: 'טעינת קובץ תלמידים מקובץ אקסל',
      icon: '📥',
      path: '/students/import',
      color: 'bg-blue-50 hover:bg-blue-100',
    },
    {
      title: 'רשימת תלמידים',
      description: 'צפיה וניהול רשימות תלמידים',
      icon: '👥',
      path: '/students',
      color: 'bg-green-50 hover:bg-green-100',
    },
    {
      title: 'ניהול מבחנים',
      description: 'ליצור ולנהל מבחנים',
      icon: '📝',
      path: '/tests',
      color: 'bg-purple-50 hover:bg-purple-100',
    },
    {
      title: 'ציונים',
      description: 'מתן ציונים לתלמידים',
      icon: '✏️',
      path: '/grades',
      color: 'bg-yellow-50 hover:bg-yellow-100',
    },
    {
      title: 'ייצוא ציונים',
      description: 'ייצוא ציונים לקובץ אקסל',
      icon: '📤',
      path: '/export',
      color: 'bg-red-50 hover:bg-red-100',
    },
  ]

  return (
    <div className="min-h-screen bg-bg-secondary">
      {/* Header */}
      <header className="bg-white shadow-sm">
        <div className="container mx-auto px-4 py-4">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold text-text-primary">
                {t('auth.loginTitle')}
              </h1>
              <p className="text-sm text-text-secondary mt-1">
                {t('dashboard.welcome', { name: user?.name })}
              </p>
            </div>
            <Button variant="secondary" onClick={handleLogout}>
              {t('auth.logout')}
            </Button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="container mx-auto px-4 py-8">
        <div className="mb-8">
          <h2 className="text-xl font-semibold text-text-primary mb-2">
            {t('dashboard.quickActions')}
          </h2>
          <p className="text-text-secondary">
            בחר פעולה כדי להתחיל
          </p>
        </div>

        {/* Navigation Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {navigationCards.map((card) => (
            <button
              key={card.path}
              onClick={() => navigate(card.path)}
              className={`${card.color} rounded-lg p-6 text-left transition-colors border border-border-color hover:shadow-md focus:outline-none focus:ring-2 focus:ring-primary-color focus:ring-offset-2`}
            >
              <div className="text-4xl mb-3">{card.icon}</div>
              <h3 className="text-lg font-semibold text-text-primary mb-2">
                {card.title}
              </h3>
              <p className="text-sm text-text-secondary">{card.description}</p>
            </button>
          ))}
        </div>

        {/* Info Section */}
        <div className="mt-12 bg-white rounded-lg shadow-sm p-6">
          <h3 className="text-lg font-semibold text-text-primary mb-4">
            מדריך התחלה מהירה
          </h3>
          <ol className="space-y-3 text-text-secondary">
            <li className="flex items-start">
              <span className="font-semibold text-primary-color mr-2">1.</span>
              <span>ייבא את התלמידים שלך מקובץ Excel</span>
            </li>
            <li className="flex items-start">
              <span className="font-semibold text-primary-color mr-2">2.</span>
              <span>צור מבחנים והגדר שיטות חישוב</span>
            </li>
            <li className="flex items-start">
              <span className="font-semibold text-primary-color mr-2">3.</span>
              <span>הזן ציונים עבור התלמידים שלך</span>
            </li>
            <li className="flex items-start">
              <span className="font-semibold text-primary-color mr-2">4.</span>
              <span>ייצא תוצאות ל-Excel עבור משרד החינוך</span>
            </li>
          </ol>
        </div>
      </main>
    </div>
  )
}
