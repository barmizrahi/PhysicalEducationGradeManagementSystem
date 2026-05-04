import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import heTranslation from './locales/he/translation.json';

/**
 * i18next configuration for Hebrew localization.
 * Initializes i18next with Hebrew as the default language.
 */
i18n
  .use(initReactI18next) // Passes i18n down to react-i18next
  .init({
    resources: {
      he: {
        translation: heTranslation,
      },
    },
    lng: 'he', // Default language
    fallbackLng: 'he', // Fallback language if translation is missing
    
    interpolation: {
      escapeValue: false, // React already escapes values
    },
    
    // Support for pluralization in Hebrew
    pluralSeparator: '_',
    
    // Debug mode (set to false in production)
    debug: false,
    
    // React-specific options
    react: {
      useSuspense: false, // Disable suspense mode for better error handling
    },
  });

export default i18n;
