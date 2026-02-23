# 얇은 런타임 이미지: Gradle 빌드는 Jenkins에서 완료됨
# → Docker는 빌드된 JAR를 복사해 실행만 담당 (이중 빌드 제거)
FROM --platform=linux/amd64 amazoncorretto:21

RUN yum install -y shadow-utils && \
    groupadd -r appuser && useradd -r -g appuser -d /app appuser && \
    yum clean all

WORKDIR /app
COPY build/libs/*.jar app.jar
RUN chown -R appuser:appuser /app

USER appuser

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]
