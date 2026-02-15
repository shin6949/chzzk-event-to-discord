# React FE 추가 구현 계획 (Public App + Chzzk OAuth/RBAC)

작성일: 2026-02-15
검증 기준: CHZZK 공식 문서(Authorization/User/참고사항), 확인일 2026-02-15

## 0. 현재 백엔드 상태 요약 (repo 기반)
- Java 17 + Spring Boot 3.5.8 + Gradle
- DB: PostgreSQL(운영), 테스트: H2 (`application-test.yml`)
- Docker/GitHub Actions로 Spring Boot JAR 이미지 GHCR 빌드/푸시
- 현재 공개 API는 사실상 `POST /form/insert` 1개 중심
- 인증은 임시 방식(`Authorization: Bearer {APP_INSERT_PASSWORD}`)이며 Chzzk OAuth 연동 부재
- 사용자 계정/권한/소유권 모델이 데이터 레벨로 정식 정의되어 있지 않음

## 1. 목표/범위 (업데이트)
### 필수 범위
- FE를 **공개 서비스(Public-facing app)** 기준으로 설계/배포
- 인증은 **Chzzk OAuth(Naver OAuth)** 를 단일 로그인 수단으로 사용
- 사용자(`USER`)는 **본인과 링크된 Chzzk 계정**에 대해서만 알림/구독(`subscriptions`) 생성/수정/삭제 가능
- `ADMIN`은 전체 사용자/구독 리소스 조회 및 운영 기능 수행 가능
- 핵심 리소스의 CRUD 구현
  - `subscriptions` (필수)
  - `webhooks`, `bot-profiles` (운영 정책에 따라 유지/확장)
- FE 라우팅에서 Public/App/Admin 분리 및 보호 라우트 적용
- 운영 배포 보안 항목 포함(보안 헤더, rate limiting, 감사 로그, OAuth 비밀 관리)

### 비범위(후순위)
- 다중 OAuth provider 동시 지원(예: Google, Kakao)
- MFA, 팀/조직 단위 고급 ACL
- 대규모 분석 대시보드

## 2. 권한/소유권/아이덴티티 모델
### 2.1 애플리케이션 사용자 식별자 모델
- 원칙: 인가 판단의 기준 키는 `channelId` (출처: `GET /open/v1/users/me`)
- OAuth code/token 획득 직후 `GET /open/v1/users/me`를 호출해 `channelId`를 조회
- 옵션 A(단순): `users`의 PK를 `channelId`로 직접 사용
- 옵션 B(권장): 내부 `users.id` 유지 + `oauth_accounts(provider_user_id = channelId)` 매핑
  - 장점: 내부 참조 안정성, 향후 provider 확장 용이, 계정 이력 관리 용이
  - 인가 시에는 항상 현재 로그인 principal의 `channelId`를 추출해 사용
- 기존 스키마와의 정합: `channelId`는 `chzzk_channel.channel_id`와 동일 키로 취급

### 2.2 소유권 규칙
- `subscriptions.ownerChannelId`는 로그인 사용자 `channelId`와 반드시 일치해야 함(`ADMIN` 제외)
- 생성 API에서 owner 관련 필드는 클라이언트 입력을 신뢰하지 않고 서버에서 강제 세팅
- 조회/수정/삭제는 서비스/리포지토리 레벨에서 `channelId` 조건을 필수 적용
- `ADMIN`은 전역 조회/수정/삭제 가능(감사 로그 필수)

## 3. API 요구사항 (필수 엔드포인트)
기본 prefix: `/api/v1`

### 3.1 Chzzk Open API 인증 Provider 명세 (검증 반영)
- Authorization Code 시작: `GET https://chzzk.naver.com/account-interlock`
  - query: `clientId`, `redirectUri`, `state`
- Open API 도메인: `https://openapi.chzzk.naver.com`
- 토큰 발급/갱신: `POST /auth/v1/token`
  - `grantType=authorization_code`: code 교환
  - `grantType=refresh_token`: access token 재발급
  - TTL: access token 1일(`expiresIn=86400`), refresh token 30일, refresh token은 일회용(one-time use)
