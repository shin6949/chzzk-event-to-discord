FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu AS cache
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
RUN chmod +x ./gradlew && ./gradlew --no-daemon --version
RUN ./gradlew --no-daemon clean build --stacktrace

FROM cache AS build
COPY src src
RUN ./gradlew --no-daemon build

FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu
LABEL org.opencontainers.image.source=https://github.com/shin6949/chzzk-event-to-discord
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
