import React from 'react';
import { useTranslation } from 'react-i18next';

export interface ErrorMessageProps {
  message: string;
  title?: string;
  onRetry?: () => void;
  className?: string;
}

/**
 * Reusable ErrorMessage component for displaying error states
 * Supports Hebrew RTL text and mobile-responsive design
 */
export const ErrorMessage: React.FC<ErrorMessageProps> = ({
  message,
  title,
  onRetry,
  className = '',
}) => {
  const { t } = useTranslation();
  const displayTitle = title || t('common.error');
  
  return (
    <div
      className={`rounded-md bg-red-50 border border-error-color p-4 ${className}`}
      role="alert"
    >
      <div className="flex">
        <div className="flex-shrink-0">
          <svg
            className="h-5 w-5 text-error-color"
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 20 20"
            fill="currentColor"
            aria-hidden="true"
          >
            <path
              fillRule="evenodd"
              d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z"
              clipRule="evenodd"
            />
          </svg>
        </div>
        <div className="ml-3 flex-1">
          <h3 className="text-sm font-medium text-error-color">{displayTitle}</h3>
          <div className="mt-2 text-sm text-red-700">
            <p>{message}</p>
          </div>
          {onRetry && (
            <div className="mt-4">
              <button
                type="button"
                onClick={onRetry}
                className="text-sm font-medium text-error-color hover:text-red-600 focus:outline-none focus:underline"
              >
                {t('errors.tryAgain')}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
