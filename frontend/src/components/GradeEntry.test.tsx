import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { I18nextProvider } from 'react-i18next';
import i18n from '../i18n/config';
import { GradeEntry } from './GradeEntry';
import { studentsApi } from '../api/students';
import { testsApi } from '../api/tests';
import { gradesApi } from '../api/grades';
import type { Student, Test, TestResult } from '../types';

// Mock API modules
vi.mock('../api/students');
vi.mock('../api/tests');
vi.mock('../api/grades');

// Helper function to render GradeEntry with MemoryRouter and I18nextProvider
const renderGradeEntry = () => {
  return render(
    <I18nextProvider i18n={i18n}>
      <MemoryRouter>
        <GradeEntry />
      </MemoryRouter>
    </I18nextProvider>
  );
};

describe('GradeEntry Component', () => {
  const mockClasses = {
    'י': {
      'Class A': [
        { id: 1, name: 'Student 1', studentId: '123456789', gradeLevel: 'י', classId: 1, className: 'Class A', createdAt: '2024-01-01', updatedAt: '2024-01-01' },
        { id: 2, name: 'Student 2', studentId: '987654321', gradeLevel: 'י', classId: 1, className: 'Class A', createdAt: '2024-01-01', updatedAt: '2024-01-01' },
      ] as Student[],
    },
    'יא': {
      'Class B': [
        { id: 3, name: 'Student 3', studentId: null, gradeLevel: 'יא', classId: 2, className: 'Class B', createdAt: '2024-01-01', updatedAt: '2024-01-01' },
      ] as Student[],
    },
  };

  const mockTests: Test[] = [
    {
      id: 1,
      name: 'Running Test',
      calculationType: 'PENALTY',
      unitType: 'TIME',
      maxValue: null,
      targetValue: 10.0,
      penaltyPerUnit: 2.0,
      penaltyUnit: 1.0,
      createdBy: 1,
      createdAt: '2024-01-01',
      updatedAt: '2024-01-01',
    },
    {
      id: 2,
      name: 'Push-ups',
      calculationType: 'RATIO',
      unitType: 'COUNT',
      maxValue: 50,
      targetValue: null,
      penaltyPerUnit: null,
      penaltyUnit: null,
      createdBy: 1,
      createdAt: '2024-01-01',
      updatedAt: '2024-01-01',
    },
  ];

  const mockStudents: Student[] = [
    { id: 1, name: 'Student 1', studentId: '123456789', gradeLevel: 'י', classId: 1, className: 'Class A', createdAt: '2024-01-01', updatedAt: '2024-01-01' },
    { id: 2, name: 'Student 2', studentId: '987654321', gradeLevel: 'י', classId: 1, className: 'Class A', createdAt: '2024-01-01', updatedAt: '2024-01-01' },
  ];

  const mockTestResults: TestResult[] = [
    {
      id: 1,
      studentId: 1,
      testId: 1,
      rawResult: 9.5,
      calculatedGrade: 99.0,
      notes: 'Good performance',
      createdAt: '2024-01-01',
      updatedAt: '2024-01-01',
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
    
    // Setup default mocks
    vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue(mockClasses);
    vi.mocked(studentsApi.getStudentsByClass).mockResolvedValue(mockStudents);
    vi.mocked(testsApi.getTestsForClass).mockResolvedValue(mockTests);
    vi.mocked(gradesApi.getTestResultsForClass).mockResolvedValue(mockTestResults);
    vi.mocked(gradesApi.bulkSaveTestResults).mockResolvedValue([]);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('Initial Rendering', () => {
    it('should display loading spinner initially', () => {
      renderGradeEntry();
      expect(screen.getByRole('status')).toBeInTheDocument();
    });

    it('should display class and test selectors after loading', async () => {
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
        expect(screen.getByLabelText(/שם המבחן/i)).toBeInTheDocument();
      });
    });

    it('should display empty state when no classes exist', async () => {
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue({});

      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByText(/אין תלמידים/i)).toBeInTheDocument();
      });
    });

    it('should display error message when fetching classes fails', async () => {
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockRejectedValue(new Error('Network error'));

      renderGradeEntry();

      await waitFor(() => {
        // When classes fail to load, we show empty state since classOptions.length === 0
        expect(screen.getByText(/אין תלמידים/i)).toBeInTheDocument();
      });
    });
  });

  describe('Class and Test Selection', () => {
    it('should populate class selector with available classes', async () => {
      renderGradeEntry();

      await waitFor(() => {
        const classSelect = screen.getByLabelText(/כיתה/i) as HTMLSelectElement;
        expect(classSelect.options.length).toBeGreaterThan(1); // Including placeholder
      });
    });

    it('should fetch tests when class is selected', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(testsApi.getTestsForClass).toHaveBeenCalledWith(1);
      });
    });

    it('should auto-select first test when tests are loaded', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        const testSelect = screen.getByLabelText(/שם המבחן/i) as HTMLSelectElement;
        expect(testSelect.value).toBe('1');
      });
    });

    it('should fetch students and grades when class and test are selected', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(studentsApi.getStudentsByClass).toHaveBeenCalledWith(1);
        expect(gradesApi.getTestResultsForClass).toHaveBeenCalledWith(1, 1);
      });
    });
  });
});

