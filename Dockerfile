# 첫 번째 스테이지: Gradle Wrapper와 의존성을 캐시하기 위해 설정
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu AS cache
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
RUN chmod +x ./gradlew && ./gradlew --no-daemon --version
RUN ./gradlew --no-daemon clean build --stacktrace

# 두 번째 스테이지: 실제 빌드를 진행
FROM cache AS build
COPY src src
RUN ./gradlew --no-daemon build

# 최종 스테이지: 빌드된 애플리케이션을 실행하기 위한 환경 설정
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu
LABEL org.opencontainers.image.source=https://github.com/shin6949/chzzk-event-to-discord
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
