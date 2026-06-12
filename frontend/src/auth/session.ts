/**
 * `AppRole`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export type AppRole = 'USER' | 'ADMIN';

/**
 * `AppSession`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export type AppSession = {
  channelId: string;
  role: AppRole;
  channelName?: string | null;
  profileUrl?: string | null;
};

const SESSION_STORAGE_KEY = 'streaming-alert-service:session';

/**
 * `isValidRole`는 조건 충족 여부를 판단합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
function isValidRole(value: unknown): value is AppRole {
  return value === 'USER' || value === 'ADMIN';
}

/**
 * `parseSession`는 값을 변환하거나 생성합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
function parseSession(value: string | null): AppSession | null {
  if (!value) {
    return null;
  }

  try {
    const parsed = JSON.parse(value) as Partial<AppSession>;
    if (typeof parsed.channelId !== 'string' || !isValidRole(parsed.role)) {
      return null;
    }
    return {
      channelId: parsed.channelId,
      role: parsed.role,
      channelName: typeof parsed.channelName === 'string' ? parsed.channelName : undefined,
      profileUrl: typeof parsed.profileUrl === 'string' ? parsed.profileUrl : undefined,
    };
  } catch {
    return null;
  }
}

/**
 * `getSession`는 데이터를 요청하거나 조회합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function getSession(): AppSession | null {
  if (typeof window === 'undefined') {
    return null;
  }
  return parseSession(window.localStorage.getItem(SESSION_STORAGE_KEY));
}

/**
 * `setSession`는 사용자 동작 또는 상태 변경을 처리합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function setSession(session: AppSession): void {
  if (typeof window === 'undefined') {
    return;
  }
  window.localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session));
}

/**
 * `setMockSession`는 사용자 동작 또는 상태 변경을 처리합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function setMockSession(role: AppRole): void {
  setSession({
    channelId: role === 'ADMIN' ? 'mock-admin-channel' : 'mock-user-channel',
    role,
    channelName: role === 'ADMIN' ? 'Mock Admin' : 'Mock User',
    profileUrl: null,
  });
}

/**
 * `clearSession`는 사용자 동작 또는 상태 변경을 처리합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function clearSession(): void {
  if (typeof window === 'undefined') {
    return;
  }
  window.localStorage.removeItem(SESSION_STORAGE_KEY);
}
