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

type JsonBody = string | number | boolean | null | JsonBody[] | { [key: string]: JsonBody };
type RequestOptions<B = JsonBody> = Omit<RequestInit, 'body'> & { body?: B };

const REFRESH_PATH = '/auth/refresh';

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
    const fields = body as { message?: unknown; error?: unknown; detail?: unknown };
    for (const value of [fields.message, fields.error, fields.detail]) {
      if (typeof value === 'string' && value.trim().length > 0) {
        return value.trim();
      }
    }
  }

  return fallback;
}

function shouldAttemptRefresh(path: string, response: Response, retryOnUnauthorized: boolean): boolean {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  return retryOnUnauthorized && response.status === 401 && normalizedPath !== REFRESH_PATH;
}

async function refreshAuth(): Promise<boolean> {
  const headers = new Headers();
  headers.set('Accept', 'application/json');

  const response = await fetch(buildApiUrl(REFRESH_PATH), {
    method: 'POST',
    headers,
    credentials: 'include',
  });

  return response.ok;
}

async function request<T, B = JsonBody>(
  path: string,
  init: RequestOptions<B> = {},
  retryOnUnauthorized = true,
): Promise<T> {
  const method = init.method ?? 'GET';
  const headers = new Headers(init.headers ?? {});
  headers.set('Accept', 'application/json');
  if (init.body !== undefined && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(buildApiUrl(path), {
    ...init,
    method,
    headers,
    credentials: 'include',
    body: init.body === undefined ? undefined : JSON.stringify(init.body),
  });

  const body = await parseResponseBody(response);

  if (!response.ok) {
    if (shouldAttemptRefresh(path, response, retryOnUnauthorized) && await refreshAuth()) {
      return request<T, B>(path, init, false);
    }

    const message = extractErrorMessage(body, `Request failed with status ${response.status}`);
    throw new ApiError(response.status, message, body);
  }

  return body as T;
}

async function formRequest<T>(
  path: string,
  method: 'POST' | 'PUT',
  body: FormData,
  retryOnUnauthorized = true,
): Promise<T> {
  const headers = new Headers();
  headers.set('Accept', 'application/json');

  const response = await fetch(buildApiUrl(path), {
    method,
    headers,
    credentials: 'include',
    body,
  });

  const responseBody = await parseResponseBody(response);

  if (!response.ok) {
    if (shouldAttemptRefresh(path, response, retryOnUnauthorized) && await refreshAuth()) {
      return formRequest<T>(path, method, body, false);
    }

    const message = extractErrorMessage(responseBody, `Request failed with status ${response.status}`);
    throw new ApiError(response.status, message, responseBody);
  }

  return responseBody as T;
}

export async function apiGet<T>(path: string): Promise<T> {
  return request<T>(path, { method: 'GET' });
}

export async function apiPost<T, B extends JsonBody = JsonBody>(path: string, body?: B): Promise<T> {
  return request<T>(path, {
    method: 'POST',
    body,
  });
}

export async function apiPut<T, B extends JsonBody = JsonBody>(path: string, body: B): Promise<T> {
  return request<T>(path, {
    method: 'PUT',
    body,
  });
}

export async function apiDelete<T = void>(path: string): Promise<T> {
  return request<T>(path, { method: 'DELETE' });
}

export async function apiPostForm<T>(path: string, body: FormData): Promise<T> {
  return formRequest<T>(path, 'POST', body);
}

export async function apiPutForm<T>(path: string, body: FormData): Promise<T> {
  return formRequest<T>(path, 'PUT', body);
}
