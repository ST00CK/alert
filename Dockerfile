# 1단계: Gradle 빌드 이미지
FROM gradle:7.6.0-jdk17 AS builder
WORKDIR /app

# Gradle 캐시 활용을 위해 build.gradle 및 settings.gradle만 먼저 복사
COPY build.gradle settings.gradle gradle/ ./
RUN gradle dependencies --no-daemon || true

# 프로젝트 소스 복사 및 JAR 빌드
COPY . .
RUN gradle bootJar --no-daemon

# 2단계: 실행 이미지
FROM openjdk:17-jdk-slim
WORKDIR /app

# 빌드 결과물 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
