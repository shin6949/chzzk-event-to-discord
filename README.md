# Chzzk Event To Discord
네이버 치지직의 방송 시작, 방송 종료 이벤트를 Discord Webhook을 통해 전송하는 Application 입니다.

## Dev Stack
- Java 17 (Microsoft OpenJDK 17)
- Spring Boot 3.5.8
- Gradle 8.5
- PostgreSQL 16
- Docker
- GitHub Action

## What can it receive?
- 방송 시작 이벤트 (STREAM_ONLINE)
- 방송 종료 이벤트 (STERAM_OFFLINE)
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
Docker Image: ghcr.io/shin6949/chzzk-event-to-discord:latest

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
