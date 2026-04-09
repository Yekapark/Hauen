# ── 1단계: 빌드 ──────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Maven wrapper 및 pom.xml 복사
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# 의존성 먼저 다운로드 (캐시 활용)
RUN ./mvnw dependency:go-offline -B

# 소스 복사 후 빌드
COPY src src
RUN ./mvnw package -DskipTests

# ── 2단계: 실행 ──────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
