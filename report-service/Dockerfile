# ============================================================
# JVM build
# ============================================================
FROM eclipse-temurin:25-jdk-jammy AS build
WORKDIR /app

COPY gradlew .
COPY gradle/ gradle/

RUN chmod +x gradlew

COPY build.gradle settings.gradle ./
COPY report-service/build.gradle report-service/
COPY transaction-service/build.gradle transaction-service/

RUN ./gradlew dependencies --no-daemon || true

COPY report-service/src/ report-service/src/
COPY transaction-service/src/ transaction-service/src/

RUN ./gradlew :report-service:bootJar -x test --no-daemon

# ============================================================
# Runtime
# ============================================================
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=build /app/report-service/build/libs/*.jar app.jar

EXPOSE 8380
ENTRYPOINT ["java", "-jar", "app.jar"]