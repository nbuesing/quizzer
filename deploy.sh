#!/bin/sh

DOCKER_USER=nbuesing

docker push ${DOCKER_USER}/request-handler:2.0.0
docker push ${DOCKER_USER}/quizzer:2.0.0
docker push ${DOCKER_USER}/quiz-builder:2.0.0
docker push ${DOCKER_USER}/quizzer-ui:2.0.0
docker push ${DOCKER_USER}/nginx:2.0.0

