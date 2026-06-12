# Streaming Alert Service
네이버 치지직의 방송 시작, 방송 종료 이벤트를 Discord Webhook을 통해 전송하는 Application 입니다.

## 프로젝트 역할
이 프로젝트는 치지직(CHZZK) 채널 상태를 주기적으로 확인하고, 사용자가 설정한 구독 조건에 맞는 이벤트가 발생하면 Discord Webhook으로 알림을 전송하는 중계 애플리케이션입니다. 치지직에서 공식 Webhook을 직접 제공하지 않는 상황을 전제로 하며, 애플리케이션이 일정 간격으로 치지직 API를 조회한 뒤 이전에 저장된 채널 상태와 새 API 응답을 비교하여 이벤트를 판단합니다.

주요 역할은 다음과 같습니다.

- **치지직 채널 상태 감시**: 활성화된 구독 폼에 등록된 채널 ID를 기준으로 치지직 API를 조회합니다. 조회 주기는 `CHZZK_CHECK_INTERVAL` 환경변수로 제어하며 기본값은 30초입니다.
- **이벤트 분류**: 데이터베이스에 저장된 이전 채널 정보와 API에서 새로 가져온 채널 정보를 비교하여 방송 시작(`STREAM_ONLINE`), 방송 종료(`STREAM_OFFLINE`), 채널 정보 변경(`CHANNEL_UPDATE`) 이벤트를 구분합니다.
- **Discord 알림 전송**: 이벤트가 발생하면 구독 폼에 연결된 Discord Webhook URL로 메시지를 전송합니다. 알림에는 사용자가 설정한 본문, Embed 색상, 봇 표시 이름, 봇 아바타 URL이 반영됩니다.
- **방송 시작 상세 정보 구성**: 방송 시작 이벤트의 경우 라이브 제목, 카테고리, 카테고리 포스터, 방송 썸네일, 채팅 제한 조건, 성인 방송 여부, 시청자 수, 태그 등을 설정에 따라 Discord Embed에 포함할 수 있습니다.
- **중복 알림 방지**: 구독 폼별 알림 로그를 저장하고, `intervalMinute` 설정 시간 안에 같은 구독으로 발송된 알림이 있으면 중복 전송을 막습니다.
- **구독 설정 관리**: 치지직 채널, Discord Webhook, Discord 봇 프로필, 알림 언어, 알림 간격, 활성화 여부, 이벤트 유형 같은 구독 정보를 데이터베이스에 저장하고 관리합니다.
- **사용자 인증 및 구독 CRUD 제공**: 치지직 OAuth 로그인으로 사용자를 식별하고, 인증된 사용자가 본인 소유 구독을 생성, 조회, 수정, 삭제할 수 있는 `/api/v1/subscriptions` API를 제공합니다. 관리자는 전체 구독을 조회할 수 있도록 권한 모델이 분리되어 있습니다.
- **프론트엔드 관리 화면 제공**: React/Vite 기반 프론트엔드는 치지직 로그인 진입, 구독 목록 조회, 구독 생성, 구독 수정 및 삭제 화면을 제공합니다.
- **배포 지원**: Spring Boot 백엔드와 정적 프론트엔드 컨테이너를 분리해 Docker Compose로 실행할 수 있으며, 백엔드는 데이터베이스 연결, 치지직 API/OAuth 설정, Discord Webhook 발송을 담당합니다.

전체 동작 흐름은 다음과 같습니다.

1. 사용자가 구독할 치지직 채널, 알림 유형, Discord Webhook, 봇 프로필, 알림 옵션을 등록합니다.
2. 백엔드 스케줄러가 활성화된 구독을 조회하고, 필요한 치지직 채널 ID 목록을 수집합니다.
3. 각 채널의 현재 상태를 치지직 API에서 가져오고, 데이터베이스에 저장된 이전 상태와 비교합니다.
4. 방송 시작, 방송 종료, 채널 정보 변경 중 어떤 이벤트인지 판단합니다.
5. 해당 이벤트를 구독 중인 활성 구독 폼만 필터링하고, 최근 알림 로그를 확인해 중복 발송 여부를 검사합니다.
6. 이벤트별 Discord Embed 메시지를 생성한 뒤 Webhook URL로 전송합니다.
7. 발송이 완료되면 알림 로그를 저장하여 이후 중복 알림 방지에 사용합니다.

즉, 이 프로젝트의 핵심 목적은 **치지직의 방송/채널 이벤트를 Discord 서버에서 바로 확인할 수 있도록 자동화하는 것**입니다. 치지직 API와 Discord Webhook 사이에서 이벤트 감지, 메시지 변환, 구독 관리, 중복 제어를 담당하는 백엔드 중심의 알림 브리지이며, 현재는 이를 관리하기 위한 웹 프론트엔드도 함께 포함하고 있습니다.

## Dev Stack
- Java 17 (Microsoft OpenJDK 17)
- Spring Boot 3.5.8
- Gradle 8.5
- PostgreSQL 16
- Docker
- GitHub Action

## What can it receive?
- 방송 시작 이벤트 (STREAM_ONLINE)
- 방송 종료 이벤트 (STREAM_OFFLINE)
- 채널 정보 변경 이벤트 (CHANNEL_UPDATE)

