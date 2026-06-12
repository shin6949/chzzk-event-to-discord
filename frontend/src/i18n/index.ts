import i18n from 'i18next';
import {initReactI18next} from 'react-i18next';
import {resources} from './resources';

export const LANGUAGE_STORAGE_KEY = 'streaming-alert-service-ui-language';
export const DEFAULT_LANGUAGE = 'ko';
export const SUPPORTED_LANGUAGES = ['ko', 'en'] as const;

/**
 * `SupportedLanguage`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export type SupportedLanguage = (typeof SUPPORTED_LANGUAGES)[number];

/**
 * `isSupportedLanguage`는 조건 충족 여부를 판단합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function isSupportedLanguage(value: string | null | undefined): value is SupportedLanguage {
  return Boolean(value && SUPPORTED_LANGUAGES.includes(value as SupportedLanguage));
}

/**
 * `getStoredLanguage`는 데이터를 요청하거나 조회합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
function getStoredLanguage(): SupportedLanguage {
  if (typeof window === 'undefined') {
    return DEFAULT_LANGUAGE;
  }

  const storedLanguage = window.localStorage.getItem(LANGUAGE_STORAGE_KEY);
  return isSupportedLanguage(storedLanguage) ? storedLanguage : DEFAULT_LANGUAGE;
}

void i18n.use(initReactI18next).init({
  resources,
  lng: getStoredLanguage(),
  fallbackLng: 'en',
  interpolation: {
    escapeValue: false,
  },
});

i18n.on('languageChanged', (language) => {
  if (typeof document !== 'undefined' && isSupportedLanguage(language)) {
    document.documentElement.lang = language;
  }
  if (typeof window !== 'undefined' && isSupportedLanguage(language)) {
    window.localStorage.setItem(LANGUAGE_STORAGE_KEY, language);
  }
});

if (typeof document !== 'undefined') {
  document.documentElement.lang = i18n.language;
}

export default i18n;
