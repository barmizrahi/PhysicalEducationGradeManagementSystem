/**
 * Shared UI Components
 * 
 * Mobile-responsive, accessible components with:
 * - Minimum 16px font size for inputs (prevents iOS zoom)
 * - Hebrew RTL text support
 * - Touch-optimized sizing (min 44px height for interactive elements)
 * - Responsive design (min 375px width support)
 */

export { Button } from './Button';
export type { ButtonProps } from './Button';

export { Input } from './Input';
export type { InputProps } from './Input';

export { Select } from './Select';
export type { SelectProps, SelectOption } from './Select';

export { Table } from './Table';
export type { TableProps, TableColumn } from './Table';

export { ErrorMessage } from './ErrorMessage';
export type { ErrorMessageProps } from './ErrorMessage';

export { LoadingSpinner } from './LoadingSpinner';
export type { LoadingSpinnerProps } from './LoadingSpinner';