- 토큰 폐기(revoke): `POST /auth/v1/token/revoke`
- 사용자 식별 매핑: `GET /open/v1/users/me` 응답의 `channelId`를 앱 사용자 식별자로 사용

### 3.2 Auth (앱 내부 엔드포인트)
- `GET /api/v1/auth/chzzk/login` (Public): OAuth 시작, `state` 생성 후 `https://chzzk.naver.com/account-interlock`로 리다이렉트
- `GET /api/v1/auth/chzzk/callback` (Public): authorization code 교환 + `/open/v1/users/me` 조회 + 사용자 링크/생성 + 앱 세션/JWT 발급
- `GET /api/v1/auth/me` (Auth): 현재 사용자/역할/연결된 Chzzk 프로필 조회
- `POST /api/v1/auth/disconnect` (Auth): Chzzk 연결 해제, `POST /auth/v1/token/revoke` 호출 + 내부 토큰 폐기 + 세션/JWT 무효화
- `POST /api/v1/auth/logout` (Auth): 애플리케이션 로그아웃(로컬 세션/JWT 종료)

### 3.3 User/Role
- `GET /api/v1/users/me` (Auth): 내 앱 프로필 조회
- `PATCH /api/v1/users/me` (Auth): 내 프로필 수정(권한/식별자 필드 제외)
- `GET /api/v1/admin/users` (ADMIN): 사용자 목록
- `GET /api/v1/admin/users/{userId}` (ADMIN): 사용자 상세
- `PATCH /api/v1/admin/users/{userId}/roles` (ADMIN): 역할 변경
- `PATCH /api/v1/admin/users/{userId}/status` (ADMIN): 상태 변경(활성/비활성)

### 3.4 Core CRUD: Subscriptions (필수)
- `POST /api/v1/subscriptions` (USER/ADMIN): 구독 생성 (`USER`는 본인 `channelId`만 가능)
- `GET /api/v1/subscriptions` (USER/ADMIN): 구독 목록 (`USER`는 본인만, `ADMIN`은 전체 + 필터)
- `GET /api/v1/subscriptions/{subscriptionId}` (USER/ADMIN): 구독 상세
- `PUT /api/v1/subscriptions/{subscriptionId}` (USER/ADMIN): 구독 전체 수정
- `PATCH /api/v1/subscriptions/{subscriptionId}` (USER/ADMIN): 부분 수정(예: enabled)
- `DELETE /api/v1/subscriptions/{subscriptionId}` (USER/ADMIN): 구독 삭제

### 3.5 보조 조회 API (FE UX용)
- `GET /api/v1/chzzk/channels/search?keyword=...` (Auth): 채널 검색
- `GET /api/v1/chzzk/channels/{channelId}` (Auth): 채널 상세
- `GET /api/v1/meta/subscription-types` (Auth): 구독 타입 목록

### 3.6 Admin 운영 API
- `GET /api/v1/admin/audit-logs` (ADMIN): 감사 로그 목록/검색
- `GET /api/v1/admin/audit-logs/{auditId}` (ADMIN): 감사 로그 상세

### 3.7 API 공통 규칙
- 에러 포맷 표준화: `code`, `message`, `details`, `traceId`, `timestamp`
- 목록 API: pagination/sort/filter 표준화(`page`, `size`, `sort`)
- 변경 API(POST/PUT/PATCH/DELETE): 감사 로그 이벤트 생성
- 소유권은 요청 body/query가 아니라 인증 principal의 `channelId`로 강제 검증
- 기존 임시 API `POST /form/insert`는 Deprecated 처리 후 점진 제거

