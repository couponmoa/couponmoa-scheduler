FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/app.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]