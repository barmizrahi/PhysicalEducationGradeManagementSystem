import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { TestForm } from './TestForm';
import { Test } from '../types';

describe('TestForm', () => {
  const mockOnSubmit = vi.fn();
  const mockOnCancel = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Rendering', () => {
    it('renders all required form fields', () => {
      render(<TestForm onSubmit={mockOnSubmit} />);

      expect(screen.getByLabelText('שם המבחן')).toBeInTheDocument();
      expect(screen.getByLabelText('סוג חישוב')).toBeInTheDocument();
      expect(screen.getByLabelText('סוג יחידה')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: 'צור מבחן' })).toBeInTheDocument();
    });

    it('renders cancel button when onCancel is provided', () => {
      render(<TestForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      expect(screen.getByRole('button', { name: 'ביטול' })).toBeInTheDocument();
    });

    it('does not render cancel button when onCancel is not provided', () => {
      render(<TestForm onSubmit={mockOnSubmit} />);

      expect(screen.queryByRole('button', { name: 'ביטול' })).not.toBeInTheDocument();
    });

    it('displays error message when error prop is provided', () => {
      const errorMessage = 'Failed to create test';
      render(<TestForm onSubmit={mockOnSubmit} error={errorMessage} />);

      expect(screen.getByText(errorMessage)).toBeInTheDocument();
    });
  });

  describe('Initial Values', () => {
    it('populates form with initial test data when editing', () => {
      const initialTest: Partial<Test> = {
        name: 'Push-ups Test',
        calculationType: 'RATIO',
        unitType: 'COUNT',
        maxValue: 50,
      };

      render(<TestForm onSubmit={mockOnSubmit} initialTest={initialTest} />);

      expect(screen.getByLabelText('שם המבחן')).toHaveValue('Push-ups Test');
      expect(screen.getByLabelText('סוג חישוב')).toHaveValue('RATIO');
      expect(screen.getByLabelText('סוג יחידה')).toHaveValue('COUNT');
      expect(screen.getByLabelText('ערך מקסימלי')).toHaveValue(50);
    });

    it('defaults to RATIO and COUNT when no initial data provided', () => {
      render(<TestForm onSubmit={mockOnSubmit} />);

      expect(screen.getByLabelText('סוג חישוב')).toHaveValue('RATIO');
      expect(screen.getByLabelText('סוג יחידה')).toHaveValue('COUNT');
    });
  });

  describe('Conditional Fields - RATIO', () => {
    it('shows maxValue field when RATIO calculation type is selected', () => {
      render(<TestForm onSubmit={mockOnSubmit} />);

      const calculationType = screen.getByLabelText('סוג חישוב');
      fireEvent.change(calculationType, { target: { value: 'RATIO' } });

      expect(screen.getByLabelText('ערך מקסימלי')).toBeInTheDocument();
      expect(screen.queryByLabelText('ערך יעד')).not.toBeInTheDocument();
      expect(screen.queryByLabelText('קנס ליחידה')).not.toBeInTheDocument();
    });

    it('clears PENALTY fields when switching from PENALTY to RATIO', () => {
      render(<TestForm onSubmit={mockOnSubmit} />);

      // Switch to PENALTY and fill fields
      const calculationType = screen.getByLabelText('סוג חישוב');
      fireEvent.change(calculationType, { target: { value: 'PENALTY' } });

      const targetValue = screen.getByLabelText('ערך יעד');
      const penaltyPerUnit = screen.getByLabelText('קנס ליחידה');
      fireEvent.change(targetValue, { target: { value: '10.5' } });
      fireEvent.change(penaltyPerUnit, { target: { value: '2' } });

      // Switch back to RATIO
      fireEvent.change(calculationType, { target: { value: 'RATIO' } });

      // PENALTY fields should not be visible
      expect(screen.queryByLabelText('ערך יעד')).not.toBeInTheDocument();
      expect(screen.queryByLabelText('קנס ליחידה')).not.toBeInTheDocument();
    });
  });

  describe('Conditional Fields - PENALTY', () => {
    it('shows targetValue and penaltyPerUnit fields when PENALTY calculation type is selected', () => {
      render(<TestForm onSubmit={mockOnSubmit} />);

      const calculationType = screen.getByLabelText(/calculation type/i);
      fireEvent.change(calculationType, { target: { value: 'PENALTY' } });

      expect(screen.getByLabelText(/target value/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/penalty per unit/i)).toBeInTheDocument();
      expect(screen.queryByLabelText(/max value/i)).not.toBeInTheDocument();
    });

    it('clears RATIO fields when switching from RATIO to PENALTY', () => {
      render(<TestForm onSubmit={mockOnSubmit} />);

      // Fill RATIO field
      const maxValue = screen.getByLabelText(/max value/i);
      fireEvent.change(maxValue, { target: { value: '50' } });

      // Switch to PENALTY
      const calculationType = screen.getByLabelText(/calculation type/i);
      fireEvent.change(calculationType, { target: { value: 'PENALTY' } });

      // RATIO field should not be visible
      expect(screen.queryByLabelText(/max value/i)).not.toBeInTheDocument();
    });
  });

  describe('Validation - Required Fields', () => {
    it('shows error when test name is empty', async () => {
      render(<TestForm onSubmit={mockOnSubmit} />);

      const submitButton = screen.getByRole('button', { name: /create test/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/test name is required/i)).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('shows error when maxValue is empty for RATIO calculation', async () => {
      render(<TestForm onSubmit={mockOnSubmit} />);

      const nameInput = screen.getByLabelText(/test name/i);
      fireEvent.change(nameInput, { target: { value: 'Test Name' } });

      const submitButton = screen.getByRole('button', { name: /create test/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/max value is required/i)).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('shows error when targetValue is empty for PENALTY calculation', async () => {
      render(<TestForm onSubmit={mockOnSubmit} />);

      const nameInput = screen.getByLabelText(/test name/i);
      fireEvent.change(nameInput, { target: { value: 'Test Name' } });

      const calculationType = screen.getByLabelText(/calculation type/i);
      fireEvent.change(calculationType, { target: { value: 'PENALTY' } });

      const penaltyPerUnit = screen.getByLabelText(/penalty per unit/i);
      fireEvent.change(penaltyPerUnit, { target: { value: '2' } });

      const submitButton = screen.getByRole('button', { name: /create test/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/target value is required/i)).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('shows error when penaltyPerUnit is empty for PENALTY calculation', async () => {
      render(<TestForm onSubmit={mockOnSubmit} />);

      const nameInput = screen.getByLabelText(/test name/i);
      fireEvent.change(nameInput, { target: { value: 'Test Name' } });

      const calculationType = screen.getByLabelText(/calculation type/i);
      fireEvent.change(calculationType, { target: { value: 'PENALTY' } });

      const targetValue = screen.getByLabelText(/target value/i);
      fireEvent.change(targetValue, { target: { value: '10.5' } });

      const submitButton = screen.getByRole('button', { name: /create test/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/penalty per unit is required/i)).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
    });
  });

  describe('Validation - Numeric Values', () => {
    it('shows error when maxValue is not a positive number', async () => {
      render(<TestForm onSubmit={mockOnSubmit} />);

      const nameInput = screen.getByLabelText(/test name/i);
      fireEvent.change(nameInput, { target: { value: 'Test Name' } });

      const maxValue = screen.getByLabelText(/max value/i);
      fireEvent.change(maxValue, { target: { value: '-5' } });

      const submitButton = screen.getByRole('button', { name: /create test/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/max value must be a positive number/i)).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('shows error when targetValue is negative', async () => {
      render(<TestForm onSubmit={mockOnSubmit} />);

      const nameInput = screen.getByLabelText(/test name/i);
      fireEvent.change(nameInput, { target: { value: 'Test Name' } });

      const calculationType = screen.getByLabelText(/calculation type/i);
      fireEvent.change(calculationType, { target: { value: 'PENALTY' } });

      const targetValue = screen.getByLabelText(/target value/i);
      fireEvent.change(targetValue, { target: { value: '-10' } });

      const penaltyPerUnit = screen.getByLabelText(/penalty per unit/i);
      fireEvent.change(penaltyPerUnit, { target: { value: '2' } });

      const submitButton = screen.getByRole('button', { name: /create test/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/target value must be a non-negative number/i)).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('shows error when penaltyPerUnit is not a positive number', async () => {
      render(<TestForm onSubmit={mockOnSubmit} />);

      const nameInput = screen.getByLabelText(/test name/i);
      fireEvent.change(nameInput, { target: { value: 'Test Name' } });

      const calculationType = screen.getByLabelText(/calculation type/i);
      fireEvent.change(calculationType, { target: { value: 'PENALTY' } });

      const targetValue = screen.getByLabelText(/target value/i);
      fireEvent.change(targetValue, { target: { value: '10.5' } });

      const penaltyPerUnit = screen.getByLabelText(/penalty per unit/i);
      fireEvent.change(penaltyPerUnit, { target: { value: '0' } });

      const submitButton = screen.getByRole('button', { name: /create test/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(screen.getByText(/penalty per unit must be a positive number/i)).toBeInTheDocument();
      });

      expect(mockOnSubmit).not.toHaveBeenCalled();
    });
  });

  describe('Form Submission - RATIO', () => {
    it('submits valid RATIO test data', async () => {
      mockOnSubmit.mockResolvedValue(undefined);
      render(<TestForm onSubmit={mockOnSubmit} />);

      const nameInput = screen.getByLabelText(/test name/i);
      fireEvent.change(nameInput, { target: { value: 'Push-ups Test' } });

      const calculationType = screen.getByLabelText(/calculation type/i);
      fireEvent.change(calculationType, { target: { value: 'RATIO' } });

      const unitType = screen.getByLabelText(/unit type/i);
      fireEvent.change(unitType, { target: { value: 'COUNT' } });

      const maxValue = screen.getByLabelText(/max value/i);
      fireEvent.change(maxValue, { target: { value: '50' } });

      const submitButton = screen.getByRole('button', { name: /create test/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith({
          name: 'Push-ups Test',
          calculationType: 'RATIO',
          unitType: 'COUNT',
          maxValue: 50,
          targetValue: null,
          penaltyPerUnit: null,
        });
      });
    });

    it('submits RATIO test with decimal maxValue', async () => {
      mockOnSubmit.mockResolvedValue(undefined);
      render(<TestForm onSubmit={mockOnSubmit} />);

      const nameInput = screen.getByLabelText(/test name/i);
      fireEvent.change(nameInput, { target: { value: 'Test' } });

      const maxValue = screen.getByLabelText(/max value/i);
      fireEvent.change(maxValue, { target: { value: '15.5' } });

      const submitButton = screen.getByRole('button', { name: /create test/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith(
          expect.objectContaining({
            maxValue: 15.5,
          })
        );
      });
    });
  });

  describe('Form Submission - PENALTY', () => {
    it('submits valid PENALTY test data', async () => {
      mockOnSubmit.mockResolvedValue(undefined);
      render(<TestForm onSubmit={mockOnSubmit} />);

      const nameInput = screen.getByLabelText(/test name/i);
      fireEvent.change(nameInput, { target: { value: '1500m Run' } });

      const calculationType = screen.getByLabelText(/calculation type/i);
      fireEvent.change(calculationType, { target: { value: 'PENALTY' } });

      const unitType = screen.getByLabelText(/unit type/i);
      fireEvent.change(unitType, { target: { value: 'TIME' } });

      const targetValue = screen.getByLabelText(/target value/i);
      fireEvent.change(targetValue, { target: { value: '10.5' } });

      const penaltyPerUnit = screen.getByLabelText(/penalty per unit/i);
      fireEvent.change(penaltyPerUnit, { target: { value: '2' } });

      const submitButton = screen.getByRole('button', { name: /create test/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith({
          name: '1500m Run',
          calculationType: 'PENALTY',
          unitType: 'TIME',
          maxValue: null,
          targetValue: 10.5,
          penaltyPerUnit: 2,
        });
      });
    });

    it('submits PENALTY test with decimal values', async () => {
      mockOnSubmit.mockResolvedValue(undefined);
      render(<TestForm onSubmit={mockOnSubmit} />);

      const nameInput = screen.getByLabelText(/test name/i);
      fireEvent.change(nameInput, { target: { value: 'Test' } });

      const calculationType = screen.getByLabelText(/calculation type/i);
      fireEvent.change(calculationType, { target: { value: 'PENALTY' } });

      const targetValue = screen.getByLabelText(/target value/i);
      fireEvent.change(targetValue, { target: { value: '12.75' } });

      const penaltyPerUnit = screen.getByLabelText(/penalty per unit/i);
      fireEvent.change(penaltyPerUnit, { target: { value: '1.5' } });

      const submitButton = screen.getByRole('button', { name: /create test/i });
      fireEvent.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith(
          expect.objectContaining({
            targetValue: 12.75,
            penaltyPerUnit: 1.5,
          })
        );
      });
    });
  });

  describe('Loading State', () => {
    it('disables all inputs when loading', () => {
      render(<TestForm onSubmit={mockOnSubmit} loading={true} />);

      expect(screen.getByLabelText(/test name/i)).toBeDisabled();
      expect(screen.getByLabelText(/calculation type/i)).toBeDisabled();
      expect(screen.getByLabelText(/unit type/i)).toBeDisabled();
      expect(screen.getByRole('button', { name: /create test/i })).toBeDisabled();
    });

    it('shows loading state on submit button', () => {
      render(<TestForm onSubmit={mockOnSubmit} loading={true} />);

      const submitButton = screen.getByRole('button', { name: /create test/i });
      expect(submitButton).toBeDisabled();
    });
  });

  describe('Cancel Button', () => {
    it('calls onCancel when cancel button is clicked', () => {
      render(<TestForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />);

      const cancelButton = screen.getByRole('button', { name: /cancel/i });
      fireEvent.click(cancelButton);

      expect(mockOnCancel).toHaveBeenCalledTimes(1);
    });

    it('disables cancel button when loading', () => {
      render(<TestForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} loading={true} />);

      const cancelButton = screen.getByRole('button', { name: /cancel/i });
      expect(cancelButton).toBeDisabled();
    });
  });

  describe('Edit Mode', () => {
    it('shows "Update Test" button text when editing', () => {
      const initialTest: Partial<Test> = {
        name: 'Existing Test',
        calculationType: 'RATIO',
        unitType: 'COUNT',
        maxValue: 50,
      };

      render(<TestForm onSubmit={mockOnSubmit} initialTest={initialTest} />);

      expect(screen.getByRole('button', { name: /update test/i })).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: /create test/i })).not.toBeInTheDocument();
    });
  });

  describe('Mobile Responsiveness', () => {
    it('renders with fullWidth inputs for mobile layout', () => {
      render(<TestForm onSubmit={mockOnSubmit} />);

      const nameInput = screen.getByLabelText(/test name/i);
      expect(nameInput.parentElement).toHaveClass('w-full');
    });
  });
});
