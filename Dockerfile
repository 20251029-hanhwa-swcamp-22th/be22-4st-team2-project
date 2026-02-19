# --platform=linux/amd64 : Mac Apple Silicon(M1/M2/M3)에서 Gradle 빌드 native 모듈 이슈 방지
FROM --platform=linux/amd64 amazoncorretto:21 AS build
WORKDIR /workspace/app

COPY . .

RUN ./gradlew clean build -x test

FROM --platform=linux/amd64 amazoncorretto:21
VOLUME /tmp
COPY --from=build /workspace/app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
