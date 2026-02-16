import { createContext, ReactNode, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { ApiError, apiGet, apiPost } from '../api/client';
import { AppSession, clearSession, setSession } from './session';

type AuthContextValue = {
  session: AppSession | null;
  loading: boolean;
  reloadSession: () => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSessionState] = useState<AppSession | null>(null);
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
      } else {
        setSessionState(null);
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

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}
