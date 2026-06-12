// / <reference types="vite/client" />

/**
 * `ImportMetaEnv`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
interface ImportMetaEnv {
  readonly VITE_API_BASE_URL?: string;
}

/**
 * `ImportMeta`는 데이터 계약을 정의합니다.
 *
 * Git 이력: 생성 2026-02-16 22:09:10 +0900, 작성자 COCOBLUE, 작성 버전 unreleased after Ver.0.1.4, 근거 커밋 0abb6a3.
 */
interface ImportMeta {
  readonly env: ImportMetaEnv;
}
