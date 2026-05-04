import React from 'react';
import { GradeEntry } from './GradeEntry';

/**
 * GradeEntryDemo Component
 * 
 * Demonstrates the GradeEntry component in isolation.
 * This component can be used for:
 * - Visual testing during development
 * - Component documentation
 * - Storybook integration
 * 
 * Note: Requires API mocking or backend to be running for full functionality.
 */
export const GradeEntryDemo: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-50 p-4">
      <div className="max-w-4xl mx-auto">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-text-primary mb-2">
            GradeEntry Component Demo
          </h1>
          <p className="text-text-secondary">
            Core grade entry interface for teachers to enter test results during class.
          </p>
        </div>

        <div className="bg-white rounded-lg shadow-md">
          <GradeEntry />
        </div>

        <div className="mt-6 p-4 bg-blue-50 border border-blue-200 rounded-md">
          <h2 className="text-lg font-semibold text-blue-900 mb-2">Features</h2>
          <ul className="list-disc list-inside text-sm text-blue-800 space-y-1">
            <li>Class and test selector</li>
            <li>Display all students in selected class</li>
            <li>Input fields for raw results (one per student)</li>
            <li>Real-time grade calculation display</li>
            <li>Optional notes field per student</li>
            <li>Time format input (mm:ss) for TIME tests</li>
            <li>Input validation with inline error messages</li>
            <li>Mobile-optimized touch input</li>
            <li>Fast keyboard navigation between fields</li>
            <li>Auto-save functionality</li>
          </ul>
        </div>

        <div className="mt-4 p-4 bg-yellow-50 border border-yellow-200 rounded-md">
          <h2 className="text-lg font-semibold text-yellow-900 mb-2">Usage Instructions</h2>
          <ol className="list-decimal list-inside text-sm text-yellow-800 space-y-1">
            <li>Select a class from the dropdown</li>
            <li>Select a test from the dropdown (auto-populated based on class)</li>
            <li>Enter raw results for each student</li>
            <li>For TIME tests, use mm:ss format (e.g., 10:30)</li>
            <li>For COUNT tests, enter numeric values (e.g., 25)</li>
            <li>Optionally add notes for each student</li>
            <li>Press Enter to navigate between fields</li>
            <li>Click "Save All" or wait for auto-save (3 seconds)</li>
          </ol>
        </div>

        <div className="mt-4 p-4 bg-green-50 border border-green-200 rounded-md">
          <h2 className="text-lg font-semibold text-green-900 mb-2">Keyboard Navigation</h2>
          <ul className="list-disc list-inside text-sm text-green-800 space-y-1">
            <li><kbd className="px-2 py-1 bg-white border border-green-300 rounded">Enter</kbd> in Raw Result field → Moves to Notes field</li>
            <li><kbd className="px-2 py-1 bg-white border border-green-300 rounded">Enter</kbd> in Notes field → Moves to next student's Raw Result</li>
            <li><kbd className="px-2 py-1 bg-white border border-green-300 rounded">Tab</kbd> → Standard tab navigation</li>
          </ul>
        </div>

        <div className="mt-4 p-4 bg-purple-50 border border-purple-200 rounded-md">
          <h2 className="text-lg font-semibold text-purple-900 mb-2">Grade Calculation Examples</h2>
          <div className="text-sm text-purple-800 space-y-2">
            <div>
              <strong>RATIO Calculation:</strong>
              <p className="ml-4">grade = (rawResult / maxValue) × 100</p>
              <p className="ml-4 text-xs">Example: 25 push-ups with max 50 = 50.00 grade</p>
            </div>
            <div>
              <strong>PENALTY Calculation:</strong>
              <p className="ml-4">grade = 100 - ((rawResult - targetValue) × penaltyPerUnit)</p>
              <p className="ml-4 text-xs">Example: 11:00 minutes with target 10:00 and penalty 2.0 = 98.00 grade</p>
            </div>
          </div>
        </div>

        <div className="mt-4 p-4 bg-red-50 border border-red-200 rounded-md">
          <h2 className="text-lg font-semibold text-red-900 mb-2">Validation Rules</h2>
          <ul className="list-disc list-inside text-sm text-red-800 space-y-1">
            <li>TIME format must be mm:ss (e.g., 10:30)</li>
            <li>Seconds must be less than 60</li>
            <li>COUNT format must be numeric (e.g., 25 or 15.5)</li>
            <li>Negative values are not allowed</li>
            <li>Empty values are allowed (student didn't take test)</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default GradeEntryDemo;
