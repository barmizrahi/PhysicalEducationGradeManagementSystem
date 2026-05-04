import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { StudentImport } from './StudentImport';
import { studentsApi } from '../api/students';
import { ImportResult } from '../types';

// Mock the students API
vi.mock('../api/students', () => ({
  studentsApi: {
    importStudents: vi.fn(),
  },
}));

describe('StudentImport', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders the component with title and file input', () => {
    render(<StudentImport />);
    
    expect(screen.getByText('ייבוא תלמידים מקובץ Excel')).toBeInTheDocument();
    expect(screen.getByText('בחר קובץ Excel')).toBeInTheDocument();
  });

  it('displays format instructions with example table', () => {
    render(<StudentImport />);
    
    expect(screen.getByText('פורמט קובץ צפוי')).toBeInTheDocument();
    expect(screen.getByText('הקובץ חייב להכיל בדיוק 4 עמודות בסדר הבא:')).toBeInTheDocument();
    expect(screen.getByText('• עמודה 1: תעודת זהות')).toBeInTheDocument();
    expect(screen.getByText('• עמודה 2: שם התלמיד (בעברית)')).toBeInTheDocument();
    expect(screen.getByText('• עמודה 3: שכבה (י, יא, או יב)')).toBeInTheDocument();
    expect(screen.getByText('• עמודה 4: שם הכיתה')).toBeInTheDocument();
    expect(screen.getByText('דוגמה:')).toBeInTheDocument();
    
    // Check example table headers
    expect(screen.getByText('תעודת זהות')).toBeInTheDocument();
    expect(screen.getByText('שם התלמיד')).toBeInTheDocument();
    expect(screen.getByText('שכבה')).toBeInTheDocument();
    expect(screen.getByText('כיתה')).toBeInTheDocument();
    
    // Check example table data
    expect(screen.getByText('יוסי כהן')).toBeInTheDocument();
    expect(screen.getByText('שרה לוי')).toBeInTheDocument();
    expect(screen.getByText('דוד מזרחי')).toBeInTheDocument();
  });

  it('does not display column mapping section', () => {
    render(<StudentImport />);
    
    const file = new File(['test'], 'students.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });
    
    const fileInput = screen.getByLabelText('בחר קובץ Excel') as HTMLInputElement;
    fireEvent.change(fileInput, { target: { files: [file] } });
    
    // Column mapping section should not exist
    expect(screen.queryByText('מיפוי עמודות')).not.toBeInTheDocument();
    expect(screen.queryByText('עמודת שם')).not.toBeInTheDocument();
    expect(screen.queryByText('עמודת תעודת זהות')).not.toBeInTheDocument();
  });

  it('shows error when invalid file type is selected', () => {
    render(<StudentImport />);
    
    const file = new File(['test'], 'students.txt', { type: 'text/plain' });
    
    const fileInput = screen.getByLabelText('בחר קובץ Excel') as HTMLInputElement;
    fireEvent.change(fileInput, { target: { files: [file] } });
    
    expect(screen.getByText('פורמט קובץ לא תקין')).toBeInTheDocument();
  });

  it('successfully imports students with valid data', async () => {
    const mockResult: ImportResult = {
      studentsCreated: 5,
      studentsUpdated: 2,
      errors: [],
    };
    
    vi.mocked(studentsApi.importStudents).mockResolvedValue(mockResult);
    
    render(<StudentImport />);
    
    // Select file
    const file = new File(['test'], 'students.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });
    const fileInput = screen.getByLabelText('בחר קובץ Excel') as HTMLInputElement;
    fireEvent.change(fileInput, { target: { files: [file] } });
    
    // Click import (no column mapping needed)
    const importButton = screen.getByText('ייבוא תלמידים');
    fireEvent.click(importButton);
    
    await waitFor(() => {
      expect(screen.getByText(/תלמידים יובאו בהצלחה/)).toBeInTheDocument();
      expect(screen.getByText(/תלמידים נוצרו:/)).toBeInTheDocument();
      expect(screen.getByText('5')).toBeInTheDocument();
      expect(screen.getByText(/תלמידים עודכנו:/)).toBeInTheDocument();
      expect(screen.getByText('2')).toBeInTheDocument();
    });
    
    // API should be called without column mapping
    expect(studentsApi.importStudents).toHaveBeenCalledWith(file);
  });

  it('displays import errors', async () => {
    const mockResult: ImportResult = {
      studentsCreated: 2,
      studentsUpdated: 0,
      errors: ['שורה 3: שכבה לא תקינה', 'שורה 5: חסר שם כיתה'],
    };
    
    vi.mocked(studentsApi.importStudents).mockResolvedValue(mockResult);
    
    render(<StudentImport />);
    
    // Select file
    const file = new File(['test'], 'students.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });
    const fileInput = screen.getByLabelText('בחר קובץ Excel') as HTMLInputElement;
    fireEvent.change(fileInput, { target: { files: [file] } });
    
    // Click import
    const importButton = screen.getByText('ייבוא תלמידים');
    fireEvent.click(importButton);
    
    await waitFor(() => {
      expect(screen.getByText('שגיאות:')).toBeInTheDocument();
      expect(screen.getByText('שורה 3: שכבה לא תקינה')).toBeInTheDocument();
      expect(screen.getByText('שורה 5: חסר שם כיתה')).toBeInTheDocument();
    });
  });

  it('handles API errors', async () => {
    const errorMessage = 'מספר עמודות שגוי - נדרשות בדיוק 4 עמודות';
    vi.mocked(studentsApi.importStudents).mockRejectedValue({
      response: { data: { message: errorMessage } },
    });
    
    render(<StudentImport />);
    
    // Select file
    const file = new File(['test'], 'students.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });
    const fileInput = screen.getByLabelText('בחר קובץ Excel') as HTMLInputElement;
    fireEvent.change(fileInput, { target: { files: [file] } });
    
    // Click import
    const importButton = screen.getByText('ייבוא תלמידים');
    fireEvent.click(importButton);
    
    await waitFor(() => {
      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
  });

  it('calls onImportComplete callback when provided', async () => {
    const mockResult: ImportResult = {
      studentsCreated: 3,
      studentsUpdated: 1,
      errors: [],
    };
    
    const onImportComplete = vi.fn();
    vi.mocked(studentsApi.importStudents).mockResolvedValue(mockResult);
    
    render(<StudentImport onImportComplete={onImportComplete} />);
    
    // Select file
    const file = new File(['test'], 'students.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });
    const fileInput = screen.getByLabelText('בחר קובץ Excel') as HTMLInputElement;
    fireEvent.change(fileInput, { target: { files: [file] } });
    
    // Click import
    const importButton = screen.getByText('ייבוא תלמידים');
    fireEvent.click(importButton);
    
    await waitFor(() => {
      expect(onImportComplete).toHaveBeenCalledWith(mockResult);
    });
  });

  it('allows resetting the form after import', async () => {
    const mockResult: ImportResult = {
      studentsCreated: 5,
      studentsUpdated: 0,
      errors: [],
    };
    
    vi.mocked(studentsApi.importStudents).mockResolvedValue(mockResult);
    
    render(<StudentImport />);
    
    // Select file
    const file = new File(['test'], 'students.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });
    const fileInput = screen.getByLabelText('בחר קובץ Excel') as HTMLInputElement;
    fireEvent.change(fileInput, { target: { files: [file] } });
    
    // Click import
    const importButton = screen.getByText('ייבוא תלמידים');
    fireEvent.click(importButton);
    
    await waitFor(() => {
      expect(screen.getByText(/תלמידים יובאו בהצלחה/)).toBeInTheDocument();
    });
    
    // Click reset button
    const resetButton = screen.getByText('ייבא קובץ נוסף');
    fireEvent.click(resetButton);
    
    // Verify form is reset
    expect(screen.queryByText(/תלמידים יובאו בהצלחה/)).not.toBeInTheDocument();
  });

  it('disables import button when no file is selected', () => {
    render(<StudentImport />);
    
    const importButton = screen.getByText('ייבוא תלמידים');
    expect(importButton).toBeDisabled();
  });

  it('shows loading state during import', async () => {
    vi.mocked(studentsApi.importStudents).mockImplementation(
      () => new Promise((resolve) => setTimeout(resolve, 100))
    );
    
    render(<StudentImport />);
    
    // Select file
    const file = new File(['test'], 'students.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });
    const fileInput = screen.getByLabelText('בחר קובץ Excel') as HTMLInputElement;
    fireEvent.change(fileInput, { target: { files: [file] } });
    
    // Click import
    const importButton = screen.getByText('ייבוא תלמידים');
    fireEvent.click(importButton);
    
    // Check for loading state
    expect(screen.getByText('מייבא...')).toBeInTheDocument();
  });

  it('clears file when clear button is clicked', () => {
    render(<StudentImport />);
    
    const file = new File(['test'], 'students.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    });
    
    const fileInput = screen.getByLabelText('בחר קובץ Excel') as HTMLInputElement;
    fireEvent.change(fileInput, { target: { files: [file] } });
    
    expect(screen.getByText(/קובץ נבחר: students.xlsx/)).toBeInTheDocument();
    
    const clearButton = screen.getByText('איפוס');
    fireEvent.click(clearButton);
    
    expect(screen.queryByText(/קובץ נבחר: students.xlsx/)).not.toBeInTheDocument();
  });
});
