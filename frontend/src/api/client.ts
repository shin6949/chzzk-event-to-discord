import {env} from '../config/env';
import {isSafeApiPath} from '../security/urls';

/**
 * `ApiError`는 관련 상태와 동작을 캡슐화합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
export class ApiError extends Error {
  status: number;
  body?: unknown;

  /**
   * `constructor`는 인스턴스 초기 상태를 구성합니다.
   *
   * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
   */
  constructor(status: number, message: string, body?: unknown) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
  }
}

const MAX_ERROR_MESSAGE_LENGTH = 500;

/**
 * `buildApiUrl`는 값을 변환하거나 생성합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function buildApiUrl(path: string): string {
  if (!isSafeApiPath(path)) {
    throw new ApiError(0, 'Unsafe API path rejected');
  }
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  return `${env.apiBaseUrl}${normalizedPath}`;
}

/**
 * `JsonBody`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
type JsonBody = string | number | boolean | null | JsonBody[] | { [key: string]: JsonBody };
/**
 * `RequestOptions`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
type RequestOptions<B = JsonBody> = Omit<RequestInit, 'body'> & { body?: B };

const REFRESH_PATH = '/auth/refresh';
const CSRF_PATH = '/auth/csrf';
const CSRF_COOKIE_NAME = 'XSRF-TOKEN';
const CSRF_HEADER_NAME = 'X-XSRF-TOKEN';

let csrfTokenRequest: Promise<string | null> | null = null;

/**
 * `isJsonResponse`는 조건 충족 여부를 판단합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
function isJsonResponse(response: Response): boolean {
  return (response.headers.get('content-type') ?? '').includes('application/json');
}

/**
 * `parseResponseBody`는 값을 변환하거나 생성합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
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

/**
 * `extractErrorMessage`는 관련 프론트엔드 기능을 수행합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
function extractErrorMessage(body: unknown, fallback: string): string {
  let message = fallback;
  if (typeof body === 'string' && body.trim().length > 0) {
    message = body.trim();
  } else if (body && typeof body === 'object') {
    const fields = body as { message?: unknown; error?: unknown; detail?: unknown };
    for (const value of [fields.message, fields.error, fields.detail]) {
      if (typeof value === 'string' && value.trim().length > 0) {
        message = value.trim();
        break;
      }
    }
  }

  return message.length <= MAX_ERROR_MESSAGE_LENGTH ?
    message :
    `${message.slice(0, MAX_ERROR_MESSAGE_LENGTH)}...`;
}

/**
 * `shouldAttemptRefresh`는 조건 충족 여부를 판단합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
 */
function shouldAttemptRefresh(path: string, response: Response, retryOnUnauthorized: boolean): boolean {
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  return retryOnUnauthorized && response.status === 401 && normalizedPath !== REFRESH_PATH;
}

/**
 * `isUnsafeMethod`는 조건 충족 여부를 판단합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
 */
function isUnsafeMethod(method: string): boolean {
  return !['GET', 'HEAD', 'OPTIONS', 'TRACE'].includes(method.toUpperCase());
}

/**
 * `readCookie`는 데이터를 요청하거나 조회합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
 */
function readCookie(name: string): string | null {
  if (typeof document === 'undefined') {
    return null;
  }

  const prefix = `${name}=`;
  const cookie = document.cookie
    .split(';')
    .map((value) => value.trim())
    .find((value) => value.startsWith(prefix));
  if (!cookie) {
    return null;
  }
  return decodeURIComponent(cookie.slice(prefix.length));
}

/**
 * `ensureCsrfToken`는 사용자 동작 또는 상태 변경을 처리합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
 */
async function ensureCsrfToken(): Promise<string | null> {
  const existingToken = readCookie(CSRF_COOKIE_NAME);
  if (existingToken) {
    return existingToken;
  }
  if (csrfTokenRequest) {
    return csrfTokenRequest;
  }

  csrfTokenRequest = fetch(buildApiUrl(CSRF_PATH), {
    method: 'GET',
    headers: {Accept: 'application/json'},
    credentials: 'include',
  })
    .then(async (response) => {
      if (!response.ok) {
        return null;
      }
      const body = await parseResponseBody(response);
      if (body && typeof body === 'object') {
        const token = (body as { token?: unknown }).token;
        if (typeof token === 'string' && token.length > 0) {
          return token;
        }
      }
      return readCookie(CSRF_COOKIE_NAME);
    })
    .finally(() => {
      csrfTokenRequest = null;
    });

  return csrfTokenRequest;
}

