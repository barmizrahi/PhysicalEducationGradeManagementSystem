import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { TestAssignment } from './TestAssignment';
import { testsApi } from '../api/tests';
import { studentsApi } from '../api/students';

// Mock the API modules
vi.mock('../api/tests');
vi.mock('../api/students');

describe('TestAssignment', () => {
  const mockTestId = 1;
  const mockTestName = 'Push-ups Test';
  const mockOnAssignmentComplete = vi.fn();
  const mockOnCancel = vi.fn();

  const mockGroupedStudents = {
    'י': {
      'Class 1': [
        {
          id: 1,
          name: 'Student 1',
          studentId: '001',
          gradeLevel: 'י',
          classId: 101,
          className: 'Class 1',
          createdAt: '2024-01-01',
          updatedAt: '2024-01-01',
        },
      ],
      'Class 2': [
        {
          id: 2,
          name: 'Student 2',
          studentId: '002',
          gradeLevel: 'י',
          classId: 102,
          className: 'Class 2',
          createdAt: '2024-01-01',
          updatedAt: '2024-01-01',
        },
      ],
    },
    'יא': {
      'Class 3': [
        {
          id: 3,
          name: 'Student 3',
          studentId: '003',
          gradeLevel: 'יא',
          classId: 103,
          className: 'Class 3',
          createdAt: '2024-01-01',
          updatedAt: '2024-01-01',
        },
      ],
    },
  };

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue(mockGroupedStudents);
    vi.mocked(testsApi.assignTestToClasses).mockResolvedValue();
  });

  it('renders component with test name', async () => {
    render(
      <TestAssignment
        testId={mockTestId}
        testName={mockTestName}
        onAssignmentComplete={mockOnAssignmentComplete}
        onCancel={mockOnCancel}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('Assign Test to Classes')).toBeInTheDocument();
      expect(screen.getByText(mockTestName)).toBeInTheDocument();
    });
  });

  it('loads and displays classes grouped by grade level', async () => {
    render(
      <TestAssignment
        testId={mockTestId}
        testName={mockTestName}
        onAssignmentComplete={mockOnAssignmentComplete}
      />
    );

    await waitFor(() => {
      expect(studentsApi.getStudentsByGradeAndClass).toHaveBeenCalled();
    });

    // Switch to class selection mode to see the classes
    const classButton = screen.getByText('By Individual Classes');
    fireEvent.click(classButton);

    await waitFor(() => {
      expect(screen.getByText('Grade י')).toBeInTheDocument();
      expect(screen.getByText('Grade יא')).toBeInTheDocument();
      expect(screen.getByText('Class 1')).toBeInTheDocument();
      expect(screen.getByText('Class 2')).toBeInTheDocument();
      expect(screen.getByText('Class 3')).toBeInTheDocument();
    });
  });

  it('allows grade level assignment', async () => {
    render(
      <TestAssignment
        testId={mockTestId}
        testName={mockTestName}
        onAssignmentComplete={mockOnAssignmentComplete}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('By Grade Level')).toBeInTheDocument();
    });

    // Select grade level
    const gradeSelect = screen.getByLabelText('Grade Level');
    fireEvent.change(gradeSelect, { target: { value: 'י' } });

    await waitFor(() => {
      expect(screen.getByText(/Class 1, Class 2/)).toBeInTheDocument();
    });

    // Submit assignment
    const submitButton = screen.getByText('Assign Test');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(testsApi.assignTestToClasses).toHaveBeenCalledWith(mockTestId, [101, 102]);
      expect(mockOnAssignmentComplete).toHaveBeenCalled();
    });
  });

  it('allows individual class selection', async () => {
    render(
      <TestAssignment
        testId={mockTestId}
        testName={mockTestName}
        onAssignmentComplete={mockOnAssignmentComplete}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('By Individual Classes')).toBeInTheDocument();
    });

    // Switch to class selection mode
    const classButton = screen.getByText('By Individual Classes');
    fireEvent.click(classButton);

    await waitFor(() => {
      expect(screen.getByText('Class 1')).toBeInTheDocument();
    });

    // Select individual classes
    const class1Checkbox = screen.getByLabelText('Class 1');
    const class3Checkbox = screen.getByLabelText('Class 3');
    
    fireEvent.click(class1Checkbox);
    fireEvent.click(class3Checkbox);

    await waitFor(() => {
      expect(screen.getByText('2 class(es) selected')).toBeInTheDocument();
    });

    // Submit assignment
    const submitButton = screen.getByText('Assign Test');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(testsApi.assignTestToClasses).toHaveBeenCalledWith(mockTestId, [101, 103]);
      expect(mockOnAssignmentComplete).toHaveBeenCalled();
    });
  });

  it('validates grade level selection', async () => {
    render(
      <TestAssignment
        testId={mockTestId}
        testName={mockTestName}
        onAssignmentComplete={mockOnAssignmentComplete}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('Assign Test')).toBeInTheDocument();
    });

    // Try to submit without selecting grade level
    const submitButton = screen.getByText('Assign Test');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Please select a grade level')).toBeInTheDocument();
      expect(testsApi.assignTestToClasses).not.toHaveBeenCalled();
    });
  });

  it('validates class selection', async () => {
    render(
      <TestAssignment
        testId={mockTestId}
        testName={mockTestName}
        onAssignmentComplete={mockOnAssignmentComplete}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('By Individual Classes')).toBeInTheDocument();
    });

    // Switch to class selection mode
    const classButton = screen.getByText('By Individual Classes');
    fireEvent.click(classButton);

    await waitFor(() => {
      expect(screen.getByText('Select Classes')).toBeInTheDocument();
    });

    // Try to submit without selecting any classes
    const submitButton = screen.getByText('Assign Test');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Please select at least one class')).toBeInTheDocument();
      expect(testsApi.assignTestToClasses).not.toHaveBeenCalled();
    });
  });

  it('handles select all in grade', async () => {
    render(
      <TestAssignment
        testId={mockTestId}
        testName={mockTestName}
        onAssignmentComplete={mockOnAssignmentComplete}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('By Individual Classes')).toBeInTheDocument();
    });

    // Switch to class selection mode
    const classButton = screen.getByText('By Individual Classes');
    fireEvent.click(classButton);

    await waitFor(() => {
      expect(screen.getByText('Grade י')).toBeInTheDocument();
    });

    // Click "Select All" for grade י
    const selectAllButtons = screen.getAllByText('Select All');
    fireEvent.click(selectAllButtons[0]); // First "Select All" button (for grade י)

    await waitFor(() => {
      expect(screen.getByText('2 class(es) selected')).toBeInTheDocument();
    });

    // Click "Deselect All"
    const deselectAllButton = screen.getByText('Deselect All');
    fireEvent.click(deselectAllButton);

    await waitFor(() => {
      expect(screen.queryByText('2 class(es) selected')).not.toBeInTheDocument();
    });
  });

  it('handles API errors during class loading', async () => {
    vi.mocked(studentsApi.getStudentsByGradeAndClass).mockRejectedValue(
      new Error('Failed to load classes')
    );

    render(
      <TestAssignment
        testId={mockTestId}
        testName={mockTestName}
        onAssignmentComplete={mockOnAssignmentComplete}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('Failed to load classes')).toBeInTheDocument();
    });
  });

  it('handles API errors during assignment', async () => {
    vi.mocked(testsApi.assignTestToClasses).mockRejectedValue(
      new Error('Failed to assign test')
    );

    render(
      <TestAssignment
        testId={mockTestId}
        testName={mockTestName}
        onAssignmentComplete={mockOnAssignmentComplete}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('By Grade Level')).toBeInTheDocument();
    });

    // Select grade level
    const gradeSelect = screen.getByLabelText('Grade Level');
    fireEvent.change(gradeSelect, { target: { value: 'י' } });

    // Submit assignment
    const submitButton = screen.getByText('Assign Test');
    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(screen.getByText('Failed to assign test')).toBeInTheDocument();
      expect(mockOnAssignmentComplete).not.toHaveBeenCalled();
    });
  });

  it('calls onCancel when cancel button is clicked', async () => {
    render(
      <TestAssignment
        testId={mockTestId}
        testName={mockTestName}
        onAssignmentComplete={mockOnAssignmentComplete}
        onCancel={mockOnCancel}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('Cancel')).toBeInTheDocument();
    });

    const cancelButton = screen.getByText('Cancel');
    fireEvent.click(cancelButton);

    expect(mockOnCancel).toHaveBeenCalled();
  });

  it('disables buttons during loading', async () => {
    render(
      <TestAssignment
        testId={mockTestId}
        testName={mockTestName}
        onAssignmentComplete={mockOnAssignmentComplete}
      />
    );

    await waitFor(() => {
      expect(screen.getByText('By Grade Level')).toBeInTheDocument();
    });

    // Select grade level
    const gradeSelect = screen.getByLabelText('Grade Level');
    fireEvent.change(gradeSelect, { target: { value: 'י' } });

    // Submit assignment
    const submitButton = screen.getByText('Assign Test');
    fireEvent.click(submitButton);

    // Buttons should be disabled during loading
    expect(submitButton).toBeDisabled();
    expect(screen.getByText('By Grade Level')).toBeDisabled();
    expect(screen.getByText('By Individual Classes')).toBeDisabled();
  });
});
