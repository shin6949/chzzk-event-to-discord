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
LABEL org.opencontainers.image.source=https://github.com/shin6949/streaming-alert-service
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
RUN useradd --system --uid 10001 --create-home appuser
COPY --from=build /app/build/libs/*.jar app.jar
RUN chown appuser:appuser app.jar
USER 10001:10001
ENTRYPOINT ["java", "-jar", "app.jar"]
