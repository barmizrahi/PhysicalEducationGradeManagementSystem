import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { ExportForm } from './ExportForm';
import { exportApi } from '../api/export';

// Mock the API module
vi.mock('../api/export');

// Mock react-i18next
vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string) => {
      const translations: Record<string, string> = {
        'export.title': 'ייצוא ציונים',
        'export.exportToExcel': 'ייצוא ל-Excel',
        'export.exporting': 'מייצא...',
        'export.exportError': 'שגיאה בייצוא הקובץ',
        'export.exportFormat': 'פורמט ייצוא',
        'export.formatDescription': 'הקובץ יכלול 5 עמודות בסדר הבא:',
        'export.exportColumn1': 'עמודה 1: תעודת זהות',
        'export.exportColumn2': 'עמודה 2: שם התלמיד',
        'export.exportColumn3': 'עמודה 3: שכבה',
        'export.exportColumn4': 'עמודה 4: כיתה',
        'export.exportColumn5': 'עמודה 5: ציון סופי (מספר שלם)',
        'export.gradeNote': 'הציונים יעוגלו למספרים שלמים',
      };
      return translations[key] || key;
    },
  }),
}));

describe('ExportForm', () => {
  const mockOnExportComplete = vi.fn();
  const mockBlob = new Blob(['test data'], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(exportApi.exportGrades).mockResolvedValue(mockBlob);
    vi.mocked(exportApi.downloadExportedFile).mockImplementation(() => {});
  });

  it('renders component with Hebrew title', () => {
    render(<ExportForm onExportComplete={mockOnExportComplete} />);
    expect(screen.getByText('ייצוא ציונים')).toBeInTheDocument();
  });

  it('displays export format description in Hebrew', () => {
    render(<ExportForm onExportComplete={mockOnExportComplete} />);
    
    expect(screen.getByText('פורמט ייצוא')).toBeInTheDocument();
    expect(screen.getByText('הקובץ יכלול 5 עמודות בסדר הבא:')).toBeInTheDocument();
    expect(screen.getByText('עמודה 1: תעודת זהות')).toBeInTheDocument();
    expect(screen.getByText('עמודה 2: שם התלמיד')).toBeInTheDocument();
    expect(screen.getByText('עמודה 3: שכבה')).toBeInTheDocument();
    expect(screen.getByText('עמודה 4: כיתה')).toBeInTheDocument();
    expect(screen.getByText('עמודה 5: ציון סופי (מספר שלם)')).toBeInTheDocument();
  });

  it('displays grade rounding note in Hebrew', () => {
    render(<ExportForm onExportComplete={mockOnExportComplete} />);
    expect(screen.getByText('הציונים יעוגלו למספרים שלמים')).toBeInTheDocument();
  });

  it('displays export button', () => {
    render(<ExportForm onExportComplete={mockOnExportComplete} />);
    expect(screen.getByText('ייצוא ל-Excel')).toBeInTheDocument();
  });

  it('exports grades when button is clicked', async () => {
    render(<ExportForm onExportComplete={mockOnExportComplete} />);

    const exportButton = screen.getByText('ייצוא ל-Excel');
    fireEvent.click(exportButton);

    await waitFor(() => {
      expect(exportApi.exportGrades).toHaveBeenCalledWith({
        classIds: [],
        testIds: [],
        includeNotes: false,
      });
      expect(exportApi.downloadExportedFile).toHaveBeenCalledWith(
        mockBlob,
        expect.stringMatching(/grades_export_\d{4}-\d{2}-\d{2}\.xlsx/)
      );
      expect(mockOnExportComplete).toHaveBeenCalled();
    });
  });

  it('shows loading state during export', async () => {
    render(<ExportForm onExportComplete={mockOnExportComplete} />);

    const exportButton = screen.getByText('ייצוא ל-Excel');
    fireEvent.click(exportButton);

    expect(screen.getByText('מייצא...')).toBeInTheDocument();
    expect(exportButton).toBeDisabled();

    await waitFor(() => {
      expect(mockOnExportComplete).toHaveBeenCalled();
    });
  });

  it('handles API errors during export', async () => {
    vi.mocked(exportApi.exportGrades).mockRejectedValue(
      new Error('Failed to export grades')
    );

    render(<ExportForm onExportComplete={mockOnExportComplete} />);

    const exportButton = screen.getByText('ייצוא ל-Excel');
    fireEvent.click(exportButton);

    await waitFor(() => {
      expect(screen.getByText('Failed to export grades')).toBeInTheDocument();
      expect(mockOnExportComplete).not.toHaveBeenCalled();
    });
  });

  it('does not require any configuration before export', () => {
    render(<ExportForm onExportComplete={mockOnExportComplete} />);

    // No class selection UI
    expect(screen.queryByText(/Select Classes/i)).not.toBeInTheDocument();
    
    // No test selection UI
    expect(screen.queryByText(/Select Tests/i)).not.toBeInTheDocument();
    
    // No include notes checkbox
    expect(screen.queryByText(/Include notes/i)).not.toBeInTheDocument();
    
    // Export button is immediately available
    const exportButton = screen.getByText('ייצוא ל-Excel');
    expect(exportButton).not.toBeDisabled();
  });

  it('applies custom className prop', () => {
    const { container } = render(
      <ExportForm onExportComplete={mockOnExportComplete} className="custom-class" />
    );
    
    const formDiv = container.firstChild as HTMLElement;
    expect(formDiv.className).toContain('custom-class');
  });
});

