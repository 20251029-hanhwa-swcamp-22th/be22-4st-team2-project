FROM amazoncorretto:21 AS build
WORKDIR /workspace/app

COPY . .

RUN ./gradlew clean build -x test

FROM amazoncorretto:21
VOLUME /tmp
COPY --from=build /workspace/app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
