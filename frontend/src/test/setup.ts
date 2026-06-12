import '@testing-library/jest-dom/vitest';

/**
 * `createLocalStorageMock`는 값을 변환하거나 생성합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
 */
function createLocalStorageMock(): Storage {
  const store = new Map<string, string>();

  return {
    get length() {
      return store.size;
    },
    /**
     * `clear`는 사용자 동작 또는 상태 변경을 처리합니다.
     *
     * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
     */
    clear() {
      store.clear();
    },
    /**
     * `getItem`는 데이터를 요청하거나 조회합니다.
     *
     * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
     */
    getItem(key: string) {
      return store.get(key) ?? null;
    },
    /**
     * `key`는 관련 프론트엔드 기능을 수행합니다.
     *
     * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
     */
    key(index: number) {
      return Array.from(store.keys())[index] ?? null;
    },
    /**
     * `removeItem`는 사용자 동작 또는 상태 변경을 처리합니다.
     *
     * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
     */
    removeItem(key: string) {
      store.delete(key);
    },
    /**
     * `setItem`는 사용자 동작 또는 상태 변경을 처리합니다.
     *
     * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
     */
    setItem(key: string, value: string) {
      store.set(key, value);
    },
  } as Storage;
}

/**
 * `hasUsableLocalStorage`는 조건 충족 여부를 판단합니다.
 *
 * Git 이력: 생성 2026-06-12 13:50:39 +0900, 작성자 AI, 작성 버전 unreleased after Ver.0.1.4, 근거 Git 이력 없음(현재 작업트리 신규/미커밋, 사용자 확인: Codex 작성).
 */
function hasUsableLocalStorage(): boolean {
  try {
    const key = '__local_storage_test__';
    window.localStorage.setItem(key, key);
    window.localStorage.removeItem(key);
    return true;
  } catch {
    return false;
  }
}

if (typeof window !== 'undefined' && !hasUsableLocalStorage()) {
  Object.defineProperty(window, 'localStorage', {
    value: createLocalStorageMock(),
    configurable: true,
  });
}

const i18nReady = import('../i18n');

beforeEach(async () => {
  const {default: i18n, DEFAULT_LANGUAGE, LANGUAGE_STORAGE_KEY} = await i18nReady;
  window.localStorage.removeItem(LANGUAGE_STORAGE_KEY);
  await i18n.changeLanguage(DEFAULT_LANGUAGE);
});
