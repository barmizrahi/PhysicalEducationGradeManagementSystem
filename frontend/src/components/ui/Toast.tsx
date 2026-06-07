import React, { useEffect } from 'react';

 

interface ToastProps {

  message: string | null;

  type?: 'success' | 'error' | 'info';

  onClose: () => void;

  duration?: number;

}

 

const TOAST_STYLES = {

  success: 'bg-green-100 border-green-400',

  error: 'bg-red-100 border-red-400',

  info: 'bg-blue-100 border-blue-400',

};

 

const TOAST_ICONS = {

  success: '✅',

  error: '❌',

  info: 'ℹ️',

};

 

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

 

  const currentStyle = TOAST_STYLES[type] || TOAST_STYLES.success;

  const icon = TOAST_ICONS[type];

 

  return (

    <div

      dir="rtl"

      className={`

        fixed bottom-6 right-6 z-[100]

        min-w-[300px] max-w-md

        ${currentStyle}

        border-2 text-black rounded-xl shadow-2xl

        flex items-center justify-between

        px-5 py-4

      `}

    >

      <div className="flex items-center gap-3 flex-1">

        <div className="text-2xl">{icon}</div>

        <p className="text-base font-medium leading-tight flex-1 text-black">

          {message}

        </p>

      </div>

 

      <button

        onClick={onClose}

        className="mr-4 text-2xl leading-none text-black/70 hover:text-black transition-colors font-bold"

        aria-label="סגור"

      >

        ×

      </button>

    </div>

  );

};
