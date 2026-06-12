import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Link} from 'react-router-dom';
import {PagePlaceholder} from '../../components/PagePlaceholder';
import {useAuth} from '../../auth/AuthContext';
import {apiGet, ApiError} from '../../api/client';
import {isTrustedChzzkAuthorizationUrl} from '../../security/urls';

/**
 * `ChzzkLoginPage`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function ChzzkLoginPage() {
  const {t} = useTranslation();
  const {session, loading, reloadSession} = useAuth();
  const [isSubmitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  if (session) {
    return (
      <PagePlaceholder title={t('auth.alreadyLoggedInTitle')} description={t('auth.alreadyLoggedInDescription')}>
        <div className="d-flex flex-wrap gap-2">
          <button type="button" className="btn btn-outline-secondary" onClick={() => void reloadSession()}>
            {t('auth.refreshSession')}
          </button>
          <Link className="btn btn-primary" to="/subscriptions">
            {t('auth.goToSubscriptions')}
          </Link>
        </div>
      </PagePlaceholder>
    );
  }

  if (loading) {
    return <div className="text-center py-5">{t('common.checkingAuthentication')}</div>;
  }

  /**
   * `handleStartLogin`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
   */
  async function handleStartLogin() {
    setSubmitting(true);
    setError('');

    try {
      const response = await apiGet<{ authorizationUrl: string }>('/auth/chzzk/login');
      if (!isTrustedChzzkAuthorizationUrl(response.authorizationUrl)) {
        setError(t('auth.untrustedAuthorizationUrl'));
        return;
      }
      window.location.href = response.authorizationUrl;
    } catch (err) {
      const message = err instanceof ApiError ? err.message : t('auth.unableToStartLogin');
      setError(message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <PagePlaceholder title={t('auth.loginTitle')} description={t('auth.loginDescription')}>
      <p className="brand-relationship-note small rounded px-3 py-2 mb-3">{t('auth.brandNotice')}</p>
      {error ? <div className="alert alert-danger">{error}</div> : null}
      <div className="d-flex flex-wrap gap-2">
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => void handleStartLogin()}
          disabled={isSubmitting}
        >
          {t('nav.loginWithChzzk')}
        </button>
      </div>
    </PagePlaceholder>
  );
}
