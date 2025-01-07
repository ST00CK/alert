# Gradle과 JDK를 포함한 이미지 사용
FROM gradle:7.5.1-jdk11 AS build

# 작업 디렉토리 설정
WORKDIR /app

# 소스 코드 복사
COPY . .

# Gradle 빌드 실행
RUN gradle clean build --no-daemon

# 최종 이미지 설정 (필요에 따라 추가)
FROM openjdk:11-jre-slim
COPY --from=build /app/build/libs/your-app.jar /app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "/app.jar"]
