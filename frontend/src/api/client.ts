import { env } from '../config/env';

export class ApiError extends Error {
  status: number;
  body?: unknown;

  constructor(status: number, message: string, body?: unknown) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
  }
}

function buildApiUrl(path: string): string {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  return `${env.apiBaseUrl}${normalizedPath}`;
}

function isJsonResponse(response: Response): boolean {
  return (response.headers.get('content-type') ?? '').includes('application/json');
}

async function parseResponseBody(response: Response): Promise<unknown | null> {
  const text = await response.text().catch(() => '');
  if (!text) {
    return null;
  }

  if (!isJsonResponse(response)) {
    return text;
  }

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function extractErrorMessage(body: unknown, fallback: string): string {
  if (typeof body === 'string' && body.trim().length > 0) {
    return body.trim();
  }

  if (body && typeof body === 'object') {
    const value = (body as { message?: unknown }).message;
    if (typeof value === 'string' && value.trim().length > 0) {
      return value.trim();
    }
  }

  return fallback;
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const method = init.method ?? 'GET';
  const headers = new Headers(init.headers ?? {});
  headers.set('Accept', 'application/json');

  const response = await fetch(buildApiUrl(path), {
    ...init,
    method,
    headers,
    credentials: 'include',
    body: init.body === undefined ? undefined : JSON.stringify(init.body),
  });

  const body = await parseResponseBody(response);

  if (!response.ok) {
    const message = extractErrorMessage(body, `Request failed with status ${response.status}`);
    throw new ApiError(response.status, message, body);
  }

  return body as T;
}

export async function apiGet<T>(path: string): Promise<T> {
  return request<T>(path, { method: 'GET' });
}

export async function apiPost<T, B = unknown>(path: string, body?: B): Promise<T> {
  return request<T>(path, {
    method: 'POST',
    body,
  });
}

export async function apiPut<T, B = unknown>(path: string, body: B): Promise<T> {
  return request<T>(path, {
    method: 'PUT',
    body,
  });
}

export async function apiDelete<T = void>(path: string): Promise<T> {
  return request<T>(path, { method: 'DELETE' });
}
