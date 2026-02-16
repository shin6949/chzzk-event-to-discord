import { describe, expect, it } from 'vitest';
import { normalizeApiBaseUrl } from './env';

describe('normalizeApiBaseUrl', () => {
  it('returns default API base URL when value is missing', () => {
    expect(normalizeApiBaseUrl()).toBe('http://localhost:8080/api/v1');
  });

  it('trims trailing slash when present', () => {
    expect(normalizeApiBaseUrl('https://api.example.com/')).toBe('https://api.example.com');
  });

  it('keeps value unchanged when no trailing slash is present', () => {
    expect(normalizeApiBaseUrl('https://api.example.com')).toBe('https://api.example.com');
  });
});
