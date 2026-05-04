import React from 'react';

export interface SelectOption {
  value: string;
  label: string;
}

export interface SelectProps extends React.SelectHTMLAttributes<HTMLSelectElement> {
  label?: string;
  error?: string;
  helperText?: string;
  fullWidth?: boolean;
  options: SelectOption[];
  placeholder?: string;
}

/**
 * Reusable Select component with mobile-responsive design
 * Uses minimum 16px font size to prevent automatic zoom on iOS devices
 * Supports Hebrew RTL text
 */
export const Select = React.forwardRef<HTMLSelectElement, SelectProps>(
  ({ label, error, helperText, fullWidth = false, options, placeholder, className = '', id, ...props }, ref) => {
    const selectId = id || `select-${Math.random().toString(36).substr(2, 9)}`;
    
    const baseStyles = 'border rounded-md px-3 py-2 transition-colors focus:outline-none focus:ring-2 focus:ring-primary-color focus:border-transparent disabled:bg-gray-100 disabled:cursor-not-allowed bg-white';
    const errorStyles = error ? 'border-error-color' : 'border-border-color';
    const widthStyle = fullWidth ? 'w-full' : '';
    
    // Minimum 16px font size to prevent mobile zoom
    const fontStyle = 'text-base'; // 16px
    
    const combinedClassName = `${baseStyles} ${errorStyles} ${widthStyle} ${fontStyle} ${className}`.trim();
    
    return (
      <div className={fullWidth ? 'w-full' : ''}>
        {label && (
          <label
            htmlFor={selectId}
            className="block text-sm font-medium text-text-primary mb-1"
          >
            {label}
          </label>
        )}
        <select
          ref={ref}
          id={selectId}
          className={combinedClassName}
          aria-invalid={error ? 'true' : 'false'}
          aria-describedby={error ? `${selectId}-error` : helperText ? `${selectId}-helper` : undefined}
          {...props}
        >
          {placeholder && (
            <option value="" disabled>
              {placeholder}
            </option>
          )}
          {options.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
        {error && (
          <p
            id={`${selectId}-error`}
            className="mt-1 text-sm text-error-color"
            role="alert"
          >
            {error}
          </p>
        )}
        {helperText && !error && (
          <p
            id={`${selectId}-helper`}
            className="mt-1 text-sm text-text-secondary"
          >
            {helperText}
          </p>
        )}
      </div>
    );
  }
);

Select.displayName = 'Select';
