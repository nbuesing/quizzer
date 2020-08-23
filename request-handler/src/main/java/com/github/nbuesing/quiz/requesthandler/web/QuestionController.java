package com.github.nbuesing.quiz.requesthandler.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.nbuesing.quiz.model.Question;
import com.github.nbuesing.quiz.requesthandler.properties.ApplicationProperties;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.allegro.schema.json2avro.converter.JsonAvroConverter;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RequestMapping(path = "/questions")
@RestController
public class QuestionController extends AbstractController {

    public QuestionController(
            final ApplicationProperties applicationProperties,
            final KafkaProducer<String, Object> kafkaProducer,
            final JsonAvroConverter jsonAvroConverter) {
        super(applicationProperties, kafkaProducer, jsonAvroConverter);
    }

    @PostMapping
    public JsonNode post(@RequestBody final byte[] data) throws IOException, InterruptedException, ExecutionException {

        JsonNode jsonNode = toJsonNode(data);

        if (jsonNode.isArray()) {

            for (JsonNode node : jsonNode) {
                Question question = toSpecificRecord(node, Question.class, Question.getClassSchema());

                Future<RecordMetadata> future = kafkaProducer.send(
                        new ProducerRecord<>(
                                applicationProperties.getQuestionTopic(),
                                question.getQuestionId(),
                                question)
                );
            }

            return jsonNode;

        } else {
            Question question = toSpecificRecord(jsonNode, Question.class, Question.getClassSchema());

            Future<RecordMetadata> future = kafkaProducer.send(
                    new ProducerRecord<>(
                            applicationProperties.getQuestionTopic(),
                            question.getQuestionId(),
                            question)
            );

            return toJsonNode(question);
        }
    }
}
