package com.github.nbuesing.quiz.requesthandler.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.nbuesing.quiz.QuizStart;
import com.github.nbuesing.quiz.QuizSubmission;
import com.github.nbuesing.quiz.requesthandler.properties.ApplicationProperties;
import org.apache.avro.Schema;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.web.servlet.view.AbstractTemplateView;
import tech.allegro.schema.json2avro.converter.JsonAvroConverter;

import java.io.IOException;
import java.util.UUID;

public abstract class AbstractController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final JsonAvroConverter jsonAvroConverter;

    protected final ApplicationProperties applicationProperties;
    protected final KafkaProducer<String, Object> kafkaProducer;


    protected AbstractController(
            final ApplicationProperties applicationProperties,
            final KafkaProducer<String, Object> kafkaProducer,
            final JsonAvroConverter jsonAvroConverter) {
        this.applicationProperties = applicationProperties;
        this.kafkaProducer = kafkaProducer;
        this.jsonAvroConverter = jsonAvroConverter;
    }


    protected <T extends SpecificRecordBase & SpecificRecord> T toSpecificRecord(final JsonNode data, Class<T> clazz, final Schema schema) {
        try {
            return toSpecificRecord(OBJECT_MAPPER.writeValueAsBytes(data), clazz, schema);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T extends SpecificRecordBase & SpecificRecord> T toSpecificRecord(final byte[] data, Class<T> clazz, final Schema schema) {
        return jsonAvroConverter.convertToSpecificRecord(data, clazz, schema);
    }

    protected JsonNode toJsonNode(final byte[] bytes) {
        try {
            return OBJECT_MAPPER.readValue(bytes, JsonNode.class);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected JsonNode toJsonNode(SpecificRecordBase specificRecord) {
        try {
            return OBJECT_MAPPER.readValue(jsonAvroConverter.convertToJson(specificRecord), JsonNode.class);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String debug(final SpecificRecordBase specificRecord) {
        try {
            final byte[] bytes = jsonAvroConverter.convertToJson(specificRecord);
            final JsonNode jsonNode = OBJECT_MAPPER.readValue(bytes, JsonNode.class);
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String debug(final JsonNode jsonNode) {
        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

//    protected String generateQuizId() {
//        return RandomStringUtils.randomAlphabetic(10).toLowerCase();
//    }

    protected String generateRequestId() {
        return UUID.randomUUID().toString();
    }
}
