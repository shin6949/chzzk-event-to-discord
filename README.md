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
- 방송 시작 이벤트
- 방송 종료 이벤트
- 방송 제목 변경 이벤트
- 방송 게임 변경 이벤트

## Environment variables
- DB_URL: PostgreSQL JDBC URL
- DB_USER: PostgreSQL Username
- DB_PASSWORD: PostgreSQL Password
- API_CALL_INTERVAL: API 요청 주기 (단위: 초) - Default: 30

## Docker Image
Docker Image: ghcr.io/shin6949/chzzk-event-to-discord:latest

## Notice
아직 Chzzk의 정식 API가 나오지 않은 관계로 일정 주기에 따라 API에 요청하여 시간 단위로 데이터를 비교하여 이벤트를 전송하고 있습니다.  
이 Application은 설정한 주기에 따라 API에 요청을 보냅니다. 서버에 부하를 주지 않기 위해 Database에 데이터를 일부 캐싱하는 등의 방법을 사용 중이지만, 너무 짧은 주기로 설정할 경우 서버에 부하를 줄 수 있으며, 차단될 수 있습니다.   
추후, Chzzk에서 Webhook 형태의 API를 제공할 경우를 대비하여 Spring Boot로 구현하였으며, 해당 API가 나오면 변경할 예정입니다.

## How to use
현재는 Database에 데이터를 수동으로 저장해야합니다.
