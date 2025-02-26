FROM gradle:jdk21 AS builder
WORKDIR /home/app
COPY --chown=gradle:gradle . .
RUN gradle build --no-daemon -x test --build-cache
FROM openjdk:21-jdk-slim-buster
WORKDIR /app
RUN groupadd -g 1000 appuser && useradd -u 1000 -g appuser -s /bin/bash appuser
RUN mkdir -p /app && chown -R appuser:appuser /app
COPY --from=builder /home/app/build/libs/app-0.0.1-SNAPSHOT.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]