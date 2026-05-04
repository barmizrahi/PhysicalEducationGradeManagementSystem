import React from 'react';
import { useTranslation } from 'react-i18next';

export interface LoadingSpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  message?: string;
  fullScreen?: boolean;
  className?: string;
}

/**
 * Reusable LoadingSpinner component for loading states
 * Supports Hebrew RTL text and mobile-responsive design
 */
export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  size = 'md',
  message,
  fullScreen = false,
  className = '',
}) => {
  const { t } = useTranslation();
  
  const sizeStyles = {
    sm: 'h-6 w-6 border-2',
    md: 'h-10 w-10 border-3',
    lg: 'h-16 w-16 border-4',
  };
  
  const spinner = (
    <div className={`flex flex-col items-center justify-center ${className}`}>
      <div
        className={`${sizeStyles[size]} border-border-color border-t-primary-color rounded-full animate-spin`}
        role="status"
        aria-label={t('common.loading')}
      >
        <span className="sr-only">{t('common.loading')}</span>
      </div>
      {message && (
        <p className="mt-3 text-sm text-text-secondary">{message}</p>
      )}
    </div>
  );
  
  if (fullScreen) {
    return (
      <div className="fixed inset-0 bg-white bg-opacity-75 flex items-center justify-center z-50">
        {spinner}
      </div>
    );
  }
  
  return spinner;
};