## Environment variables
- Backend required:
  - `APP_DB_DRIVER`, `APP_DB_URL`, `APP_DB_USER`, `APP_DB_PASSWORD`
  - `CHZZK_OAUTH_CLIENT_ID`, `CHZZK_OAUTH_CLIENT_SECRET`, `CHZZK_OAUTH_REDIRECT_URI`
- Backend optional (defaults):
  - `CHZZK_OAUTH_AUTH_BASE_URL` (`https://chzzk.naver.com`)
  - `CHZZK_OAUTH_TOKEN_BASE_URL` (`https://openapi.chzzk.naver.com`)
  - `CHZZK_OAUTH_API_BASE_URL` (`https://openapi.chzzk.naver.com`)
  - `CHZZK_CHECK_INTERVAL` (`30`)
  - `CHZZK_API_URL` (`https://api.chzzk.naver.com`)
  - `APP_DEFAULT_TIMEZONE` (`Asia/Seoul`)
  - `APP_IS_TEST` (`false`)
  - `APP_INSERT_PASSWORD` (empty)
- Frontend:
  - `VITE_API_BASE_URL`
  - Local example: `http://localhost:8080/api/v1`
  - `docker-compose` example: `http://backend:8080/api/v1`

For full setup details, see `docs/DEPLOY.md`.

## Docker Image
Docker Image: ghcr.io/shin6949/streaming-alert-service:latest

## Deployment

- Compose-based FE/BE deployment notes: `docs/DEPLOY.md`

## Notice
아직 Chzzk의 정식 API가 나오지 않은 관계로 일정 주기에 따라 API에 요청하여 시간 단위로 데이터를 비교하여 이벤트를 전송하고 있습니다.  
이 Application은 설정한 주기에 따라 API에 요청을 보냅니다. 서버에 부하를 주지 않기 위해 Database에 데이터를 일부 캐싱하는 등의 방법을 사용 중이지만, 너무 짧은 주기로 설정할 경우 서버에 부하를 줄 수 있으며, 차단될 수 있습니다.   
추후, Chzzk에서 Webhook 형태의 API를 제공할 경우를 대비하여 Spring Boot로 구현하였으며, 해당 API가 나오면 변경할 예정입니다.

## How to use
현재는 Database에 데이터를 수동으로 저장해야합니다.

### Insert API 사용 방법
Frontend가 만들어질 때까지는 임시적으로 Insert API를 제공합니다.  
Insert API를 사용하기 위해서는 `APP_INSERT_PASSWORD` 환경변수를 설정해야합니다.  

#### Insert API endpoint
- POST /form/insert

#### Request Sample Header
Authorization: Bearer {APP_INSERT_PASSWORD}

#### Request Sample Body
```
{
    "channelId": [String] <이벤트를 구독할 채널 ID가 있으면 입력하세요. 모른다면 null로 설정하세요.>,
    "channelName": [String] <채널 ID가 없거나 채널 이름으로 추가하고 싶을 때 여기에 채널 이름을 설정하세요.>,
    "content": [String] <이벤트 발송 시 내용. 디스코드 메시지로 표시됩니다.>,
    "colorHex": [String] <임베드 색상에 사용되는 색상 코드. 이 코드는 Discord의 Embed의 띠 색으로 활용됩니다.>,
    "subscriptionType": [String] <이벤트 구독 유형. 문서 상단에서 유형을 볼 수 있습니다.>,
    "webhookId": [long] <DB에 있는 Webhook ID를 사용하고 싶을 때>,
    "webhookName": [String] <등록하고자 하는 Webhook이 DB에 없을 때, webhook을 구분할 이름>,
    "webhookUrl": [String] <등록하고자 하는 Webhook이 DB에 없을 때, 등록할 Webhook URL. webhookName을 설정했다면 같이 설정되어야합니다.>,
    "botProfileId": [long] <DB에 있는 Bot Profile ID를 사용하고 싶을 때. 없으면 null로 설정하세요.>,
    "botUsername": [String] <bot profile id가 없거나 bot 계정의 Discord 사용자 이름으로 추가하고 싶을 때. botAvatarUrl과 짝을 이룹니다.>,
    "botAvatarUrl": [String] <bot profile id가 없거나 bot 계정의 Discord 봇의  URL로 추가하고 싶을 때. botUsername과 짝을 이룹니다.>,
    "ownerChannelId": [String] <기존 폼 소유주 채널 ID를 사용하고 싶을 때>,
    "ownerChannelName": [String] <소유주 채널 ID가 없거나 소유주 채널 이름으로 추가하고 싶을 때>,
    "intervalMinute": [int] <짧은 시간 내에 여러 번 알림이 올 경우 알림 간격 분>,
    "showDetail": [boolean] <이벤트의 세부 정보를 표시하고 싶을 때. STREAM_ONLINE 이벤트일 때, 이 옵션이 의미가 있습니다. 기본값은 false입니다.>,
    "language": [String] <콘텐츠의 언어. 기본값은 한국어입니다.>,
    "enabled": [boolean] <구독을 활성화하고 싶으면 true로 설정하세요. 기본값은 true입니다.>
}
```
