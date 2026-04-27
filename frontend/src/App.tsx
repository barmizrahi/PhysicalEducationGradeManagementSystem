import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AuthProvider } from './contexts/AuthContext'

// Placeholder components - will be implemented in later tasks
const Login = () => <div>Login Page</div>
const Dashboard = () => <div>Dashboard</div>
const StudentImport = () => <div>Student Import</div>
const StudentList = () => <div>Student List</div>
const TestManagement = () => <div>Test Management</div>
const GradeEntry = () => <div>Grade Entry</div>
const Export = () => <div>Export</div>

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
})

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <Router>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/students/import" element={<StudentImport />} />
            <Route path="/students" element={<StudentList />} />
            <Route path="/tests" element={<TestManagement />} />
            <Route path="/grades" element={<GradeEntry />} />
            <Route path="/export" element={<Export />} />
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </Router>
      </AuthProvider>
    </QueryClientProvider>
  )
}

export default App
