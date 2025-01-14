# Gradle과 JDK를 포함한 빌드 이미지
FROM gradle:8.5-jdk17 AS build

# 작업 디렉토리 설정 및 권한 수정
WORKDIR /app
COPY --chown=gradle:gradle . .

# Gradle 빌드 실행 (Daemon 활성화)
RUN gradle clean build --no-daemon --info -g /app/gradle-cache

# 최종 런타임 이미지 생성
FROM openjdk:17
COPY --from=build /app/build/libs/grpcServer-0.0.1-SNAPSHOT.jar /app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