## 4. DB 설계 업데이트 (users / oauth_accounts / tokens / subscriptions)
### 4.1 테이블/컬럼 제안
| Table | 주요 컬럼 | 제약/설명 |
|---|---|---|
| `users` | `id`(PK), `role`, `status`, `primary_channel_id`(nullable), `created_at`, `updated_at` | 내부 사용자 식별자 유지 시 사용. `primary_channel_id`는 unique 권장 |
| `oauth_accounts` | `id`(PK), `user_id`(FK), `provider`, `provider_user_id`, `profile_json`, `linked_at`, `disconnected_at` | `provider_user_id`는 `/open/v1/users/me.channelId`, `UNIQUE(provider, provider_user_id)` |
| `tokens` | `id`(PK), `oauth_account_id`(FK), `access_token_enc`, `refresh_token_enc`, `scope`, `access_expires_at`, `refresh_expires_at`, `rotated_at`, `revoked_at` | 토큰은 평문 저장 금지(암호화 저장), refresh token 일회용 회전 기록 필수 |
| `subscriptions` | `id`(PK), `owner_user_id`(FK), `owner_channel_id`, `chzzk_channel_id`, `event_type`, `target_webhook`, `enabled`, `created_at`, `updated_at` | `USER` 인가 기준은 `owner_channel_id` (`chzzk_channel.channel_id`와 정합) |

### 4.2 마이그레이션 전략
1. 1차 배포: 신규 테이블(`oauth_accounts`, `tokens`) 및 `subscriptions.owner_channel_id` 컬럼 추가(초기 nullable 가능)
2. 백필: 기존 리소스 소유자 데이터를 `users`/`subscriptions`로 이관하고 `oauth_accounts` 링크 생성
3. 애플리케이션 이중 읽기/단일 쓰기 전환: 읽기는 구/신 컬럼 호환, 쓰기는 신 컬럼 우선
4. 제약 강화: `NOT NULL`, unique/FK, 인덱스(`owner_channel_id`, `provider_user_id`) 적용
5. 정리: 레거시 owner 필드/인증 로직 제거

## 5. 백엔드 변경사항 (Spring Boot)
### 5.1 의존성/보안 구성
- `spring-boot-starter-security` + `spring-boot-starter-oauth2-client` 도입
- `SecurityFilterChain` 권한 규칙
  - Public: `/api/v1/auth/chzzk/login`, `/api/v1/auth/chzzk/callback`
  - Auth 필요: `/api/v1/**`
  - ADMIN 전용: `/api/v1/admin/**`

### 5.2 OAuth2 로그인 플로우
1. `/auth/chzzk/login`에서 `state` 생성/저장(단기 TTL, 1회성)
2. `GET https://chzzk.naver.com/account-interlock?clientId=...&redirectUri=...&state=...`로 redirect
3. `/auth/chzzk/callback`에서 `state` 일치 검증 후 `POST /auth/v1/token`(`grantType=authorization_code`)으로 토큰 교환
4. Access Token으로 `GET /open/v1/users/me` 호출하여 `channelId` 획득
5. `channelId` 기준으로 `users`/`oauth_accounts` upsert 후 앱 인증 세션/JWT 발급

### 5.3 OAuth 이후 세션/JWT 전략
- 옵션 A: 서버 세션(권장 단순 운영)
  - HttpOnly + Secure + SameSite 설정
  - Redis 세션 저장소 사용 가능
- 옵션 B: JWT(access) + refresh
  - access 짧은 TTL, refresh 회전(rotation)/폐기(revoke) 필수
- 어떤 옵션이든 `/auth/me`, `/auth/logout`, `/auth/disconnect` 동작 정의 필요

### 5.4 토큰 저장/갱신/해제
- `tokens` 테이블에 OAuth access/refresh token을 암호화 저장
- 토큰 만료 정책 반영: access token 1일, refresh token 30일
- 갱신 시 `POST /auth/v1/token` + `grantType=refresh_token` 사용, refresh token은 1회 사용 후 즉시 회전
- `disconnect` 또는 보안 이벤트 시 `POST /auth/v1/token/revoke` 호출 + 내부 저장 토큰 즉시 폐기

### 5.5 소유권 강제(핵심)
- 서비스 레이어에서 `principal.channelId` 기반 쿼리 강제
- 클라이언트가 전달한 `ownerUserId`/`ownerChannelId`는 무시하거나 검증 실패 처리
- ADMIN만 전역 조회 가능, 일반 사용자에게는 항상 본인 범위 필터 적용

