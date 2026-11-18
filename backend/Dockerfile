# ---------- 1. Aşama: Maven ile derleme ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Bağımlılıkları önbelleğe almak için önce yalnızca pom.xml kopyalanır
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- 2. Aşama: Çalıştırma ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S teias && adduser -S teias -G teias \
    && mkdir -p /app/uploads && chown -R teias:teias /app
USER teias

COPY --from=build /build/target/teias-backend.jar app.jar

ENV UPLOAD_DIR=/app/uploads \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -Duser.timezone=Europe/Istanbul"

EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD wget -qO- http://127.0.0.1:8080/actuator/health/readiness | grep -q UP || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
