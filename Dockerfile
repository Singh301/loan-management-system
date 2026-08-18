# ===== Build Stage =====
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B clean package -DskipTests

# ===== Runtime Stage =====
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring \
    && mkdir -p /app/logs /app/uploads \
    && chown -R spring:spring /app

USER spring:spring

COPY --from=build /app/target/loan-management-system-*.jar app.jar

EXPOSE 8080

# Actuator health endpoint (requires management.endpoint.health exposed)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