## 6. FE 라우팅 및 UI 업데이트
### 6.1 Public routes
- `/` (랜딩)
- `/auth/chzzk/login` (로그인 진입 버튼/redirect 핸들링)
- `/auth/chzzk/callback` (콜백 처리 화면)

### 6.2 Authenticated routes (`USER`, `ADMIN`)
- `/app/dashboard`
- `/app/subscriptions`
- `/app/subscriptions/new`
- `/app/subscriptions/:id`
- `/app/settings/account` (Chzzk 계정 연결 상태/해제)

### 6.3 Admin-only routes (`ADMIN`)
- `/admin/users`
- `/admin/users/:id`
- `/admin/subscriptions`
- `/admin/audit-logs`

### 6.4 FE 동작 규칙
- 로그인 버튼은 `Login with Chzzk` 라벨로 제공하고, 클릭 시 `/auth/chzzk/login`으로 이동
- `auth/me` 응답으로 세션 복구 + role hydration + 연결된 Chzzk 프로필 표시
- 계정 카드에 `channelId`, 닉네임, 프로필 이미지, 연결 시각 표시
- 일반 사용자 UI에서 타인 계정 선택/입력 UI 제거
- 구독 생성/수정 폼은 현재 로그인 사용자 계정으로만 동작
- ADMIN 화면에서는 전체 목록/필터/강제 수정 가능

### 6.5 FE 인증 UX 플로우 (필수)
1. 랜딩/로그인 화면: `Login with Chzzk` 버튼 노출
2. 버튼 클릭: FE는 `/auth/chzzk/login` 호출, BE가 `state` 생성 후 `https://chzzk.naver.com/account-interlock`로 redirect
3. 콜백 라우트(`/auth/chzzk/callback`): 로딩/에러 처리 + 성공 시 앱 세션 확정
4. 링크 상태 화면(`/app/settings/account`): 계정 연결 상태(`linked`, `disconnected`, `token_expired`)와 `channelId` 표시
5. 연결 해제 액션: 사용자 확인 후 `/api/v1/auth/disconnect` 호출, 성공 시 링크 상태 즉시 반영

## 7. Admin UI 요구사항
- 사용자 관리
  - 사용자 목록/검색/상세
  - 역할 변경(`USER` ↔ `ADMIN`)
  - 계정 비활성/연결 해제 처리
- 전체 구독 리소스 관리
  - `subscriptions` 전체 조회 + `owner_channel_id` 필터
  - 관리자 강제 수정/삭제
- 감사 로그 뷰어
  - 기간/사용자/액션 기준 필터
  - 위험 이벤트(권한 변경, 대량 삭제, 강제 disconnect) 빠른 확인

## 8. 로컬 개발/배포 (Docker 포함)
### 로컬 개발
- BE: 기존 Gradle 실행 유지
- FE: `frontend/` Vite dev server
- OAuth redirect URI를 dev/stage/prod 별도 등록
- 개발 환경 CORS: FE dev origin(`http://localhost:5173`) 명시 허용 또는 proxy 사용

### 프로덕션 배포
- 권장 구조
  - FE 정적 파일(Nginx) + BE API(Spring Boot) 분리
  - 역프록시에서 HTTPS 강제 및 보안 헤더 적용
- Secret 관리
  - OAuth client id/secret, 토큰 암호화 키, 세션/JWT 키, DB 비밀번호를 GitHub Secrets + 런타임 env로 주입

### 필수 환경변수/시크릿 (명시)
- `CHZZK_CLIENT_ID`: OAuth client id
- `CHZZK_CLIENT_SECRET`: OAuth client secret (서버 사이드 전용, FE 번들 금지)
- `CHZZK_AUTHORIZE_URL`: `https://chzzk.naver.com/account-interlock`
- `CHZZK_OPENAPI_BASE_URL`: `https://openapi.chzzk.naver.com`
- `CHZZK_REDIRECT_URI`: 등록된 redirect URI와 완전 일치해야 함
- `TOKEN_ENCRYPTION_KEY`: 토큰 암호화(저장 시) 키 또는 KMS 키 식별자
- `SESSION_SECRET` 또는 `JWT_SIGNING_KEY`: 앱 세션/JWT 서명 키

