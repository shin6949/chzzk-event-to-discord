FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean && ./gradlew build

FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu
LABEL org.opencontainers.image.source=https://github.com/shin6949/chzzk-event-to-discord

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
