import {hasAsciiControlCharacter, isSafeApiPath} from '../security/urls';

const DEFAULT_API_BASE_URL = '/api/v1';
const FALLBACK_ORIGIN = 'http://localhost';

/**
 * `currentOrigin`는 데이터를 요청하거나 조회합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
 */
function currentOrigin(): string {
  if (typeof window !== 'undefined' && window.location?.origin) {
    return window.location.origin;
  }
  return FALLBACK_ORIGIN;
}

/**
 * `trimTrailingSlash`는 값을 변환하거나 생성합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
 */
function trimTrailingSlash(value: string): string {
  return value.endsWith('/') ? value.slice(0, -1) : value;
}

/**
 * `rawPathFromApiBase`는 값을 변환하거나 생성합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
 */
function rawPathFromApiBase(value: string): string {
  if (/^[a-z][a-z0-9+.-]*:\/\//i.test(value)) {
    return value.replace(/^[a-z][a-z0-9+.-]*:\/\/[^/?#]*/i, '') || '/';
  }
  return value;
}

/**
 * `normalizeApiBaseUrl`는 관련 프론트엔드 기능을 수행합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function normalizeApiBaseUrl(value?: string): string {
  const trimmed = value?.trim();
  if (!trimmed) {
    return DEFAULT_API_BASE_URL;
  }

  if (trimmed.startsWith('//') || hasAsciiControlCharacter(trimmed)) {
    throw new Error('VITE_API_BASE_URL must be a same-origin API path');
  }
  const rawPath = rawPathFromApiBase(trimmed).split(/[?#]/, 1)[0] || '/';
  const rawPathForValidation = rawPath === '/' ? rawPath : trimTrailingSlash(rawPath);
  if (!isSafeApiPath(rawPathForValidation)) {
    throw new Error('VITE_API_BASE_URL contains an unsafe path');
  }

  const baseOrigin = currentOrigin();
  let url: URL;
  try {
    url = new URL(trimmed, baseOrigin);
  } catch {
    throw new Error('VITE_API_BASE_URL must be a valid same-origin API path');
  }

  if (url.origin !== baseOrigin || url.search || url.hash) {
    throw new Error('VITE_API_BASE_URL must be same-origin and must not include query or fragment');
  }

  const normalizedPath = trimTrailingSlash(url.pathname);
  if (!isSafeApiPath(normalizedPath)) {
    throw new Error('VITE_API_BASE_URL contains an unsafe path');
  }
  return normalizedPath || DEFAULT_API_BASE_URL;
}

export const env = Object.freeze({
  apiBaseUrl: normalizeApiBaseUrl(import.meta.env.VITE_API_BASE_URL),
});
