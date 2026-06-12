# 보안 취약사항 진단 및 조치 리포트

진단일: 2026-06-10

## 기준

- 소프트웨어 개발보안 가이드(2021.12.29)
- 소프트웨어 보안약점 진단가이드(2021)

주요 매핑 항목은 중요정보 저장/전송, 인증수행 제한, 접근통제, 입력값 검증, 업로드 파일 검증, 오류 및 로그 처리이다.

## 진단 범위

- Spring Boot 백엔드 인증, OAuth, JWT/refresh token, legacy form insert, Discord webhook/bot profile 리소스 처리
- DB 마이그레이션과 보안 관련 애플리케이션 설정
- 프론트엔드 API 호출, URL/이미지/파일 입력값 검증, CSRF 연동, 빌드/테스트 상태
- GitHub Actions 기반 SCA 구성

## 조치 완료 사항

### 1. CHZZK OAuth 토큰 평문 저장

- 위험: CHZZK access token과 refresh token이 DB에 평문으로 저장되면 DB 유출 시 외부 계정 권한이 바로 노출된다.
- 가이드 매핑: 중요정보 저장, 암호연산, 충분하지 않은 보호조치.
- 조치:
  - `ChzzkAuthService`가 OAuth access/refresh token을 저장할 때 `SecretEncryptionService`로 AES-GCM 암호화하도록 변경했다.
  - 이미 저장된 평문 토큰은 `getValidAccessToken` 경로에서 복호화 사용 후 암호문으로 재저장되도록 lazy migration을 추가했다.
  - revoke/refresh 요청 시에는 저장값을 복호화한 뒤 외부 CHZZK API로 전송하도록 했다.
  - OAuth 실패 응답 본문은 로그에 원문을 남기지 않고 길이만 남기도록 바꿨다.
  - 암호문 길이 증가를 고려해 `chzzk_oauth_token.access_token`, `refresh_token` 컬럼을 5000자로 확장하는 V5 Flyway 마이그레이션을 추가했다.

### 2. Refresh token 회전 동시성

- 위험: 동일 refresh token으로 동시 요청이 들어오면 둘 다 세션 활성 상태를 읽고 새 refresh token을 발급받을 수 있다.
- 가이드 매핑: 인증수행 제한, 세션통제.
- 조치:
  - refresh token session 조회에 pessimistic write lock을 적용했다.
  - consume 시점에 잠긴 행을 확인하고 즉시 revoked 처리하여 재사용 창을 줄였다.

### 3. 운영 OAuth URL 보안 검증 부족

- 위험: 운영 설정 실수로 CHZZK OAuth token/API/redirect URL이 HTTP 또는 credential 포함 URL로 설정되면 code, client secret, token이 평문 전송되거나 오염된 엔드포인트로 전달될 수 있다.
- 가이드 매핑: 중요정보 전송, 보안기능 입력값 검증.
- 조치:
  - 시작 시 `chzzk.oauth.auth-base-url`, `token-base-url`, `api-base-url`, `redirect-uri`를 검증한다.
  - HTTPS를 기본 요구사항으로 두고, 테스트 또는 loopback HTTP만 예외 허용한다.
  - 인증 cookie의 SameSite 값 유효성과 `SameSite=None` 사용 시 Secure 필수 조건도 검증한다.

### 4. Legacy form insert 리소스 소유자 검증

- 위험: legacy `/form/insert`가 활성화된 경우, 요청자가 다른 owner의 webhookId 또는 botProfileId를 넣어 교차 소유 리소스를 연결할 수 있다.
- 가이드 매핑: 중요자원 접근통제, 입력값 검증.
- 조치:
  - webhookId/botProfileId를 사용할 때 해당 리소스의 owner channel과 요청 owner channel이 일치하는지 검증한다.
  - 존재하지 않는 ID는 404, 소유자 불일치는 403으로 처리한다.
  - channelId/channelName 입력 누락을 400으로 명시 처리하고 channelId/channelName 공백을 정규화한다.

### 5. 프론트엔드 URL 및 전송 경로 검증 부족