/**
 * `appendCsrfHeader`는 사용자 동작 또는 상태 변경을 처리합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
 */
async function appendCsrfHeader(headers: Headers, method: string): Promise<void> {
  if (!isUnsafeMethod(method) || headers.has(CSRF_HEADER_NAME)) {
    return;
  }
  const csrfToken = await ensureCsrfToken();
  if (csrfToken) {
    headers.set(CSRF_HEADER_NAME, csrfToken);
  }
}

/**
 * `refreshAuth`는 사용자 동작 또는 상태 변경을 처리합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 3c42b97.
 */
async function refreshAuth(): Promise<boolean> {
  const headers = new Headers();
  headers.set('Accept', 'application/json');
  await appendCsrfHeader(headers, 'POST');

  const response = await fetch(buildApiUrl(REFRESH_PATH), {
    method: 'POST',
    headers,
    credentials: 'include',
  });

  return response.ok;
}

/**
 * `request`는 데이터를 요청하거나 조회합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 HEAD blame 커밋 3c42b97.
 */
async function request<T, B = JsonBody>(
  path: string,
  init: RequestOptions<B> = {},
  retryOnUnauthorized = true,
): Promise<T> {
  const method = init.method ?? 'GET';
  const url = buildApiUrl(path);
  const headers = new Headers(init.headers ?? {});
  headers.set('Accept', 'application/json');
  if (init.body !== undefined && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  await appendCsrfHeader(headers, method);

  const response = await fetch(url, {
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

/**
 * `formRequest`는 관련 프론트엔드 기능을 수행합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 HEAD blame 커밋 3c42b97.
 */
async function formRequest<T>(
  path: string,
  method: 'POST' | 'PUT',
  body: FormData,
  retryOnUnauthorized = true,
): Promise<T> {
  const url = buildApiUrl(path);
  const headers = new Headers();
  headers.set('Accept', 'application/json');
  await appendCsrfHeader(headers, method);

  const response = await fetch(url, {
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

/**
 * `apiGet`는 데이터를 요청하거나 조회합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 HEAD blame 커밋 ebb9e17.
 */
export async function apiGet<T>(path: string): Promise<T> {
  return request<T>(path, {method: 'GET'});
}

/**
 * `apiPost`는 데이터를 요청하거나 조회합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 HEAD blame 커밋 ebb9e17.
 */
export async function apiPost<T, B extends JsonBody = JsonBody>(path: string, body?: B): Promise<T> {
  return request<T>(path, {
    method: 'POST',
    body,
  });
}

/**
 * `apiPut`는 데이터를 요청하거나 조회합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 HEAD blame 커밋 ebb9e17.
 */
export async function apiPut<T, B extends JsonBody = JsonBody>(path: string, body: B): Promise<T> {
  return request<T>(path, {
    method: 'PUT',
    body,
  });
}

/**
 * `apiDelete`는 데이터를 요청하거나 조회합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 HEAD blame 커밋 ebb9e17.
 */
export async function apiDelete<T = void>(path: string): Promise<T> {
  return request<T>(path, {method: 'DELETE'});
}

/**
 * `apiPostForm`는 데이터를 요청하거나 조회합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 HEAD blame 커밋 3c42b97.
 */
export async function apiPostForm<T>(path: string, body: FormData): Promise<T> {
  return formRequest<T>(path, 'POST', body);
}

/**
 * `apiPutForm`는 데이터를 요청하거나 조회합니다.
 *
 * Git 이력: 생성 2026-06-04 10:40:23 +0900, 작성자 shin6949, 작성 버전 unreleased after Ver.0.1.4, 근거 HEAD blame 커밋 3c42b97.
 */
export async function apiPutForm<T>(path: string, body: FormData): Promise<T> {
  return formRequest<T>(path, 'PUT', body);
}
