# 1단계: 빌드 단계
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

RUN chmod +x gradlew && \
    ./gradlew bootJar -x test --no-daemon && \
    find build/libs/ -name "*.jar" ! -name "*-plain.jar" -exec mv {} build/libs/app.jar \;

# 2단계: 실행 단계
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/app.jar .

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

