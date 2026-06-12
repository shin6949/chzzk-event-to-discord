import {useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {NavLink, Outlet, useLocation, useNavigate} from 'react-router-dom';
import {useAuth} from '../auth/AuthContext';
import {FirstVisitTutorialModal} from '../components/FirstVisitTutorialModal';
import type {SupportedLanguage} from '../i18n';
import {safeImageSrc} from '../security/urls';
import {dismissFirstVisitTutorial, hasDismissedFirstVisitTutorial} from '../tutorial/firstVisitTutorial';

/**
 * `TutorialOutletContext`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
 */
export type TutorialOutletContext = {
  showTutorial: () => void;
};

/**
 * `navLinkClass`는 관련 프론트엔드 기능을 수행합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
function navLinkClass({isActive}: { isActive: boolean }) {
  return `nav-link${isActive ? ' active fw-semibold' : ''}`;
}

/**
 * `formatChannelLabel`는 값을 변환하거나 생성합니다.
 *
 * Git 이력: 생성 2026-02-16 23:22:45 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 ebb9e17.
 */
function formatChannelLabel(channelId: string | null | undefined, fallback: string): string {
  const truncated = channelId?.trim() ?? '';
  if (!truncated) {
    return fallback;
  }
  if (truncated.length <= 20) {
    return truncated;
  }
  return `${truncated.slice(0, 20)}…`;
}

/**
 * `MainLayout`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function MainLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const {t, i18n} = useTranslation();
  const {session, loading, logout} = useAuth();
  const [tutorialOpen, setTutorialOpen] = useState(false);
  const [tutorialDismissed, setTutorialDismissed] = useState(() => hasDismissedFirstVisitTutorial());
  const currentLanguage: SupportedLanguage = i18n.language === 'en' ? 'en' : 'ko';
  const displayName = session?.channelName?.trim() || (session ? formatChannelLabel(session.channelId, t('nav.signedInChannel')) : '');
  const discordResourcesActive = location.pathname === '/discord' || location.pathname.startsWith('/discord/');
  const safeProfileUrl = safeImageSrc(session?.profileUrl);

  useEffect(() => {
    if (!loading && !tutorialDismissed) {
      setTutorialOpen(true);
    }
  }, [loading, tutorialDismissed]);

  /**
   * `showTutorial`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   */
  function showTutorial() {
    setTutorialOpen(true);
  }

  /**
   * `closeTutorial`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   */
  function closeTutorial() {
    dismissFirstVisitTutorial();
    setTutorialDismissed(true);
    setTutorialOpen(false);
  }

  /**
   * `handleTutorialPrimaryAction`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   */
  function handleTutorialPrimaryAction() {
    closeTutorial();
    navigate(session ? '/discord/webhooks' : '/login');
  }

  /**
   * `handleLogout`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
   */
  async function handleLogout() {
    await logout();
    navigate('/login');
  }

  /**
   * `handleLanguageChange`는 사용자 동작 또는 상태 변경을 처리합니다.
   *
   * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
   */
  function handleLanguageChange(language: SupportedLanguage) {
    void i18n.changeLanguage(language);
  }

  return (
    <div className="d-flex flex-column min-vh-100">
      <nav className="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm">
        <div className="container">
          <NavLink to="/" className="navbar-brand d-flex align-items-center gap-2">
            <i className="bi bi-broadcast-pin" aria-hidden="true" />
            <span>{t('app.name')}</span>
          </NavLink>
          <button
            className="navbar-toggler"
            type="button"
            data-bs-toggle="collapse"
            data-bs-target="#mainNav"
            aria-controls="mainNav"
            aria-expanded="false"
            aria-label={t('nav.toggleNavigation')}
          >
            <span className="navbar-toggler-icon" />
          </button>
          <div className="collapse navbar-collapse" id="mainNav">
            <ul className="navbar-nav me-auto mb-2 mb-lg-0">
              <li className="nav-item">
                <NavLink to="/subscriptions" className={navLinkClass}>
                  <i className="bi bi-list-check me-1" aria-hidden="true" />
                  {t('nav.subscriptions')}
                </NavLink>
              </li>
              <li className="nav-item">
                <NavLink to="/discord/webhooks" className={({isActive}) => navLinkClass({isActive: isActive || discordResourcesActive})}>
                  <i className="bi bi-discord me-1" aria-hidden="true" />
                  {t('nav.discordResources')}
                </NavLink>
              </li>
            </ul>
            <div className="d-flex align-items-center gap-2">
              <div className="btn-group btn-group-sm" role="group" aria-label={t('nav.languageSwitcher')}>
                <button
                  type="button"
                  className={`btn ${currentLanguage === 'ko' ? 'btn-light' : 'btn-outline-light'}`}
                  onClick={() => handleLanguageChange('ko')}
                  aria-pressed={currentLanguage === 'ko'}
                >
                  {t('nav.korean')}
                </button>
                <button
                  type="button"
                  className={`btn ${currentLanguage === 'en' ? 'btn-light' : 'btn-outline-light'}`}
                  onClick={() => handleLanguageChange('en')}
                  aria-pressed={currentLanguage === 'en'}
                >
                  {t('nav.english')}
                </button>
              </div>
              <button type="button" onClick={showTutorial} className="btn btn-outline-light btn-sm">
                <i className="bi bi-question-circle me-1" aria-hidden="true" />
                {t('nav.tutorial')}
              </button>
              {session ? (
                <>
                  <div className="channel-user-summary d-flex align-items-center gap-2">
                    <span className="channel-avatar" aria-hidden="true">
                      <i className="bi bi-person-fill" aria-hidden="true" />
                      {safeProfileUrl ? (
                        <img
                          src={safeProfileUrl}
                          alt=""
                          referrerPolicy="no-referrer"
                          onError={(event) => {
                            event.currentTarget.style.display = 'none';
                          }}
                        />
                      ) : null}
                    </span>
                    <span className="channel-name text-light small fw-semibold">{displayName}</span>
                  </div>
                  <button type="button" onClick={handleLogout} className="btn btn-outline-light btn-sm">
                    <i className="bi bi-box-arrow-right me-1" aria-hidden="true" />
                    {t('nav.logout')}
                  </button>
                </>
              ) : (
                <NavLink to="/login" className="btn btn-outline-light btn-sm">
                  <i className="bi bi-box-arrow-in-right me-1" aria-hidden="true" />
                  {t('nav.loginWithChzzk')}
                </NavLink>
              )}
            </div>
          </div>
        </div>
      </nav>
      <main className="container py-4 flex-grow-1">
        <Outlet context={{showTutorial} satisfies TutorialOutletContext} />
      </main>
      <FirstVisitTutorialModal
        isAuthenticated={Boolean(session)}
        open={tutorialOpen}
        onClose={closeTutorial}
        onPrimaryAction={handleTutorialPrimaryAction}
      />
    </div>
  );
}
