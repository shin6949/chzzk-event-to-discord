import {useTranslation} from 'react-i18next';

/**
 * `FirstVisitTutorialModalProps`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
type FirstVisitTutorialModalProps = {
  isAuthenticated: boolean;
  open: boolean;
  onClose: () => void;
  onPrimaryAction: () => void;
};

const tutorialSteps = [
  {
    icon: 'bi-link-45deg',
    titleKey: 'tutorial.steps.webhook.title',
    bodyKey: 'tutorial.steps.webhook.body',
  },
  {
    icon: 'bi-robot',
    titleKey: 'tutorial.steps.botProfile.title',
    bodyKey: 'tutorial.steps.botProfile.body',
  },
  {
    icon: 'bi-broadcast-pin',
    titleKey: 'tutorial.steps.subscription.title',
    bodyKey: 'tutorial.steps.subscription.body',
  },
];

/**
 * `FirstVisitTutorialModal`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function FirstVisitTutorialModal({
  isAuthenticated,
  open,
  onClose,
  onPrimaryAction,
}: FirstVisitTutorialModalProps) {
  const {t} = useTranslation();

  if (!open) {
    return null;
  }

  return (
    <>
      <div
        className="modal fade show first-visit-tutorial"
        role="dialog"
        aria-modal="true"
        aria-labelledby="firstVisitTutorialTitle"
        tabIndex={-1}
        style={{display: 'block'}}
      >
        <div className="modal-dialog modal-dialog-centered modal-lg">
          <div className="modal-content shadow">
            <div className="modal-header">
              <div>
                <p className="small text-primary fw-semibold mb-1">{t('tutorial.quickStart')}</p>
                <h1 className="modal-title fs-4" id="firstVisitTutorialTitle">
                  {t('tutorial.title')}
                </h1>
              </div>
              <button type="button" className="btn-close" aria-label={t('tutorial.closeAria')} onClick={onClose} />
            </div>
            <div className="modal-body">
              <p className="text-secondary mb-4">
                {t('tutorial.description')}
              </p>
              <div className="row g-3">
                {tutorialSteps.map((step) => (
                  <div className="col-12 col-md-4" key={step.titleKey}>
                    <div className="tutorial-step h-100">
                      <span className="tutorial-step-icon" aria-hidden="true">
                        <i className={`bi ${step.icon}`} />
                      </span>
                      <h2 className="h6 mb-2">{t(step.titleKey)}</h2>
                      <p className="small text-secondary mb-0">{t(step.bodyKey)}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-outline-secondary" onClick={onClose}>
                {t('tutorial.close')}
              </button>
              <button type="button" className="btn btn-primary" onClick={onPrimaryAction}>
                {isAuthenticated ? t('tutorial.startDiscordSetup') : t('tutorial.goToLogin')}
              </button>
            </div>
          </div>
        </div>
      </div>
      <div className="modal-backdrop fade show" />
    </>
  );
}
