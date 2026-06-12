import {useTranslation} from 'react-i18next';
import {useNavigate, useSearchParams} from 'react-router-dom';
import type {AppRole} from '../../auth/session';
import {setMockSession} from '../../auth/session';
import {PagePlaceholder} from '../../components/PagePlaceholder';

/**
 * `parseMockRole`는 값을 변환하거나 생성합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
function parseMockRole(value: string | null): AppRole | null {
  if (value === 'USER' || value === 'ADMIN') {
    return value;
  }
  return null;
}

/**
 * `ChzzkCallbackPage`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function ChzzkCallbackPage() {
  const {t} = useTranslation();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const mockRole = parseMockRole(searchParams.get('mockRole'));

  /**
   * `handleCompleteSignIn`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
   */
  function handleCompleteSignIn() {
      if (!mockRole) {
        return;
      }
      setMockSession(mockRole);
      navigate('/subscriptions');
    }

  return (
    <PagePlaceholder
      title={t('auth.callbackTitle')}
      description={t('auth.callbackDescription')}
    >
      {mockRole ? (
        <div className="d-flex align-items-center gap-2">
          <span className="badge text-bg-info">{t('auth.mockRole', {role: mockRole})}</span>
          <button type="button" className="btn btn-primary" onClick={handleCompleteSignIn}>
            {t('auth.completeSignIn')}
          </button>
        </div>
      ) : (
        <p className="mb-0 text-muted">
          {t('auth.callbackMissing')}
        </p>
      )}
    </PagePlaceholder>
  );
}