### Public App 배포 체크리스트
- TLS(HTTPS) 필수
- OAuth redirect URI 정확성 확인
- Security headers 적용 확인
- Rate limiting 활성화 확인
- 감사 로그 보존 기간/마스킹 정책 정의

## 9. CI/CD 업데이트 (GitHub Actions)
- 기존 백엔드 워크플로(`gradle.yml`, `docker.yml`) 유지 + 보강
- FE 워크플로 추가
  - 트리거: `frontend/**` 변경
  - 작업: install → lint → test → build
- 통합 검증(권장)
  - BE API 계약 테스트(auth callback/mock + role + subscriptions ownership)
  - FE E2E smoke test(OAuth 로그인, 내 구독 생성/수정/삭제, 관리자 접근)
- 릴리즈 전 검증
  - OAuth 설정 누락/잘못된 redirect URI 검사 스크립트 권장

## 10. 보안 고려사항 (OAuth 중심)
- Scope 최소화: Chzzk 연동에 필요한 scope만 요청(least privilege)
- OAuth `state` 검증: 요청 시작 시 저장한 값과 callback의 `state`를 반드시 비교(불일치 시 즉시 실패)
- HTTPS 강제: authorize/callback/app API 전 구간 TLS 적용, http redirect URI 사용 금지(로컬 개발 제외)
- Secret 관리: `CHZZK_CLIENT_SECRET`은 서버에서만 사용하고 코드/이미지/프론트 번들 포함 금지
- Token 저장 보안: access/refresh token 평문 저장 금지, at-rest 암호화 및 키 분리/회전
- Refresh Token 보호: 1회 사용 후 즉시 교체(재사용 감지 시 세션 폐기)
- 로그 마스킹: 토큰, 인가 코드, client secret, 개인식별자(`channelId`)는 저장 전 redaction

## 11. 마일스톤 (수정)
### Milestone 0: OAuth/아이덴티티 설계 확정
- `channelId` 중심 인가 모델 확정(`/open/v1/users/me`)
- `users` + `oauth_accounts` + `tokens` + `subscriptions` 스키마 확정
- 세션/JWT 전략 확정

### Milestone 1: 백엔드 OAuth + RBAC 뼈대
- `spring-boot-starter-oauth2-client` 적용
- `/api/v1/auth/chzzk/login`, `/api/v1/auth/chzzk/callback`, `/api/v1/auth/me`, `/api/v1/auth/disconnect` 구현
- `ROLE_USER`, `ROLE_ADMIN` 및 인가 규칙 구현

### Milestone 2: Subscriptions API 완성 (필수 게이트)
- `subscriptions` CRUD 구현
- `channelId` 소유권 검증 + ADMIN 전역 접근 검증
- 감사 로그 연동

### Milestone 3: FE 구현 (보호 라우트 포함)
- OAuth 로그인/콜백/세션 복구 구현
- 내 Chzzk 계정 연결 상태 UI 구현
- `subscriptions` CRUD UI 구현(일반 사용자 본인 계정만)

### Milestone 4: Admin 콘솔 완성
- 사용자/권한/연결 상태 관리 UI
- 전체 구독/감사 로그 UI

### Milestone 5: 배포 하드닝 + CI/CD
- 보안 헤더/레이트리밋/로그 정책 반영
- OAuth 비밀/키 관리 정책 적용
- FE/BE 통합 테스트 자동화 완성

## 12. 레거시 API 전환 계획
- `POST /form/insert`는 단기 호환용으로 유지하되 Deprecated 표기
- FE는 신규 `/api/v1/*` 경로만 사용
- OAuth + subscriptions 안정화 후 `/form/insert` 제거 일정 확정
