import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

// Dil için sözlükler
import tr from './locales/tr.json';
import en from './locales/en.json';
import de from './locales/de.json';
const savedLanguage = localStorage.getItem('user-language') || 'tr';

i18n
  .use(initReactI18next) 
  .init({
    resources: {
      tr: { translation: tr },
      en: { translation: en },
      de: { translation: de }
    },
    lng: savedLanguage, 
    fallbackLng: 'tr', // Bir kelime bulunamazsa Türkçe'ye döner
    interpolation: {
      escapeValue: false 
    }
  });

export default i18n;