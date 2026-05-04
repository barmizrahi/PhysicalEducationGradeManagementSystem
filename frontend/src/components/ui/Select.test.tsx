import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Select } from './Select';

const mockOptions = [
  { value: 'option1', label: 'Option 1' },
  { value: 'option2', label: 'Option 2' },
  { value: 'option3', label: 'Option 3' },
];

describe('Select Component', () => {
  it('renders select field', () => {
    render(<Select options={mockOptions} />);
    expect(screen.getByRole('combobox')).toBeInTheDocument();
  });

  it('renders with label', () => {
    render(<Select label="Choose option" options={mockOptions} />);
    expect(screen.getByLabelText('Choose option')).toBeInTheDocument();
  });

  it('renders all options', () => {
    render(<Select options={mockOptions} />);
    expect(screen.getByRole('option', { name: 'Option 1' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Option 2' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Option 3' })).toBeInTheDocument();
  });

  it('renders with placeholder', () => {
    render(<Select options={mockOptions} placeholder="Select an option" />);
    expect(screen.getByRole('option', { name: 'Select an option' })).toBeInTheDocument();
  });

  it('renders with error message', () => {
    render(<Select options={mockOptions} error="This field is required" />);
    expect(screen.getByRole('alert')).toHaveTextContent('This field is required');
  });

  it('renders with helper text', () => {
    render(<Select options={mockOptions} helperText="Choose your preferred option" />);
    expect(screen.getByText('Choose your preferred option')).toBeInTheDocument();
  });

  it('applies error styles when error is present', () => {
    render(<Select options={mockOptions} error="Error" />);
    const select = screen.getByRole('combobox');
    expect(select).toHaveClass('border-error-color');
    expect(select).toHaveAttribute('aria-invalid', 'true');
  });

  it('applies full width when specified', () => {
    render(<Select options={mockOptions} fullWidth />);
    const select = screen.getByRole('combobox');
    expect(select).toHaveClass('w-full');
  });

  it('handles value changes', async () => {
    const handleChange = vi.fn();
    const user = userEvent.setup();
    
    render(<Select options={mockOptions} onChange={handleChange} />);
    const select = screen.getByRole('combobox');
    
    await user.selectOptions(select, 'option2');
    
    expect(handleChange).toHaveBeenCalled();
  });

  it('can be disabled', () => {
    render(<Select options={mockOptions} disabled />);
    const select = screen.getByRole('combobox');
    expect(select).toBeDisabled();
  });

  it('uses minimum 16px font size to prevent mobile zoom', () => {
    render(<Select options={mockOptions} />);
    const select = screen.getByRole('combobox');
    expect(select).toHaveClass('text-base'); // 16px
  });

  it('associates label with select using htmlFor', () => {
    render(<Select label="Grade Level" options={mockOptions} id="grade-select" />);
    const label = screen.getByText('Grade Level');
    const select = screen.getByRole('combobox');
    expect(label).toHaveAttribute('for', 'grade-select');
    expect(select).toHaveAttribute('id', 'grade-select');
  });
});
