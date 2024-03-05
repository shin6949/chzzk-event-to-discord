# Chzzk Event To Discord
네이버 치지직의 방송 시작, 방송 종료 이벤트를 Discord Webhook을 통해 전송하는 Application 입니다.

## Dev Stack
- Java 17 (Microsoft OpenJDK 17)
- Spring Boot 3.2.3
- Gradle 8.5
- PostgreSQL 16
- Docker
- GitHub Action

## What can it receive?
- 방송 시작 이벤트 (STREAM_ONLINE)
- 방송 종료 이벤트 (STERAM_OFFLINE)
- 채널 정보 변경 이벤트 (CHANNEL_UPDATE)

## Environment variables
- APP_DB_URL: PostgreSQL JDBC URL
- APP_DB_USER: PostgreSQL Username
- APP_DB_PASSWORD: PostgreSQL Password
- APP_IS_TEST: Test Run 여부 (기본 false, true 시, 스케쥴링이 작동하지 않음)
- APP_INSERT_PASSWORD: Insert API Password를 지정합니다.
- CHZZK_API_CALL_INTERVAL: API 요청 주기 (단위: 초) - Default: 30
- CHZZK_API_URL: API 주소 (기본: "https://api.chzzk.naver.com")

## Docker Image
Docker Image: ghcr.io/shin6949/chzzk-event-to-discord:latest

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
    "channelId": [String] <If you have channel id. If you not have this, set null>,
    "channelName": [String] <If you don't have channel id or want to add by channel name>,
    "content": [String] <Content when the event is sent. it displayed as discord message>,
    "colorHex": [String] <Color code of the embed message that used at Embed color>,
    "subscriptionType": [String] <Type of subscription. You can see type top of document>
    "webhookId": [long] <If you want to use existing Webhook ID in database>,
    "webhookName": [String] <If you don't have webhook id or want to add by webhook name that used only at Inner Database>,
    "webhookUrl": [String] <If you don't have webhook id or want to add by webhook url that used with webhookName as pair>,
    "botProfileId": [long] <If you want to use existing Bot Profile ID in database. If you not have this, set null>,
    "botUsername": [String] <If you don't have bot profile id or want to add by bot username that used as Discord username of bot account. It used with botAvatarUrl as pair>,
    "botAvatarUrl": [String] <If you don't have bot profile id or want to add by bot avatar url that used as Discord avatar of bot account. It used with botUsername as pair>,
    "ownerChannelId": [String] <If you want to use existing Form Owner Channel ID>,
    "ownerChannelName": [String] <If you don't have owner channel id or want to add by owner channel name>,
    "intervalMinute": [int] <Interval minutes in case the notification comes multiple times within a short period of time>,
    "language": [String] <Language of the content. Default value is Korean>,
    "enabled": [boolean] <If you want to enable the subscription. Default value is true>
}
```