- 위험: 악성 API base/path, 신뢰하지 않는 OAuth authorization URL, 위조 Discord webhook URL, protocol-relative 이미지 URL이 FE를 통해 BE 또는 브라우저 sink로 전달될 수 있다.
- 가이드 매핑: 입력값 검증, 보안기능 입력값 검증, 중요정보 전송, 크로스사이트 스크립트 방지.
- 조치:
  - `VITE_API_BASE_URL`은 same-origin API path만 허용하고, 외부 origin, query/fragment, path traversal, control character를 거부한다.
  - API client는 요청 전 `buildApiUrl`에서 unsafe path를 먼저 거부하여 CSRF 조회나 `fetch`가 실행되지 않게 했다.
  - API client error message는 500자로 제한해 과도한 서버 오류 본문이 UI로 노출되지 않게 했다.
  - CHZZK 로그인 authorization URL은 `https://chzzk.naver.com/account-interlock`만 허용한다.
  - Discord webhook 입력은 FE에서도 HTTPS, `discord.com`/`discordapp.com`, webhook path, query/fragment/userinfo 금지를 검증한다.
  - bot profile avatar URL과 로그인 사용자 profile URL은 안전한 이미지 scheme 및 내부 경로만 렌더링한다.
  - avatar 업로드는 MIME type뿐 아니라 파일 확장자와 0바이트 여부를 FE에서 선검증한다.
  - `returnTo` 링크는 `/subscriptions` 내부 경로만 허용한다.

### 6. SCA 자동화 부재

- 위험: 직접 의존성과 lockfile 변경에서 알려진 취약점이 유입되어도 PR 단계에서 자동 차단되지 않을 수 있다.
- 가이드 매핑: 보안약점 진단, 외부 컴포넌트 보안관리.
- 조치:
  - GitHub Actions에 `Security SCA` workflow를 추가했다.
  - PR에서는 `actions/dependency-review-action@v5`로 dependency change를 검사하고 moderate 이상을 실패 처리한다.
  - FE는 `yarn audit --groups dependencies --level moderate`를 실행한다.
  - BE는 OWASP Dependency-Check Gradle plugin `12.2.2`의 `dependencyCheckAnalyze`를 실행하고 HTML/JSON/JUnit 보고서를 artifact로 업로드한다.
  - push 이벤트에서는 `gradle/actions/dependency-submission@v4`로 GitHub dependency graph를 갱신한다.

## 기존 양호 사항

- JWT secret 기본값 사용과 32바이트 미만 secret은 시작 시 차단된다.
- Discord webhook URL은 HTTPS, 허용 host, webhook path, query/fragment/userinfo 금지를 검증한다.
- Discord webhook URL은 API 응답에서 token 부분이 마스킹된다.
- bot profile 이미지 업로드는 content type allowlist, 파일 크기, 이미지 signature, width/height/pixel 제한을 검사한다.
- API 변경 요청은 CSRF token을 요구하고 프론트엔드 API client가 XSRF token을 자동 첨부한다.
- 일반 사용자와 관리자 권한 경계가 subscription/webhook/bot profile 조회 및 변경 로직에 반영되어 있다.
- 프론트엔드 Nginx 설정에 CSP, X-Content-Type-Options, Referrer-Policy, Permissions-Policy가 설정되어 있다.
- Backend API는 FE 검증 우회 요청에 대해서도 Discord URL, 소유권, validation annotation, CSRF를 다시 검증한다.

## 검증 결과

- `./gradlew test --no-daemon`: 성공
- `cd frontend && yarn test`: 성공
- `cd frontend && yarn lint`: 성공
- `cd frontend && yarn build`: 성공
- `cd frontend && yarn audit:dependencies`: 0 vulnerabilities
- `./gradlew tasks --all --no-daemon`: 성공, `dependencyCheckAnalyze` task 등록 확인
- `./gradlew dependencyCheckAnalyze --no-daemon`: NVD API key 없이 CVE DB 업데이트 단계가 장시간 지속되어 로컬에서는 중단
- `./gradlew dependencies --configuration runtimeClasspath --no-daemon`: 성공, Tomcat/Netty/PostgreSQL 보안 override 적용 확인
- `git diff --check`: 성공

## 잔여 리스크 및 권고

- 기존 DB에 남아 있는 OAuth 평문 토큰은 해당 사용자의 토큰이 실제로 사용될 때 암호문으로 전환된다. 즉시 일괄 전환이 필요하면 별도 운영 migration job이 필요하다.
- `APP_AUTH_COOKIE_SECURE=false`는 로컬 개발에는 필요하지만, HTTPS 운영 환경에서는 반드시 `true`로 설정해야 한다.
- OWASP Dependency-Check는 NVD API key 없이 초기 업데이트가 오래 걸릴 수 있으므로 CI secret에 NVD API key를 추가하는 것이 좋다.
- Dependabot version/security updates를 함께 활성화하면 SCA 탐지 이후 패치 PR 자동화까지 이어갈 수 있다.
