import React, { useState } from 'react';
import { Button, Input, Select, Table, ErrorMessage, LoadingSpinner } from './index';
import type { TableColumn } from './Table';

/**
 * Demo component showcasing all shared UI components
 * This file is for development/documentation purposes only
 */

interface Student {
  id: number;
  name: string;
  grade: string;
  score: number;
}

export const ComponentDemo: React.FC = () => {
  const [inputValue, setInputValue] = useState('');
  const [selectValue, setSelectValue] = useState('');
  const [loading, setLoading] = useState(false);
  const [showError, setShowError] = useState(false);

  const gradeOptions = [
    { value: 'י', label: 'י (Grade 10)' },
    { value: 'יא', label: 'יא (Grade 11)' },
    { value: 'יב', label: 'יב (Grade 12)' },
  ];

  const students: Student[] = [
    { id: 1, name: 'דוד כהן', grade: 'י', score: 95 },
    { id: 2, name: 'שרה לוי', grade: 'יא', score: 87 },
    { id: 3, name: 'יוסף מזרחי', grade: 'יב', score: 92 },
  ];

  const columns: TableColumn<Student>[] = [
    { key: 'name', header: 'שם', width: '40%' },
    { key: 'grade', header: 'כיתה', align: 'center', width: '20%' },
    {
      key: 'score',
      header: 'ציון',
      align: 'right',
      width: '40%',
      render: (student) => (
        <span className={student.score >= 90 ? 'text-success-color font-semibold' : ''}>
          {student.score}
        </span>
      ),
    },
  ];

  const handleLoadData = () => {
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
    }, 2000);
  };

  return (
    <div className="container" style={{ padding: '2rem', maxWidth: '800px' }}>
      <h1 style={{ marginBottom: '2rem' }}>UI Components Demo</h1>

      {/* Buttons Section */}
      <section style={{ marginBottom: '2rem' }}>
        <h2 style={{ marginBottom: '1rem' }}>Buttons</h2>
        <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
          <Button variant="primary">Primary</Button>
          <Button variant="secondary">Secondary</Button>
          <Button variant="success">Success</Button>
          <Button variant="danger">Danger</Button>
          <Button loading>Loading</Button>
          <Button disabled>Disabled</Button>
        </div>
        <div style={{ marginTop: '1rem' }}>
          <Button fullWidth>Full Width Button</Button>
        </div>
      </section>

      {/* Input Section */}
      <section style={{ marginBottom: '2rem' }}>
        <h2 style={{ marginBottom: '1rem' }}>Input Fields</h2>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <Input
            label="Student Name"
            placeholder="Enter student name"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            helperText="Enter the full name in Hebrew"
            fullWidth
          />
          <Input
            label="Student ID"
            placeholder="123456789"
            error="This field is required"
            fullWidth
          />
          <Input
            label="Email"
            type="email"
            placeholder="student@example.com"
            disabled
            fullWidth
          />
        </div>
      </section>

      {/* Select Section */}
      <section style={{ marginBottom: '2rem' }}>
        <h2 style={{ marginBottom: '1rem' }}>Select Dropdown</h2>
        <Select
          label="Grade Level"
          options={gradeOptions}
          value={selectValue}
          onChange={(e) => setSelectValue(e.target.value)}
          placeholder="Select grade level"
          helperText="Choose the student's grade level"
          fullWidth
        />
      </section>

      {/* Table Section */}
      <section style={{ marginBottom: '2rem' }}>
        <h2 style={{ marginBottom: '1rem' }}>Table</h2>
        <Table
          columns={columns}
          data={students}
          keyExtractor={(student) => student.id}
          striped
          hoverable
        />
      </section>

      {/* Loading Spinner Section */}
      <section style={{ marginBottom: '2rem' }}>
        <h2 style={{ marginBottom: '1rem' }}>Loading Spinner</h2>
        <div style={{ display: 'flex', gap: '2rem', alignItems: 'center' }}>
          <LoadingSpinner size="sm" />
          <LoadingSpinner size="md" message="Loading..." />
          <LoadingSpinner size="lg" />
        </div>
        <div style={{ marginTop: '1rem' }}>
          <Button onClick={handleLoadData}>
            {loading ? 'Loading...' : 'Load Data'}
          </Button>
          {loading && <LoadingSpinner fullScreen message="Loading data..." />}
        </div>
      </section>

      {/* Error Message Section */}
      <section style={{ marginBottom: '2rem' }}>
        <h2 style={{ marginBottom: '1rem' }}>Error Message</h2>
        <Button onClick={() => setShowError(!showError)} variant="danger">
          {showError ? 'Hide Error' : 'Show Error'}
        </Button>
        {showError && (
          <div style={{ marginTop: '1rem' }}>
            <ErrorMessage
              title="Failed to load students"
              message="Unable to fetch student data from the server. Please check your connection and try again."
              onRetry={() => alert('Retrying...')}
            />
          </div>
        )}
      </section>

      {/* Mobile Responsiveness Note */}
      <section style={{ marginBottom: '2rem' }}>
        <h2 style={{ marginBottom: '1rem' }}>Mobile Responsiveness</h2>
        <div style={{ 
          padding: '1rem', 
          backgroundColor: 'var(--bg-tertiary)', 
          borderRadius: 'var(--radius-md)' 
        }}>
          <p style={{ marginBottom: '0.5rem' }}>
            <strong>✓ Minimum 16px font size</strong> for inputs (prevents iOS zoom)
          </p>
          <p style={{ marginBottom: '0.5rem' }}>
            <strong>✓ Touch-optimized sizing</strong> (44px minimum height)
          </p>
          <p style={{ marginBottom: '0.5rem' }}>
            <strong>✓ Responsive design</strong> (works on 375px+ screens)
          </p>
          <p>
            <strong>✓ Hebrew RTL support</strong> (right-to-left text direction)
          </p>
        </div>
      </section>
    </div>
  );
};
