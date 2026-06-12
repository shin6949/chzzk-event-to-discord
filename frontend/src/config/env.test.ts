import {describe, expect, it} from 'vitest';
import {normalizeApiBaseUrl} from './env';

describe('normalizeApiBaseUrl', () => {
  it('returns default API base URL when value is missing', () => {
    expect(normalizeApiBaseUrl()).toBe('/api/v1');
  });

  it('trims whitespace and trailing slash when present', () => {
    expect(normalizeApiBaseUrl(' /api/v1/ ')).toBe('/api/v1');
  });

  it('normalizes same-origin absolute URLs into paths', () => {
    expect(normalizeApiBaseUrl('http://localhost/api/v1/')).toBe('/api/v1');
  });

  it('rejects cross-origin API bases', () => {
    expect(() => normalizeApiBaseUrl('https://api.example.com')).toThrow(/same-origin/i);
    expect(() => normalizeApiBaseUrl('//api.example.com/api/v1')).toThrow(/same-origin/i);
  });

  it('rejects query, fragment, and unsafe path traversal API bases', () => {
    expect(() => normalizeApiBaseUrl('/api/v1?target=/admin')).toThrow(/query or fragment/i);
    expect(() => normalizeApiBaseUrl('/api/v1#fragment')).toThrow(/query or fragment/i);
    expect(() => normalizeApiBaseUrl('/api/v1/../admin')).toThrow(/unsafe path/i);
    expect(() => normalizeApiBaseUrl('/api/v1/%2e%2e/admin')).toThrow(/unsafe path/i);
    expect(() => normalizeApiBaseUrl('/api/v1/%2fadmin')).toThrow(/unsafe path/i);
  });
});
