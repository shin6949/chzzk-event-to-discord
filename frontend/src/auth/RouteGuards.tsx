import {useTranslation} from 'react-i18next';
import {Navigate, Outlet, useLocation} from 'react-router-dom';
import {useAuth} from './AuthContext';

/**
 * `RequireAuth`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function RequireAuth() {
  const {t} = useTranslation();
  const {session, loading} = useAuth();
  const location = useLocation();

  if (loading) {
    return <div className="text-center py-5">{t('common.checkingSession')}</div>;
  }

  if (!session) {
    return <Navigate to="/login" replace state={{from: location.pathname}} />;
  }

  return <Outlet />;
}

/**
 * `RequireAdmin`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function RequireAdmin() {
  const {t} = useTranslation();
  const {session, loading} = useAuth();

  if (loading) {
    return <div className="text-center py-5">{t('common.checkingSession')}</div>;
  }

  if (!session) {
    return <Navigate to="/login" replace />;
  }

  if (session.role !== 'ADMIN') {
    return <Navigate to="/subscriptions" replace />;
  }

  return <Outlet />;
}
