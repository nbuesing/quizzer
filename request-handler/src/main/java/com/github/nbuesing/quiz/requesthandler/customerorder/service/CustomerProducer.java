package com.github.nbuesing.quiz.requesthandler.customerorder.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nbuesing.quiz.requesthandler.customerorder.domain.Customer;
import com.github.nbuesing.quiz.requesthandler.properties.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;

/**
 * Typically would be in a separate customer service project. Added to request-handler project to save time during the on-site exercises.
 */
@Component
@Slf4j
public class CustomerProducer {

    private final ApplicationProperties applicationProperties;
    private final KafkaProducer<String, Object> kafkaProducer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomerProducer(
            final ApplicationProperties applicationProperties,
            final KafkaProducer<String, Object> kafkaProducer) {
        this.applicationProperties = applicationProperties;
        this.kafkaProducer = kafkaProducer;
    }

    public void publish(Customer customer) {
        String json = toJson(customer);
        log.info("json: {}", json);
        kafkaProducer.send(new ProducerRecord<>(applicationProperties.getQuestionTopic(), null, null, customer.getId(), json));
    }


    private String toJson(final Customer customer) {
        String value;
        try {
            value = objectMapper.writeValueAsString(customer);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return value;
    }
}
