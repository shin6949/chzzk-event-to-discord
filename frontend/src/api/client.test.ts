import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {ApiError, apiGet, apiPost, buildApiUrl} from './client';

/**
 * `jsonResponse`는 관련 프론트엔드 기능을 수행합니다.
 *
 * Git 이력: 생성 2026-02-17 06:57:49 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 d252252.
 */
function jsonResponse(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: {'Content-Type': 'application/json'},
  });
}

function discordWebhookUrl(id: string, token: string) {
  return `https://discord.com/api/webhooks/${id}/${token}`;
}

describe('api client', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    document.cookie = 'XSRF-TOKEN=; Max-Age=0; path=/';
  });

  afterEach(() => {
    vi.restoreAllMocks();
    document.cookie = 'XSRF-TOKEN=; Max-Age=0; path=/';
  });

  it('normalizes request paths and applies default fetch options', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockImplementation(async (input: string | URL | Request) => {
      const url = typeof input === 'string' ? input : input.toString();
      return jsonResponse(200, {url});
    });

    await apiGet<{ url: string }>('/auth/me');
    await apiGet<{ url: string }>('auth/me');

    expect(fetchSpy).toHaveBeenNthCalledWith(
      1,
      '/api/v1/auth/me',
      expect.objectContaining({method: 'GET', credentials: 'include'}),
    );
    expect(fetchSpy).toHaveBeenNthCalledWith(
      2,
      '/api/v1/auth/me',
      expect.objectContaining({method: 'GET', credentials: 'include'}),
    );

    const firstCallInit = fetchSpy.mock.calls[0]?.[1];
    const headers = new Headers(firstCallInit?.headers);
    expect(headers.get('Accept')).toBe('application/json');
  });

  it('builds same-origin API URLs from safe paths', () => {
    expect(buildApiUrl('/auth/me')).toBe('/api/v1/auth/me');
    expect(buildApiUrl('auth/me')).toBe('/api/v1/auth/me');
    expect(buildApiUrl('/discord/webhooks?size=100')).toBe('/api/v1/discord/webhooks?size=100');
  });

  it.each([
    'https://evil.example/api',
    '//evil.example/api',
    '/../admin',
    '/%2e%2e/admin',
    '/%2fadmin',
    '/admin#token',
    '/admin\\settings',
  ])('rejects unsafe API paths before fetch: %s', async (path) => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(200, {ok: true}));

    await expect(apiGet(path)).rejects.toMatchObject({
      status: 0,
      message: 'Unsafe API path rejected',
    });
    expect(fetchSpy).not.toHaveBeenCalled();
  });

  it('rejects unsafe mutating API paths before attempting CSRF lookup', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(200, {ok: true}));

    await expect(apiPost('https://evil.example/steal', {alias: 'Main'})).rejects.toMatchObject({
      status: 0,
      message: 'Unsafe API path rejected',
    });
    expect(fetchSpy).not.toHaveBeenCalled();
  });

  it('returns typed JSON payloads on success', async () => {
    /**
     * `AuthMeResponse`는 데이터 계약을 정의합니다.
     *
     * Git 이력: 생성 2026-02-17 06:57:49 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 d252252.
     */
    type AuthMeResponse = {
      channelId: string;
      role: 'USER' | 'ADMIN';
    };

    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(200, {channelId: 'channel-1', role: 'USER'}));

    const result = await apiGet<AuthMeResponse>('/auth/me');

    expect(result.channelId).toBe('channel-1');
    expect(result.role).toBe('USER');
  });

  it('raises typed ApiError for 401 responses', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(401, {message: 'Unauthorized'}));

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
    expect(apiError.body).toEqual({message: 'Unauthorized'});
  });

  it('refreshes auth once and retries when a request returns 401', async () => {
    document.cookie = 'XSRF-TOKEN=csrf-refresh-token; path=/';
    vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(jsonResponse(401, {message: 'Expired'}))
      .mockResolvedValueOnce(jsonResponse(200, {channelId: 'channel-1', role: 'USER'}))
      .mockResolvedValueOnce(jsonResponse(200, {channelId: 'channel-1', role: 'USER'}));

    const result = await apiGet<{ channelId: string }>('/auth/me');

    expect(result.channelId).toBe('channel-1');
    expect(globalThis.fetch).toHaveBeenNthCalledWith(
      2,
      '/api/v1/auth/refresh',
      expect.objectContaining({method: 'POST', credentials: 'include'}),
    );
    const refreshHeaders = new Headers(vi.mocked(globalThis.fetch).mock.calls[1]?.[1]?.headers);
    expect(refreshHeaders.get('X-XSRF-TOKEN')).toBe('csrf-refresh-token');
  });

  it('adds CSRF header to unsafe JSON requests', async () => {
    document.cookie = 'XSRF-TOKEN=csrf-json-token; path=/';
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(200, {ok: true}));

    await apiPost('/discord/webhooks', {alias: 'Main', url: discordWebhookUrl('1', 'token')});

    const headers = new Headers(fetchSpy.mock.calls[0]?.[1]?.headers);
    expect(headers.get('X-XSRF-TOKEN')).toBe('csrf-json-token');
  });

  it('loads a CSRF token when unsafe JSON requests have no readable cookie', async () => {
    const fetchSpy = vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(jsonResponse(200, {token: 'csrf-from-endpoint'}))
      .mockResolvedValueOnce(jsonResponse(200, {ok: true}));

    await apiPost('/discord/webhooks', {alias: 'Main', url: discordWebhookUrl('1', 'token')});

    expect(fetchSpy).toHaveBeenNthCalledWith(
      1,
      '/api/v1/auth/csrf',
      expect.objectContaining({method: 'GET', credentials: 'include'}),
    );
    const requestHeaders = new Headers(fetchSpy.mock.calls[1]?.[1]?.headers);
    expect(requestHeaders.get('X-XSRF-TOKEN')).toBe('csrf-from-endpoint');
  });

  it('truncates long error messages before exposing them to the UI', async () => {
    const longMessage = 'x'.repeat(700);
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(jsonResponse(400, {message: longMessage}));

    await expect(apiGet('/too-long')).rejects.toMatchObject({
      status: 400,
      message: `${'x'.repeat(500)}...`,
    });
  });

  it('uses error and detail fields when message is not present', async () => {
    vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(jsonResponse(404, {error: 'Not Found'}))
      .mockResolvedValueOnce(jsonResponse(403, {detail: 'Access denied'}));

    await expect(apiGet('/missing')).rejects.toMatchObject({
      status: 404,
      message: 'Not Found',
    });
    await expect(apiGet('/denied')).rejects.toMatchObject({
      status: 403,
      message: 'Access denied',
    });
  });
});
