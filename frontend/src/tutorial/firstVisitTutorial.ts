export const FIRST_VISIT_TUTORIAL_DISMISSED_KEY = 'streaming-alert-service:tutorial-dismissed';

/**
 * `hasDismissedFirstVisitTutorial`는 조건 충족 여부를 판단합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function hasDismissedFirstVisitTutorial(): boolean {
  if (typeof window === 'undefined') {
    return true;
  }

  return window.localStorage.getItem(FIRST_VISIT_TUTORIAL_DISMISSED_KEY) === 'true';
}

/**
 * `dismissFirstVisitTutorial`는 관련 프론트엔드 기능을 수행합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(미추적 파일, 사용자 확인: Codex 작성).
 */
export function dismissFirstVisitTutorial(): void {
  if (typeof window === 'undefined') {
    return;
  }

  window.localStorage.setItem(FIRST_VISIT_TUTORIAL_DISMISSED_KEY, 'true');
}
