# Gradle과 JDK를 포함한 빌드 이미지
FROM gradle:8.0-jdk17 AS build

# Gradle 캐시 디렉토리와 권한 설정
WORKDIR /app
RUN mkdir -p /app/gradle-cache && chmod -R 777 /app/gradle-cache

# 프로젝트 파일 복사
COPY . .

# Gradle 빌드 실행
RUN gradle clean build --no-daemon -g /app/gradle-cache

# 최종 런타임 이미지 생성
FROM openjdk:17
COPY --from=build /app/build/libs/User-0.0.1-SNAPSHOT.jar /app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
