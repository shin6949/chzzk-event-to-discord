const DEFAULT_API_BASE_URL = 'http://localhost:8080/api/v1';

export function normalizeApiBaseUrl(value?: string): string {
  if (!value) {
    return DEFAULT_API_BASE_URL;
  }

  return value.endsWith('/') ? value.slice(0, -1) : value;
}

export const env = Object.freeze({
  apiBaseUrl: normalizeApiBaseUrl(import.meta.env.VITE_API_BASE_URL),
});
