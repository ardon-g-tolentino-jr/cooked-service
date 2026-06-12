# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Cache dependencies before copying source
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /build/target/cooked-service-0.0.1-SNAPSHOT.jar app.jar

RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8082

# All sensitive values are injected at runtime via environment variables.
# Spring Boot's relaxed binding maps UPPER_SNAKE_CASE env vars to property keys.
ENV CUSTOM_DB_URL=jdbc:postgresql://localhost:5432/cooked \
    CUSTOM_DB_USER=cooked_user \
    CUSTOM_DB_PASS= \
    JWT_SECRET= \
    JWT_EXPIRATION_MS=86400000

ENTRYPOINT ["java", "-jar", "app.jar"]