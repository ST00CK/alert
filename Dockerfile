# Gradle과 JDK를 포함한 이미지 사용
FROM gradle:8.0-jdk17 AS build

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 캐시 설정 (캐시 디렉토리 명시)
ENV GRADLE_USER_HOME=/app/gradle-cache

# 소스 코드 복사
COPY . .

# Gradle 빌드 실행
RUN gradle clean build --no-daemon

# 최종 이미지 설정
FROM openjdk:17
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
