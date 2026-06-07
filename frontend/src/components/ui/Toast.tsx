import React, { useEffect } from 'react';

interface ToastProps {
  message: string | null;
  type?: 'success' | 'error' | 'info';
  onClose: () => void;
  duration?: number;
}

export const Toast: React.FC<ToastProps> = ({
  message,
  type = 'success',
  onClose,
  duration = 5000,
}) => {
  useEffect(() => {
    if (!message) return;

    const timer = setTimeout(onClose, duration);
    return () => clearTimeout(timer);
  }, [message, onClose, duration]);

  if (!message) return null;

  const styles = {
    success: 'bg-green-600 border-green-700',
    error: 'bg-red-600 border-red-700',
    info: 'bg-blue-600 border-blue-700',
  };

  const currentStyle = styles[type] || styles.success;

  return (
    <div className={`fixed bottom-6 right-6 z-[100] min-w-[300px] max-w-md 
                     ${currentStyle} border-2 text-white rounded-xl shadow-2xl 
                     flex items-center justify-between px-5 py-4`}>
      <div className="flex items-center gap-3 flex-1">
        <div className="text-2xl">
          {type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️'}
        </div>
        <p className="text-base font-medium leading-tight flex-1">
          {message}
        </p>
      </div>
      
      <button
        onClick={onClose}
        className="ml-4 text-2xl leading-none text-white/80 hover:text-white transition-colors font-bold"
        aria-label="Close"
      >
        ×
      </button>
    </div>
  );
};
