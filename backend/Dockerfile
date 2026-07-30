FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    UPLOAD_IMAGE_DIR=uploads/produtos

RUN addgroup -S app \
    && adduser -S app -G app \
    && mkdir -p /app/uploads/produtos \
    && chown -R app:app /app

COPY --from=build --chown=app:app /workspace/target/*.jar /app/app.jar

USER app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 CMD wget --no-verbose --tries=1 --spider "http://localhost:${SERVER_PORT:-8080}/api/health" || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
