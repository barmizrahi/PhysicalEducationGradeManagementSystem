import React, { useState } from 'react';
import { TestForm } from './TestForm';
import { Test } from '../types';

/**
 * Demo component showcasing TestForm functionality
 * This component demonstrates:
 * - Creating new tests
 * - Editing existing tests
 * - Form validation
 * - Conditional fields based on calculation type
 * - Mobile-responsive layout
 */
export const TestFormDemo: React.FC = () => {
  const [submittedData, setSubmittedData] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | undefined>(undefined);
  const [mode, setMode] = useState<'create' | 'edit'>('create');

  const sampleTest: Partial<Test> = {
    name: 'Push-ups Test',
    calculationType: 'RATIO',
    unitType: 'COUNT',
    maxValue: 50,
  };

  const handleSubmit = async (testData: Omit<Test, 'id' | 'createdBy' | 'createdAt' | 'updatedAt'>) => {
    setLoading(true);
    setError(undefined);

    // Simulate API call
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // Simulate random error (20% chance)
    if (Math.random() < 0.2) {
      setError('Failed to save test. Please try again.');
      setLoading(false);
      return;
    }

    setSubmittedData(testData);
    setLoading(false);
  };

  const handleCancel = () => {
    setSubmittedData(null);
    setError(undefined);
  };

  return (
    <div className="min-h-screen bg-gray-50 p-4">
      <div className="max-w-2xl mx-auto">
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h1 className="text-2xl font-bold text-text-primary mb-2">
            TestForm Component Demo
          </h1>
          <p className="text-text-secondary mb-4">
            Interactive demonstration of the TestForm component for creating and editing test configurations.
          </p>

          <div className="mb-4 flex gap-2">
            <button
              onClick={() => setMode('create')}
              className={`px-4 py-2 rounded-md ${
                mode === 'create'
                  ? 'bg-primary-color text-white'
                  : 'bg-gray-200 text-gray-700'
              }`}
            >
              Create Mode
            </button>
            <button
              onClick={() => setMode('edit')}
              className={`px-4 py-2 rounded-md ${
                mode === 'edit'
                  ? 'bg-primary-color text-white'
                  : 'bg-gray-200 text-gray-700'
              }`}
            >
              Edit Mode
            </button>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 className="text-xl font-semibold text-text-primary mb-4">
            {mode === 'create' ? 'Create New Test' : 'Edit Existing Test'}
          </h2>
          
          <TestForm
            initialTest={mode === 'edit' ? sampleTest : undefined}
            onSubmit={handleSubmit}
            onCancel={handleCancel}
            loading={loading}
            error={error}
          />
        </div>

        {submittedData && (
          <div className="bg-green-50 border border-success-color rounded-lg p-6">
            <h2 className="text-xl font-semibold text-success-color mb-4">
              ✓ Test Saved Successfully
            </h2>
            <div className="space-y-2 text-sm">
              <div>
                <span className="font-medium">Name:</span> {submittedData.name}
              </div>
              <div>
                <span className="font-medium">Calculation Type:</span> {submittedData.calculationType}
              </div>
              <div>
                <span className="font-medium">Unit Type:</span> {submittedData.unitType}
              </div>
              {submittedData.maxValue !== null && (
                <div>
                  <span className="font-medium">Max Value:</span> {submittedData.maxValue}
                </div>
              )}
              {submittedData.targetValue !== null && (
                <div>
                  <span className="font-medium">Target Value:</span> {submittedData.targetValue}
                </div>
              )}
              {submittedData.penaltyPerUnit !== null && (
                <div>
                  <span className="font-medium">Penalty Per Unit:</span> {submittedData.penaltyPerUnit}
                </div>
              )}
            </div>
          </div>
        )}

        <div className="bg-white rounded-lg shadow-md p-6 mt-6">
          <h2 className="text-xl font-semibold text-text-primary mb-4">
            Component Features
          </h2>
          <ul className="space-y-2 text-sm text-text-secondary">
            <li>✅ Calculation type selector (RATIO/PENALTY)</li>
            <li>✅ Unit type selector (TIME/COUNT)</li>
            <li>✅ Conditional fields based on calculation type</li>
            <li>✅ RATIO: Shows maxValue field</li>
            <li>✅ PENALTY: Shows targetValue and penaltyPerUnit fields</li>
            <li>✅ Form validation with error messages</li>
            <li>✅ Required field validation</li>
            <li>✅ Numeric value validation</li>
            <li>✅ Loading state during submission</li>
            <li>✅ Error display for submission failures</li>
            <li>✅ Mobile-responsive layout (min 375px width)</li>
            <li>✅ Touch-optimized buttons (min 44px height)</li>
            <li>✅ Minimum 16px font size (prevents iOS zoom)</li>
            <li>✅ Hebrew RTL text support</li>
            <li>✅ Accessible form controls with labels</li>
            <li>✅ Create and edit modes</li>
          </ul>
        </div>

        <div className="bg-white rounded-lg shadow-md p-6 mt-6">
          <h2 className="text-xl font-semibold text-text-primary mb-4">
            Validation Rules
          </h2>
          <div className="space-y-4 text-sm">
            <div>
              <h3 className="font-medium text-text-primary mb-2">All Tests:</h3>
              <ul className="list-disc list-inside text-text-secondary space-y-1">
                <li>Test name is required</li>
                <li>Calculation type is required</li>
                <li>Unit type is required</li>
              </ul>
            </div>
            <div>
              <h3 className="font-medium text-text-primary mb-2">RATIO Tests:</h3>
              <ul className="list-disc list-inside text-text-secondary space-y-1">
                <li>Max value is required</li>
                <li>Max value must be a positive number</li>
              </ul>
            </div>
            <div>
              <h3 className="font-medium text-text-primary mb-2">PENALTY Tests:</h3>
              <ul className="list-disc list-inside text-text-secondary space-y-1">
                <li>Target value is required</li>
                <li>Target value must be a non-negative number</li>
                <li>Penalty per unit is required</li>
                <li>Penalty per unit must be a positive number</li>
              </ul>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-md p-6 mt-6">
          <h2 className="text-xl font-semibold text-text-primary mb-4">
            Usage Example
          </h2>
          <pre className="text-xs bg-gray-50 p-4 rounded border border-border-color overflow-x-auto">
{`import { TestForm } from './components';
import { testsApi } from './api';

function CreateTestPage() {
  const [error, setError] = useState<string>();
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (testData) => {
    setLoading(true);
    setError(undefined);
    
    try {
      await testsApi.createTest(testData);
      // Navigate to test list or show success message
    } catch (err) {
      setError('Failed to create test');
    } finally {
      setLoading(false);
    }
  };

  return (
    <TestForm
      onSubmit={handleSubmit}
      onCancel={() => navigate('/tests')}
      loading={loading}
      error={error}
    />
  );
}`}
          </pre>
        </div>
      </div>
    </div>
  );
};
