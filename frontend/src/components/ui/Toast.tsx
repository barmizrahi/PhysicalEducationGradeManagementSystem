
import React, { useEffect } from 'react';

 

interface ToastProps {

  message: string | null;

  type?: 'success' | 'error' | 'info';

  onClose: () => void;

  duration?: number;

}

 

const TOAST_STYLES = {

  success: 'bg-green-50 border-green-500 shadow-green-200',

  error: 'bg-red-50 border-red-500 shadow-red-200',

  info: 'bg-blue-50 border-blue-500 shadow-blue-200',

};

 

const TOAST_ICONS = {

  success: '🎉',

  error: '🚨',

  info: '💡',

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

    <>

      {/* Backdrop overlay */}

      <div

        className="fixed inset-0 z-[99] bg-black/30 backdrop-blur-sm"

        onClick={onClose}

      />

 

      {/* Toast popup */}

      <div

        dir="rtl"

        className={`

          fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2

          z-[100]

          w-[420px] max-w-[90vw]

          ${currentStyle}

          border-4 rounded-2xl

          shadow-[0_20px_60px_rgba(0,0,0,0.3)]

          px-8 py-8

          flex flex-col items-center gap-4

          animate-[bounceIn_0.4s_ease-out]

        `}

      >

        {/* Big icon */}

        <div className="text-6xl">{icon}</div>

 

        {/* Message */}

        <p className="text-xl font-bold text-center text-gray-900 leading-relaxed">

          {message}

        </p>

 

        {/* Close button */}

        <button

          onClick={onClose}

          className="

            mt-2 px-6 py-2

            bg-gray-800 text-white

            rounded-lg text-base font-medium

            hover:bg-gray-900 transition-colors

          "

        >

          סגור

        </button>

      </div>

    </>

  );

};

 
