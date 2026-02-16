import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ApiError, apiGet } from './client';

function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  });
}

describe('api client', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('normalizes request paths and applies default fetch options', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request) => {
      const url = typeof input === 'string' ? input : input.toString();
      return jsonResponse(200, { url });
    });

    await apiGet<{ url: string }>('/auth/me');
    await apiGet<{ url: string }>('auth/me');

    expect(fetchSpy).toHaveBeenNthCalledWith(
      1,
      'http://localhost:8080/api/v1/auth/me',
      expect.objectContaining({ method: 'GET', credentials: 'include' }),
    );
    expect(fetchSpy).toHaveBeenNthCalledWith(
      2,
      'http://localhost:8080/api/v1/auth/me',
      expect.objectContaining({ method: 'GET', credentials: 'include' }),
    );

    const firstCallInit = fetchSpy.mock.calls[0]?.[1];
    const headers = new Headers(firstCallInit?.headers);
    expect(headers.get('Accept')).toBe('application/json');
  });

  it('returns typed JSON payloads on success', async () => {
    type AuthMeResponse = {
      channelId: string;
      role: 'USER' | 'ADMIN';
    };

    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(200, { channelId: 'channel-1', role: 'USER' }));

    const result = await apiGet<AuthMeResponse>('/auth/me');

    expect(result.channelId).toBe('channel-1');
    expect(result.role).toBe('USER');
  });

  it('raises typed ApiError for 401 responses', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(401, { message: 'Unauthorized' }));

    let error: unknown;
    try {
      await apiGet('/auth/me');
    } catch (caught) {
      error = caught;
    }

    expect(error).toBeInstanceOf(ApiError);
    const apiError = error as ApiError;
    expect(apiError.status).toBe(401);
    expect(apiError.message).toBe('Unauthorized');
    expect(apiError.body).toEqual({ message: 'Unauthorized' });
  });
});
