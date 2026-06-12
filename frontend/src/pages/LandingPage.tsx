import {useTranslation} from 'react-i18next';
import {Link, useOutletContext} from 'react-router-dom';
import {useAuth} from '../auth/AuthContext';
import {SetupChecklist} from '../components/SetupChecklist';
import type {TutorialOutletContext} from '../layouts/MainLayout';

/**
 * `LandingPage`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
export function LandingPage() {
  const {t} = useTranslation();
  const {showTutorial} = useOutletContext<TutorialOutletContext>();
  const {session, loading} = useAuth();
  const primaryPath = session ? '/subscriptions/new' : '/login';
  const primaryLabel = session ? t('common.createSubscription') : t('nav.loginWithChzzk');

  return (
    <>
      <section className="landing-hero bg-white border shadow-sm overflow-hidden mb-3">
        <div className="row g-0 align-items-stretch">
          <div className="col-12 col-lg-7 p-4 p-md-5">
            <div className="d-flex align-items-center gap-2 mb-3">
              <span className="landing-hero-icon" aria-hidden="true">
                <i className="bi bi-broadcast-pin" />
              </span>
              <span className="small text-primary fw-semibold">{t('landing.eyebrow')}</span>
            </div>
            <h1 className="display-6 fw-bold mb-3">{t('landing.title')}</h1>
            <p className="fs-5 text-secondary mb-4">{t('landing.description')}</p>
            <p className="brand-relationship-note small rounded px-3 py-2 mb-4">{t('landing.brandNotice')}</p>
            <div className="alert alert-light border d-flex align-items-start gap-2 mb-4">
              <i className={`bi ${session ? 'bi-check-circle-fill text-success' : 'bi-info-circle-fill text-primary'} mt-1`} aria-hidden="true" />
              <div>
                <div className="fw-semibold">{session ? t('landing.accountReady') : t('landing.loginNeeded')}</div>
                <div className="small text-secondary">{t('landing.startHint')}</div>
              </div>
            </div>
            <div className="d-flex flex-wrap gap-2">
              <Link to={primaryPath} className={`btn btn-primary${loading ? ' disabled' : ''}`} aria-disabled={loading}>
                <i className={`bi ${session ? 'bi-plus-lg' : 'bi-box-arrow-in-right'} me-1`} aria-hidden="true" />
                {primaryLabel}
              </Link>
              <button type="button" className="btn btn-outline-primary" onClick={showTutorial}>
                <i className="bi bi-question-circle me-1" aria-hidden="true" />
                {t('landing.viewTutorial')}
              </button>
              <Link to="/discord/webhooks" className="btn btn-outline-secondary">
                <i className="bi bi-discord me-1" aria-hidden="true" />
                {t('landing.discordResources')}
              </Link>
            </div>
          </div>
          <div className="col-12 col-lg-5 landing-workflow-panel p-4 p-md-5">
            <span className="badge text-bg-success mb-3">{t('landing.workflowBadge')}</span>
            <h2 className="h4 fw-bold mb-2">{t('landing.workflowTitle')}</h2>
            <p className="text-secondary mb-4">{t('landing.workflowBody')}</p>
            <ol className="list-group list-group-numbered">
              <li className="list-group-item d-flex align-items-start gap-3">
                <i className="bi bi-link-45deg text-primary mt-1" aria-hidden="true" />
                <div>
                  <div className="fw-semibold">{t('landing.steps.one.title')}</div>
                  <div className="small text-secondary">{t('landing.steps.one.body')}</div>
                </div>
              </li>
              <li className="list-group-item d-flex align-items-start gap-3">
                <i className="bi bi-robot text-primary mt-1" aria-hidden="true" />
                <div>
                  <div className="fw-semibold">{t('landing.steps.two.title')}</div>
                  <div className="small text-secondary">{t('landing.steps.two.body')}</div>
                </div>
              </li>
              <li className="list-group-item d-flex align-items-start gap-3">
                <i className="bi bi-broadcast-pin text-primary mt-1" aria-hidden="true" />
                <div>
                  <div className="fw-semibold">{t('landing.steps.three.title')}</div>
                  <div className="small text-secondary">{t('landing.steps.three.body')}</div>
                </div>
              </li>
            </ol>
          </div>
        </div>
      </section>

      <SetupChecklist currentStep="webhook" returnTo="/subscriptions/new" />

      <section className="row g-3 mt-1" aria-label={t('landing.workflowTitle')}>
        <div className="col-12 col-md-4">
          <div className="quick-start-item h-100">
            <span className="badge text-bg-primary mb-2">
              <i className="bi bi-link-45deg me-1" aria-hidden="true" />
              {t('landing.steps.one.badge')}
            </span>
            <h2 className="h6">{t('landing.steps.one.title')}</h2>
            <p className="small text-secondary mb-0">{t('landing.steps.one.body')}</p>
          </div>
        </div>
        <div className="col-12 col-md-4">
          <div className="quick-start-item h-100">
            <span className="badge text-bg-primary mb-2">
              <i className="bi bi-robot me-1" aria-hidden="true" />
              {t('landing.steps.two.badge')}
            </span>
            <h2 className="h6">{t('landing.steps.two.title')}</h2>
            <p className="small text-secondary mb-0">{t('landing.steps.two.body')}</p>
          </div>
        </div>
        <div className="col-12 col-md-4">
          <div className="quick-start-item h-100">
            <span className="badge text-bg-primary mb-2">
              <i className="bi bi-broadcast-pin me-1" aria-hidden="true" />
              {t('landing.steps.three.badge')}
            </span>
            <h2 className="h6">{t('landing.steps.three.title')}</h2>
            <p className="small text-secondary mb-0">{t('landing.steps.three.body')}</p>
          </div>
        </div>
      </section>
    </>
  );
}
