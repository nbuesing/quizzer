package com.github.nbuesing.quiz.requesthandler.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nbuesing.quiz.requesthandler.domain.Request;
import com.github.nbuesing.quiz.requesthandler.properties.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RequestProducer {

    private final ApplicationProperties applicationProperties;
    private final KafkaProducer<String, Object> kafkaProducer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public RequestProducer(
            final ApplicationProperties applicationProperties,
            final KafkaProducer<String, Object> kafkaProducer) {
        this.applicationProperties = applicationProperties;
        this.kafkaProducer = kafkaProducer;
    }

    public void publish(Request request) {
        String json = toJson(request);
        log.info("json: {}", json);
        kafkaProducer.send(new ProducerRecord<>(applicationProperties.getQuestionTopic(), null, null, request.getRequestId(), json));
    }


    private String toJson(final Request request) {
        String value;
        try {
            value = objectMapper.writeValueAsString(request);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return value;
    }
}
