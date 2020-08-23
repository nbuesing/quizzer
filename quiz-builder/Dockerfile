FROM openjdk:8-jdk-slim

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS="" \
    ENVIRONMENT=""

COPY ./build/libs/*.jar /app.jar

CMD java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.jar --spring.profiles.active=${ENVIRONMENT}
