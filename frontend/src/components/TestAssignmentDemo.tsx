import React, { useState } from 'react';
import { TestAssignment } from './TestAssignment';
import { Button } from './ui';

/**
 * Demo component for TestAssignment
 * Demonstrates the test assignment interface with mock data
 */
export const TestAssignmentDemo: React.FC = () => {
  const [showAssignment, setShowAssignment] = useState(false);
  const [assignmentResult, setAssignmentResult] = useState<string | null>(null);

  const handleAssignmentComplete = () => {
    setAssignmentResult('Test assigned successfully!');
    setShowAssignment(false);
  };

  const handleCancel = () => {
    setAssignmentResult('Assignment cancelled');
    setShowAssignment(false);
  };

  const handleReset = () => {
    setShowAssignment(false);
    setAssignmentResult(null);
  };

  return (
    <div className="min-h-screen bg-gray-100 p-4">
      <div className="max-w-4xl mx-auto">
        <div className="bg-white rounded-lg shadow-md p-6 mb-4">
          <h1 className="text-3xl font-bold text-text-primary mb-2">
            TestAssignment Component Demo
          </h1>
          <p className="text-text-secondary mb-4">
            This demo shows the test assignment interface for assigning tests to classes.
          </p>

          {!showAssignment && (
            <div className="space-y-4">
              <div className="p-4 bg-blue-50 border border-blue-200 rounded-md">
                <h2 className="text-lg font-semibold text-blue-900 mb-2">
                  Demo Features
                </h2>
                <ul className="list-disc list-inside space-y-1 text-sm text-blue-800">
                  <li>Two assignment modes: Grade Level and Individual Classes</li>
                  <li>Grade level selection assigns to all classes in that grade</li>
                  <li>Individual class selection with checkboxes</li>
                  <li>"Select All" / "Deselect All" per grade level</li>
                  <li>Visual feedback for selected classes</li>
                  <li>Form validation and error handling</li>
                  <li>Mobile-responsive layout</li>
                </ul>
              </div>

              {assignmentResult && (
                <div className={`p-4 rounded-md ${
                  assignmentResult.includes('successfully')
                    ? 'bg-green-50 border border-green-200'
                    : 'bg-yellow-50 border border-yellow-200'
                }`}>
                  <p className={`text-sm font-medium ${
                    assignmentResult.includes('successfully')
                      ? 'text-green-800'
                      : 'text-yellow-800'
                  }`}>
                    {assignmentResult}
                  </p>
                </div>
              )}

              <div className="flex gap-3">
                <Button
                  variant="primary"
                  size="md"
                  onClick={() => setShowAssignment(true)}
                  fullWidth
                >
                  Open Test Assignment
                </Button>
                {assignmentResult && (
                  <Button
                    variant="secondary"
                    size="md"
                    onClick={handleReset}
                    fullWidth
                  >
                    Reset Demo
                  </Button>
                )}
              </div>
            </div>
          )}
        </div>

        {showAssignment && (
          <TestAssignment
            testId={1}
            testName="Push-ups Test (RATIO, COUNT, max: 50)"
            onAssignmentComplete={handleAssignmentComplete}
            onCancel={handleCancel}
          />
        )}

        {/* Instructions */}
        {showAssignment && (
          <div className="mt-4 bg-white rounded-lg shadow-md p-6">
            <h2 className="text-xl font-bold text-text-primary mb-3">
              How to Use
            </h2>
            <div className="space-y-3 text-sm text-text-secondary">
              <div>
                <h3 className="font-semibold text-text-primary mb-1">
                  Grade Level Assignment:
                </h3>
                <ol className="list-decimal list-inside space-y-1 ml-2">
                  <li>Keep "By Grade Level" mode selected (default)</li>
                  <li>Select a grade level from the dropdown (י, יא, or יב)</li>
                  <li>Review the classes that will be assigned</li>
                  <li>Click "Assign Test" to assign to all classes in that grade</li>
                </ol>
              </div>

              <div>
                <h3 className="font-semibold text-text-primary mb-1">
                  Individual Class Assignment:
                </h3>
                <ol className="list-decimal list-inside space-y-1 ml-2">
                  <li>Click "By Individual Classes" button</li>
                  <li>Check the boxes for classes you want to assign</li>
                  <li>Use "Select All" to quickly select all classes in a grade</li>
                  <li>Review the count of selected classes</li>
                  <li>Click "Assign Test" to assign to selected classes</li>
                </ol>
              </div>

              <div className="p-3 bg-yellow-50 border border-yellow-200 rounded-md">
                <p className="text-yellow-800">
                  <span className="font-semibold">Note:</span> This is a demo with mock data.
                  In the real application, classes are loaded from the backend API based on
                  the authenticated teacher's classes.
                </p>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
