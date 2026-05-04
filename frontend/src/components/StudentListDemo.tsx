import React from 'react';
import { StudentList } from './StudentList';

/**
 * StudentListDemo Component
 * 
 * Demonstrates the StudentList component functionality.
 * This component can be used for manual testing and verification.
 * 
 * To use this demo:
 * 1. Import this component in your App.tsx or a route
 * 2. Ensure you have students imported in the system
 * 3. The component will automatically fetch and display students
 * 
 * Features demonstrated:
 * - Grade level selector (י, יא, יב)
 * - Class selector within selected grade level
 * - Student list display with name, ID, grade, and class
 * - Mobile-responsive layout
 * - Loading and error states
 * - Empty state handling
 */
export const StudentListDemo: React.FC = () => {
  return (
    <div className="container" style={{ maxWidth: '1200px', margin: '0 auto', padding: '20px' }}>
      <div className="mb-4">
        <h1 className="text-lg font-semibold text-text-primary mb-2">
          StudentList Component Demo
        </h1>
        <p className="text-sm text-text-secondary mb-4">
          This demo showcases the StudentList component with grade level and class filtering.
        </p>
        
        <div className="bg-bg-tertiary p-4 rounded-md mb-4">
          <h2 className="font-semibold mb-2">Features:</h2>
          <ul className="text-sm text-text-secondary space-y-1">
            <li>✓ Display students grouped by grade level and class (Requirements 2.1, 2.2)</li>
            <li>✓ Grade level selector with auto-selection (Requirement 2.3)</li>
            <li>✓ Class selector within grade level (Requirement 2.4)</li>
            <li>✓ Student list display with name, ID, grade, and class</li>
            <li>✓ Mobile-responsive layout with horizontal scroll</li>
            <li>✓ Loading spinner during data fetch</li>
            <li>✓ Error message display on fetch failure</li>
            <li>✓ Empty state when no students exist</li>
            <li>✓ Student count display</li>
            <li>✓ N/A display for missing student IDs</li>
          </ul>
        </div>

        <div className="bg-white p-4 rounded-md border border-border-color mb-4">
          <h2 className="font-semibold mb-2">Testing Instructions:</h2>
          <ol className="text-sm text-text-secondary space-y-2">
            <li>1. <strong>Import Students:</strong> Use the StudentImport component to import students from an Excel file</li>
            <li>2. <strong>Select Grade Level:</strong> Choose a grade level from the dropdown (י, יא, or יב)</li>
            <li>3. <strong>Select Class:</strong> Choose a class from the dropdown (automatically filtered by grade level)</li>
            <li>4. <strong>View Students:</strong> The table will display all students in the selected class</li>
            <li>5. <strong>Test Mobile:</strong> Resize your browser to mobile width (375px+) to test responsive layout</li>
          </ol>
        </div>
      </div>

      {/* StudentList Component */}
      <div className="bg-white rounded-md border border-border-color">
        <StudentList />
      </div>

      {/* Additional Info */}
      <div className="mt-4 p-4 bg-bg-secondary rounded-md">
        <h2 className="font-semibold mb-2">API Integration:</h2>
        <p className="text-sm text-text-secondary mb-2">
          The StudentList component uses the following API endpoint:
        </p>
        <code className="text-sm bg-white p-2 rounded border border-border-color block">
          GET /api/students/by-grade-and-class
        </code>
        <p className="text-sm text-text-secondary mt-2">
          Expected response format:
        </p>
        <pre className="text-xs bg-white p-2 rounded border border-border-color mt-2 overflow-x-auto">
{`{
  "י": {
    "Class A": [
      {
        "id": 1,
        "name": "Student Name",
        "studentId": "S001",
        "gradeLevel": "י",
        "classId": 1,
        "className": "Class A",
        "createdAt": "2024-01-01T00:00:00Z",
        "updatedAt": "2024-01-01T00:00:00Z"
      }
    ]
  }
}`}
        </pre>
      </div>
    </div>
  );
};
