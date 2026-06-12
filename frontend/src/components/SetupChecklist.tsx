import {useTranslation} from 'react-i18next';
import {Link} from 'react-router-dom';
import {isSafeInternalPath} from '../security/urls';

/**
 * `SetupStepKey`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
type SetupStepKey = 'webhook' | 'botProfile' | 'subscription';

/**
 * `SetupChecklistProps`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
type SetupChecklistProps = {
  currentStep: SetupStepKey;
  completed?: Partial<Record<SetupStepKey, boolean>>;
  returnTo?: string;
};

const steps: {
  key: SetupStepKey;
  icon: string;
  titleKey: string;
  bodyKey: string;
  actionLabelKey: string;
  to: string;
}[] = [
  {
    key: 'webhook',
    icon: 'bi-link-45deg',
    titleKey: 'setupChecklist.webhook.title',
    bodyKey: 'setupChecklist.webhook.body',
    actionLabelKey: 'setupChecklist.webhook.action',
    to: '/discord/webhooks',
  },
  {
    key: 'botProfile',
    icon: 'bi-robot',
    titleKey: 'setupChecklist.botProfile.title',
    bodyKey: 'setupChecklist.botProfile.body',
    actionLabelKey: 'setupChecklist.botProfile.action',
    to: '/discord/bot-profiles',
  },
  {
    key: 'subscription',
    icon: 'bi-broadcast-pin',
    titleKey: 'setupChecklist.subscription.title',
    bodyKey: 'setupChecklist.subscription.body',
    actionLabelKey: 'setupChecklist.subscription.action',
    to: '/subscriptions/new',
  },
];

/**
 * `buildLink`는 값을 변환하거나 생성합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
function buildLink(to: string, returnTo?: string): string {
  if (!returnTo || to === '/subscriptions/new' || !isSafeInternalPath(returnTo, ['/subscriptions'])) {
    return to;
  }

  const params = new URLSearchParams();
  params.set('returnTo', returnTo.trim());
  return `${to}?${params.toString()}`;
}

/**
 * `statusLabelKey`는 관련 프론트엔드 기능을 수행합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
function statusLabelKey(
  step: SetupStepKey,
  currentStep: SetupStepKey,
  completed: Partial<Record<SetupStepKey, boolean>>,
): string {
  if (completed[step]) {
    return 'common.done';
  }
  return step === currentStep ? 'common.current' : 'common.next';
}

/**
 * `SetupChecklist`는 화면 컴포넌트를 렌더링합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function SetupChecklist({currentStep, completed = {}, returnTo}: SetupChecklistProps) {
  const {t} = useTranslation();

  return (
    <section className="setup-checklist" aria-label={t('setupChecklist.ariaLabel')}>
      <div className="d-flex flex-column flex-lg-row justify-content-between gap-2 mb-3">
        <div>
          <h2 className="h6 mb-1">{t('setupChecklist.title')}</h2>
          <p className="small text-secondary mb-0">{t('setupChecklist.description')}</p>
        </div>
      </div>
      <div className="row g-2">
        {steps.map((step) => {
          const status = t(statusLabelKey(step.key, currentStep, completed));
          const isCurrent = step.key === currentStep;
          return (
            <div className="col-12 col-lg-4" key={step.key}>
              <div className={`setup-checklist-step h-100${isCurrent ? ' is-current' : ''}${completed[step.key] ? ' is-complete' : ''}`}>
                <div className="d-flex align-items-start gap-3">
                  <span className="setup-checklist-icon" aria-hidden="true">
                    <i className={`bi ${step.icon}`} />
                  </span>
                  <div className="min-w-0">
                    <div className="d-flex flex-wrap align-items-center gap-2">
                      <h3 className="h6 mb-0">{t(step.titleKey)}</h3>
                      <span className={`badge ${completed[step.key] ? 'text-bg-success' : isCurrent ? 'text-bg-primary' : 'text-bg-light text-secondary border'}`}>
                        {status}
                      </span>
                    </div>
                    <p className="small text-secondary my-2">{t(step.bodyKey)}</p>
                    <Link className={`btn btn-sm ${isCurrent ? 'btn-primary' : 'btn-outline-primary'}`} to={buildLink(step.to, returnTo)}>
                      {t(step.actionLabelKey)}
                    </Link>
                  </div>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </section>
  );
}
