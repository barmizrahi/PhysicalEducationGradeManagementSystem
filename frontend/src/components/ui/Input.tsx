import React from 'react';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helperText?: string;
  fullWidth?: boolean;
}

/**
 * Reusable Input component with mobile-responsive design
 * Uses minimum 16px font size to prevent automatic zoom on iOS devices
 * Supports Hebrew RTL text
 */
export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, helperText, fullWidth = false, className = '', id, ...props }, ref) => {
    const inputId = id || `input-${Math.random().toString(36).substr(2, 9)}`;
    
    const baseStyles = 'border rounded-md px-3 py-2 transition-colors focus:outline-none focus:ring-2 focus:ring-primary-color focus:border-transparent disabled:bg-gray-100 disabled:cursor-not-allowed';
    const errorStyles = error ? 'border-error-color' : 'border-border-color';
    const widthStyle = fullWidth ? 'w-full' : '';
    
    // Minimum 16px font size to prevent mobile zoom
    const fontStyle = 'text-base'; // 16px
    
    const combinedClassName = `${baseStyles} ${errorStyles} ${widthStyle} ${fontStyle} ${className}`.trim();
    
    return (
      <div className={fullWidth ? 'w-full' : ''}>
        {label && (
          <label
            htmlFor={inputId}
            className="block text-sm font-medium text-text-primary mb-1"
          >
            {label}
          </label>
        )}
        <input
          ref={ref}
          id={inputId}
          className={combinedClassName}
          aria-invalid={error ? 'true' : 'false'}
          aria-describedby={error ? `${inputId}-error` : helperText ? `${inputId}-helper` : undefined}
          {...props}
        />
        {error && (
          <p
            id={`${inputId}-error`}
            className="mt-1 text-sm text-error-color"
            role="alert"
          >
            {error}
          </p>
        )}
        {helperText && !error && (
          <p
            id={`${inputId}-helper`}
            className="mt-1 text-sm text-text-secondary"
          >
            {helperText}
          </p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';
