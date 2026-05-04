import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { StudentList } from './StudentList';
import { studentsApi } from '../api/students';
import type { Student } from '../types';

// Mock the students API
vi.mock('../api/students', () => ({
  studentsApi: {
    getStudentsByGradeAndClass: vi.fn(),
  },
}));

// Helper function to render StudentList with MemoryRouter
const renderStudentList = () => {
  return render(
    <MemoryRouter>
      <StudentList />
    </MemoryRouter>
  );
};

describe('StudentList', () => {
  const mockStudentsData: Record<string, Record<string, Student[]>> = {
    'י': {
      'Class A': [
        {
          id: 1,
          name: 'Student 1',
          studentId: 'S001',
          gradeLevel: 'י',
          classId: 1,
          className: 'Class A',
          createdAt: '2024-01-01T00:00:00Z',
          updatedAt: '2024-01-01T00:00:00Z',
        },
        {
          id: 2,
          name: 'Student 2',
          studentId: null,
          gradeLevel: 'י',
          classId: 1,
          className: 'Class A',
          createdAt: '2024-01-01T00:00:00Z',
          updatedAt: '2024-01-01T00:00:00Z',
        },
      ],
      'Class B': [
        {
          id: 3,
          name: 'Student 3',
          studentId: 'S003',
          gradeLevel: 'י',
          classId: 2,
          className: 'Class B',
          createdAt: '2024-01-01T00:00:00Z',
          updatedAt: '2024-01-01T00:00:00Z',
        },
      ],
    },
    'יא': {
      'Class C': [
        {
          id: 4,
          name: 'Student 4',
          studentId: 'S004',
          gradeLevel: 'יא',
          classId: 3,
          className: 'Class C',
          createdAt: '2024-01-01T00:00:00Z',
          updatedAt: '2024-01-01T00:00:00Z',
        },
      ],
    },
  };

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Loading and Error States', () => {
    it('should display loading spinner while fetching data', () => {
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockImplementation(
        () => new Promise(() => {}) // Never resolves
      );

      renderStudentList();

      expect(screen.getByRole('status')).toBeInTheDocument();
    });

    it('should display error message when fetch fails', async () => {
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockRejectedValue(
        new Error('Network error')
      );

      renderStudentList();

      await waitFor(() => {
        expect(screen.getByText(/שגיאת רשת/i)).toBeInTheDocument();
      });
    });

    it('should display empty state when no students exist', async () => {
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue({});

      renderStudentList();

      await waitFor(() => {
        expect(screen.getByText(/אין תלמידים/i)).toBeInTheDocument();
        expect(screen.getByText(/ייבוא תלמידים מקובץ Excel/i)).toBeInTheDocument();
      });
    });
  });

  describe('Grade Level Filtering (Requirement 2.1, 2.3)', () => {
    it('should display all grade levels in selector', async () => {
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue(mockStudentsData);

      renderStudentList();

      await waitFor(() => {
        expect(screen.getByLabelText(/שכבה/i)).toBeInTheDocument();
      });

      const gradeSelect = screen.getByLabelText(/שכבה/i) as HTMLSelectElement;
      const options = Array.from(gradeSelect.options).map(opt => opt.value);

      expect(options).toContain('י');
      expect(options).toContain('יא');
    });

    it('should auto-select first grade level on load', async () => {
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue(mockStudentsData);

      renderStudentList();

      await waitFor(() => {
        const gradeSelect = screen.getByLabelText(/שכבה/i) as HTMLSelectElement;
        expect(gradeSelect.value).toBe('י');
      });
    });

    it('should display classes for selected grade level', async () => {
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue(mockStudentsData);

      renderStudentList();

      await waitFor(() => {
        expect(screen.getByLabelText(/כיתה/i)).toBeInTheDocument();
      });

      const classSelect = screen.getByLabelText(/כיתה/i) as HTMLSelectElement;
      const options = Array.from(classSelect.options).map(opt => opt.value);

      // Should show classes for grade י (auto-selected)
      expect(options).toContain('Class A');
      expect(options).toContain('Class B');
      expect(options).not.toContain('Class C'); // Class C is in grade יא
    });

    it('should update classes when grade level changes', async () => {
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue(mockStudentsData);
      const user = userEvent.setup();

      renderStudentList();

      await waitFor(() => {
        expect(screen.getByLabelText(/שכבה/i)).toBeInTheDocument();
      });

      // Change to grade יא
      const gradeSelect = screen.getByLabelText(/שכבה/i);
      await user.selectOptions(gradeSelect, 'יא');

      await waitFor(() => {
        const classSelect = screen.getByLabelText(/כיתה/i) as HTMLSelectElement;
        const options = Array.from(classSelect.options).map(opt => opt.value);

        expect(options).toContain('Class C');
        expect(options).not.toContain('Class A');
        expect(options).not.toContain('Class B');
      });
    });
  });

  describe('Class Filtering (Requirement 2.2, 2.4)', () => {
    it('should auto-select first class when grade level is selected', async () => {
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue(mockStudentsData);

      renderStudentList();

      await waitFor(() => {
        const classSelect = screen.getByLabelText(/כיתה/i) as HTMLSelectElement;
        expect(classSelect.value).toBe('Class A');
      });
    });

    it('should display students for selected class', async () => {
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue(mockStudentsData);

      renderStudentList();

      await waitFor(() => {
        // Should show students from Class A (auto-selected)
        expect(screen.getByText('Student 1')).toBeInTheDocument();
        expect(screen.getByText('Student 2')).toBeInTheDocument();
        expect(screen.queryByText('Student 3')).not.toBeInTheDocument();
      });
    });

    it('should update students when class changes', async () => {
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue(mockStudentsData);
      const user = userEvent.setup();

      renderStudentList();

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
      });

      // Change to Class B
      const classSelect = screen.getByLabelText(/כיתה/i);
      await user.selectOptions(classSelect, 'Class B');

      await waitFor(() => {
        expect(screen.getByText('Student 3')).toBeInTheDocument();
        expect(screen.queryByText('Student 1')).not.toBeInTheDocument();
        expect(screen.queryByText('Student 2')).not.toBeInTheDocument();
      });
    });

    it('should display student count for selected class', async () => {
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue(mockStudentsData);

      renderStudentList();

      await waitFor(() => {
        expect(screen.getByText(/2.*תלמידים/i)).toBeInTheDocument();
      });
    });
  });

  describe('Student Display', () => {
    it('should display student name and ID', async () => {
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue(mockStudentsData);

      renderStudentList();

      await waitFor(() => {
        expect(screen.getByText('Student 1')).toBeInTheDocument();
        expect(screen.getByText('S001')).toBeInTheDocument();
      });
    });

    it('should display N/A for students without student ID', async () => {
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue(mockStudentsData);

      renderStudentList();

      await waitFor(() => {
        expect(screen.getByText('Student 2')).toBeInTheDocument();
        // Find the N/A in the same row as Student 2
        const rows = screen.getAllByRole('row');
        const student2Row = rows.find(row => row.textContent?.includes('Student 2'));
        expect(student2Row?.textContent).toContain('N/A');
      });
    });

    it('should display grade level and class for each student', async () => {
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue(mockStudentsData);

      renderStudentList();

      await waitFor(() => {
        const rows = screen.getAllByRole('row');
        const student1Row = rows.find(row => row.textContent?.includes('Student 1'));
        expect(student1Row?.textContent).toContain('י');
      });
    });
  });

  describe('Mobile Responsiveness', () => {
    it('should display mobile scroll tip', async () => {
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue(mockStudentsData);

      renderStudentList();

      await waitFor(() => {
        expect(screen.getByText(/סנן לפי/i)).toBeInTheDocument();
      });
    });

    it('should render selectors in column layout on mobile', async () => {
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue(mockStudentsData);

      renderStudentList();

      await waitFor(() => {
        const filtersContainer = screen.getByLabelText(/שכבה/i).closest('.flex');
        expect(filtersContainer).toHaveClass('flex-col');
      });
    });
  });

  describe('Edge Cases', () => {
    it('should handle empty class list for a grade level', async () => {
      const emptyClassData = {
        'י': {},
      };
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue(emptyClassData);

      renderStudentList();

      await waitFor(() => {
        const classSelect = screen.getByLabelText(/כיתה/i) as HTMLSelectElement;
        expect(classSelect.disabled).toBe(true);
      });
    });

    it('should handle empty student list for a class', async () => {
      const emptyStudentData = {
        'י': {
          'Class A': [],
        },
      };
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue(emptyStudentData);

      renderStudentList();

      await waitFor(() => {
        expect(screen.getByText(/אין תלמידים/i)).toBeInTheDocument();
      });
    });

    it('should display singular form for student count when count is 1', async () => {
      const singleStudentData = {
        'י': {
          'Class A': [mockStudentsData['י']['Class B'][0]], // Only one student
        },
      };
      vi.mocked(studentsApi.getStudentsByGradeAndClass).mockResolvedValue(singleStudentData);

      renderStudentList();

      await waitFor(() => {
        expect(screen.getByText(/1.*תלמיד/i)).toBeInTheDocument();
      });
    });
  });
});

