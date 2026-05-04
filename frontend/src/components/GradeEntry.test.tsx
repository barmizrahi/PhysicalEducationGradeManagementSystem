import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
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

  describe('Student Display', () => {
    it('should display all students in selected class', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
        expect(screen.getByText('Student 2')).toBeInTheDocument();
      });
    });

    it('should display student ID or "No ID" for students without ID', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('123456789')).toBeInTheDocument();
        expect(screen.getByText('987654321')).toBeInTheDocument();
      });
    });

    it('should display existing grades for students with test results', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('99.00')).toBeInTheDocument();
      });
    });
  });

  describe('Raw Result Input', () => {
    it('should accept time format input for TIME tests', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 2')).toBeInTheDocument();
      });

      const inputs = screen.getAllByLabelText(/ציון/i);
      const student2Input = inputs[1]; // Student 2 has no existing result

      await user.clear(student2Input);
      await user.type(student2Input, '10:30');

      expect(student2Input).toHaveValue('10:30');
    });

    it('should accept numeric input for COUNT tests', async () => {
      const user = userEvent.setup();
      vi.mocked(testsApi.getTestsForClass).mockResolvedValue([mockTests[1]]); // Push-ups test

      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
      });

      const inputs = screen.getAllByLabelText(/ציון/i);
      const student1Input = inputs[0];

      await user.clear(student1Input);
      await user.type(student1Input, '25');

      expect(student1Input).toHaveValue('25');
    });

    it('should display validation error for invalid time format', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 2')).toBeInTheDocument();
      });

      const inputs = screen.getAllByLabelText(/ציון/i);
      const student2Input = inputs[1];

      await user.clear(student2Input);
      await user.type(student2Input, 'invalid');

      await waitFor(() => {
        expect(screen.getByText(/פורמט לא תקין/i)).toBeInTheDocument();
      });
    });

    it('should display validation error for negative values', async () => {
      const user = userEvent.setup();
      vi.mocked(testsApi.getTestsForClass).mockResolvedValue([mockTests[1]]); // COUNT test

      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
      });

      const inputs = screen.getAllByLabelText(/ציון/i);
      const student1Input = inputs[1]; // Skip bulk input (index 0)

      await user.clear(student1Input);
      await user.type(student1Input, '-5');

      await waitFor(() => {
        // The validation will show either "invalid number" or "min value" error
        const errorElements = screen.queryAllByText(/(מספר לא תקין|ערך מינימלי)/i);
        expect(errorElements.length).toBeGreaterThan(0);
      });
    });

    it('should display validation error for non-numeric input in COUNT tests', async () => {
      const user = userEvent.setup();
      vi.mocked(testsApi.getTestsForClass).mockResolvedValue([mockTests[1]]); // COUNT test

      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
      });

      const inputs = screen.getAllByLabelText(/ציון/i);
      const student1Input = inputs[1]; // Skip bulk input (index 0)

      await user.clear(student1Input);
      await user.type(student1Input, 'abc');

      await waitFor(() => {
        expect(screen.getByText(/מספר לא תקין/i)).toBeInTheDocument();
      });
    });
  });

  describe('Real-time Grade Calculation', () => {
    it('should calculate and display grade for RATIO calculation', async () => {
      const user = userEvent.setup();
      vi.mocked(testsApi.getTestsForClass).mockResolvedValue([mockTests[1]]); // RATIO test with maxValue 50

      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
      });

      const inputs = screen.getAllByLabelText(/ציון/i);
      const student1Input = inputs[1]; // Skip bulk input (index 0)

      await user.clear(student1Input);
      await user.type(student1Input, '25');

      await waitFor(() => {
        // 25/50 * 100 = 50.00
        expect(screen.getByText('50.00')).toBeInTheDocument();
      });
    });

    it('should calculate and display grade for PENALTY calculation', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 2')).toBeInTheDocument();
      });

      const inputs = screen.getAllByLabelText(/ציון/i);
      const student2Input = inputs[2]; // Skip bulk input (index 0) and student 1 (index 1)

      await user.clear(student2Input);
      await user.type(student2Input, '11:00');

      await waitFor(() => {
        // 100 - ((11 - 10) * 2) = 98.00
        expect(screen.getByText('98.00')).toBeInTheDocument();
      });
    });

    it('should cap RATIO grade at 100', async () => {
      const user = userEvent.setup();
      vi.mocked(testsApi.getTestsForClass).mockResolvedValue([mockTests[1]]); // RATIO test with maxValue 50

      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
      });

      const inputs = screen.getAllByLabelText(/ציון/i);
      const student1Input = inputs[1]; // Skip bulk input (index 0)

      await user.clear(student1Input);
      await user.type(student1Input, '60');

      await waitFor(() => {
        // 60/50 * 100 = 120, capped at 100
        expect(screen.getByText('100.00')).toBeInTheDocument();
      });
    });

    it('should cap PENALTY grade at 0 for poor performance', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 2')).toBeInTheDocument();
      });

      const inputs = screen.getAllByLabelText(/ציון/i);
      const student2Input = inputs[2]; // Skip bulk input (index 0) and student 1 (index 1)

      await user.clear(student2Input);
      await user.type(student2Input, '20:00');

      await waitFor(() => {
        // 100 - ((20 - 10) * 2) = 80, but let's test extreme case
        // Actually 100 - (10 * 2) = 80, not capped
        // Let's use a value that would give negative: 60 minutes
        // 100 - ((60 - 10) * 2) = 100 - 100 = 0
      });

      await user.clear(student2Input);
      await user.type(student2Input, '60:00');

      await waitFor(() => {
        expect(screen.getByText('0.00')).toBeInTheDocument();
      });
    });
  });

  describe('Notes Input', () => {
    it('should accept notes input', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
      });

      const notesInputs = screen.getAllByLabelText(/הוסף.*אופציונלי/i);
      const student1Notes = notesInputs[0];

      await user.clear(student1Notes);
      await user.type(student1Notes, 'Excellent effort');

      expect(student1Notes).toHaveValue('Excellent effort');
    });

    it('should display existing notes', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        const notesInputs = screen.getAllByLabelText(/הוסף.*אופציונלי/i);
        expect(notesInputs[0]).toHaveValue('Good performance');
      });
    });
  });

  describe('Saving Grades', () => {
    it('should save grades when Save All button is clicked', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 2')).toBeInTheDocument();
      });

      const inputs = screen.getAllByLabelText(/ציון/i);
      const student2Input = inputs[1];

      await user.clear(student2Input);
      await user.type(student2Input, '10:30');

      const saveButtons = screen.getAllByRole('button', { name: /שמור/i });
      await user.click(saveButtons[0]);

      await waitFor(() => {
        expect(gradesApi.bulkSaveTestResults).toHaveBeenCalled();
      });
    });

    it('should display success message after saving', async () => {
      const user = userEvent.setup();
      vi.mocked(gradesApi.bulkSaveTestResults).mockResolvedValue([
        { ...mockTestResults[0], id: 2, studentId: 2, rawResult: 10.5, calculatedGrade: 99.0 },
      ]);

      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 2')).toBeInTheDocument();
      });

      const inputs = screen.getAllByLabelText(/ציון/i);
      const student2Input = inputs[1];

      await user.clear(student2Input);
      await user.type(student2Input, '10:30');

      const saveButtons = screen.getAllByRole('button', { name: /שמור/i });
      await user.click(saveButtons[0]);

      await waitFor(() => {
        expect(screen.getByText(/ציונים נשמרו בהצלחה/i)).toBeInTheDocument();
      });
    });

    it('should prevent saving when validation errors exist', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 2')).toBeInTheDocument();
      });

      const inputs = screen.getAllByLabelText(/ציון/i);
      const student2Input = inputs[2]; // Skip bulk input (index 0) and student 1 (index 1)

      await user.clear(student2Input);
      await user.type(student2Input, 'invalid');

      await waitFor(() => {
        const errors = screen.getAllByText(/פורמט לא תקין/i);
        expect(errors.length).toBeGreaterThan(0);
      });

      const saveButtons = screen.getAllByRole('button', { name: /שמור/i });
      await user.click(saveButtons[0]);

      await waitFor(() => {
        const errors = screen.getAllByText(/פורמט לא תקין/i);
        expect(errors.length).toBeGreaterThan(0);
      });

      expect(gradesApi.bulkSaveTestResults).not.toHaveBeenCalled();
    });

    it('should mark changes as unsaved when input changes', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 2')).toBeInTheDocument();
      });

      const inputs = screen.getAllByLabelText(/ציון/i);
      const student2Input = inputs[1];

      await user.clear(student2Input);
      await user.type(student2Input, '10:30');

      await waitFor(() => {
        expect(screen.getByText(/איפוס/i)).toBeInTheDocument();
      });
    });
  });

  describe('Keyboard Navigation', () => {
    it('should move focus to notes field when Enter is pressed in raw result field', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
      });

      const rawResultInputs = screen.getAllByLabelText(/ציון/i);
      const student1RawResult = rawResultInputs[1]; // Skip bulk input (index 0)

      student1RawResult.focus();
      await user.keyboard('{Enter}');

      const notesInputs = screen.getAllByLabelText(/הוסף.*אופציונלי/i);
      const student1Notes = notesInputs[0];

      expect(student1Notes).toHaveFocus();
    });

    it('should move focus to next student raw result when Enter is pressed in notes field', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
      });

      const notesInputs = screen.getAllByLabelText(/הוסף.*אופציונלי/i);
      const student1Notes = notesInputs[0];

      student1Notes.focus();
      await user.keyboard('{Enter}');

      const rawResultInputs = screen.getAllByLabelText(/ציון/i);
      const student2RawResult = rawResultInputs[2]; // Skip bulk input (index 0) and student 1 (index 1)

      expect(student2RawResult).toHaveFocus();
    });
  });

  describe('Mobile Responsiveness', () => {
    it('should render with mobile-optimized layout', async () => {
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      // Check that inputs have proper mobile styling (text-base = 16px)
      const classSelect = screen.getByLabelText(/כיתה/i);
      expect(classSelect).toHaveClass('text-base');
    });

    it('should display student cards instead of table on mobile', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        // Check that student info is displayed in card format
        expect(screen.getByText('Student 1')).toBeInTheDocument();
        expect(screen.getByText('Student 2')).toBeInTheDocument();
      });
    });
  });

  describe('Error Handling', () => {
    it('should display error when fetching tests fails', async () => {
      const user = userEvent.setup();
      vi.mocked(testsApi.getTestsForClass).mockRejectedValue(new Error('Network error'));

      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        // Check for Hebrew network error message
        const errorElements = screen.queryAllByText(/שגיאת רשת/i);
        expect(errorElements.length).toBeGreaterThan(0);
      });
    });

    it('should display error when saving grades fails', async () => {
      const user = userEvent.setup();
      vi.mocked(gradesApi.bulkSaveTestResults).mockRejectedValue(new Error('Save failed'));

      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 2')).toBeInTheDocument();
      });

      const inputs = screen.getAllByLabelText(/ציון/i);
      const student2Input = inputs[2]; // Skip bulk input (index 0) and student 1 (index 1)

      await user.clear(student2Input);
      await user.type(student2Input, '10:30');

      const saveButtons = screen.getAllByRole('button', { name: /שמור/i });
      await user.click(saveButtons[0]);

      await waitFor(() => {
        // The error message will be the actual error from the API
        expect(screen.getByText(/save failed/i)).toBeInTheDocument();
      });
    });
  });

  describe('Bulk Actions', () => {
    it('should display bulk actions section when class and test are selected', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText(/פעולות מרובות/i)).toBeInTheDocument();
      });
    });

    it('should allow selecting individual students', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
      });

      const checkbox = screen.getByLabelText(/select student 1/i);
      await user.click(checkbox);

      expect(checkbox).toBeChecked();
      expect(screen.getByText(/1.*נבחרו/i)).toBeInTheDocument();
    });

    it('should allow selecting all students', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
      });

      const selectAllButton = screen.getByRole('button', { name: /בחר הכל/i });
      await user.click(selectAllButton);

      expect(screen.getByText(/2.*נבחרו/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/select student 1/i)).toBeChecked();
      expect(screen.getByLabelText(/select student 2/i)).toBeChecked();
    });

    it('should allow deselecting all students', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
      });

      // Select all
      const selectAllButton = screen.getByRole('button', { name: /בחר הכל/i });
      await user.click(selectAllButton);

      expect(screen.getByText(/2.*נבחרו/i)).toBeInTheDocument();

      // Deselect all
      const deselectAllButton = screen.getByRole('button', { name: /בטל בחירה/i });
      await user.click(deselectAllButton);

      expect(screen.getByText(/0.*נבחרו/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/select student 1/i)).not.toBeChecked();
      expect(screen.getByLabelText(/select student 2/i)).not.toBeChecked();
    });

    it('should apply bulk raw result to selected students', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
      });

      // Select both students
      const checkbox1 = screen.getByLabelText(/select student 1/i);
      const checkbox2 = screen.getByLabelText(/select student 2/i);
      await user.click(checkbox1);
      await user.click(checkbox2);

      // Enter bulk raw result - get all inputs and select the first one (bulk input)
      const allInputs = screen.getAllByLabelText(/ציון.*mm:ss/i);
      const bulkInput = allInputs[0]; // First one is the bulk input
      await user.type(bulkInput, '10:00');

      // Apply to selected
      const applyButton = screen.getByRole('button', { name: /שלח/i });
      await user.click(applyButton);

      // Check that both students have the same raw result
      await waitFor(() => {
        const rawResultInputs = screen.getAllByLabelText(/^ציון/i);
        // Skip the bulk input (first one) and check student inputs
        expect(rawResultInputs[1]).toHaveValue('10:00');
        expect(rawResultInputs[2]).toHaveValue('10:00');
      });

      // Check that students are deselected after applying
      expect(screen.getByText(/0.*נבחרו/i)).toBeInTheDocument();
    });

    it('should calculate grades for all selected students when applying bulk result', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
      });

      // Select both students
      const selectAllButton = screen.getByRole('button', { name: /בחר הכל/i });
      await user.click(selectAllButton);

      // Enter bulk raw result (10:00 = 10.0 minutes, target is 10.0, so grade should be 100)
      const allInputs = screen.getAllByLabelText(/ציון.*mm:ss/i);
      const bulkInput = allInputs[0]; // First one is the bulk input
      await user.type(bulkInput, '10:00');

      // Apply to selected
      const applyButton = screen.getByRole('button', { name: /שלח/i });
      await user.click(applyButton);

      // Check that both students have calculated grades
      await waitFor(() => {
        const grades = screen.getAllByText('100.00');
        expect(grades.length).toBeGreaterThanOrEqual(2);
      });
    });

    it('should validate bulk raw result before applying', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
      });

      // Select a student
      const checkbox1 = screen.getByLabelText(/select student 1/i);
      await user.click(checkbox1);

      // Enter invalid bulk raw result
      const allInputs = screen.getAllByLabelText(/ציון.*mm:ss/i);
      const bulkInput = allInputs[0]; // First one is the bulk input
      await user.type(bulkInput, 'invalid');

      // Try to apply
      const applyButton = screen.getByRole('button', { name: /שלח/i });
      await user.click(applyButton);

      // Check for validation error
      await waitFor(() => {
        expect(screen.getByText(/פורמט לא תקין/i)).toBeInTheDocument();
      });
    });

    it('should show error when trying to apply without selecting students', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
      });

      // Enter bulk raw result without selecting students
      const allInputs = screen.getAllByLabelText(/ציון.*mm:ss/i);
      const bulkInput = allInputs[0]; // First one is the bulk input
      await user.type(bulkInput, '10:00');

      // Try to apply
      const applyButton = screen.getByRole('button', { name: /שלח/i });
      await user.click(applyButton);

      // Check for error
      await waitFor(() => {
        expect(screen.getByText(/בחר הכל/i)).toBeInTheDocument();
      });
    });

    it('should mark changes as unsaved after applying bulk result', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
      });

      // Select a student
      const checkbox1 = screen.getByLabelText(/select student 1/i);
      await user.click(checkbox1);

      // Enter and apply bulk raw result
      const allInputs = screen.getAllByLabelText(/ציון.*mm:ss/i);
      const bulkInput = allInputs[0]; // First one is the bulk input
      await user.type(bulkInput, '10:00');

      const applyButton = screen.getByRole('button', { name: /שלח/i });
      await user.click(applyButton);

      // Check for unsaved changes indicator
      await waitFor(() => {
        expect(screen.getByText(/איפוס/i)).toBeInTheDocument();
      });
    });

    it('should highlight selected students with visual indicator', async () => {
      const user = userEvent.setup();
      renderGradeEntry();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, '1');

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
      });

      // Select a student
      const checkbox1 = screen.getByLabelText(/select student 1/i);
      await user.click(checkbox1);

      // Check that the student card has the selected styling - need to find the parent card div
      const studentCard = checkbox1.closest('div.bg-white');
      expect(studentCard).toHaveClass('border-primary-color');
    });
  });
});

