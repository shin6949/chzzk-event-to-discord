export type AppRole = 'USER' | 'ADMIN';

export type AppSession = {
  channelId: string;
  role: AppRole;
};

const SESSION_STORAGE_KEY = 'chzzk:event-to-discord:session';

function isValidRole(value: unknown): value is AppRole {
  return value === 'USER' || value === 'ADMIN';
}

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
    };
  } catch {
    return null;
  }
}

export function getSession(): AppSession | null {
  if (typeof window === 'undefined') {
    return null;
  }
  return parseSession(window.localStorage.getItem(SESSION_STORAGE_KEY));
}

export function setSession(session: AppSession): void {
  if (typeof window === 'undefined') {
    return;
  }
  window.localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session));
}

export function setMockSession(role: AppRole): void {
  setSession({
    channelId: role === 'ADMIN' ? 'mock-admin-channel' : 'mock-user-channel',
    role,
  });
}

export function clearSession(): void {
  if (typeof window === 'undefined') {
    return;
  }
  window.localStorage.removeItem(SESSION_STORAGE_KEY);
}
