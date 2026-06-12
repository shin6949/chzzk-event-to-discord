import type {ReactNode} from 'react';
import {createContext, useCallback, useContext, useEffect, useMemo, useState} from 'react';
import {ApiError, apiGet, apiPost} from '../api/client';
import type {AppSession} from './session';
import {clearSession, getSession, setSession} from './session';

/**
 * `AuthContextValue`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
type AuthContextValue = {
  session: AppSession | null;
  loading: boolean;
  reloadSession: () => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

/**
 * `AuthProvider`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
export function AuthProvider({children}: { children: ReactNode }) {
  const [session, setSessionState] = useState<AppSession | null>(() => getSession());
  const [loading, setLoading] = useState(true);

  const reloadSession = useCallback(async () => {
    setLoading(true);
    try {
      const nextSession = await apiGet<AppSession>('/auth/me');
      setSessionState(nextSession);
      setSession(nextSession);
      return;
    } catch (error) {
      if (error instanceof ApiError && error.status === 401) {
        setSessionState(null);
        clearSession();
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void reloadSession();
  }, [reloadSession]);

  const logout = useCallback(async () => {
    try {
      await apiPost('/auth/logout');
    } catch (error) {
      // Keep client-side sign-out even if server-side logout fails.
      console.error(error);
    } finally {
      setSessionState(null);
      clearSession();
    }
  }, []);

  const value = useMemo(
    () => ({
      session,
      loading,
      reloadSession,
      logout,
    }),
    [session, loading, reloadSession, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

/**
 * `useAuth`는 React 상태와 부수 효과를 캡슐화합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}
