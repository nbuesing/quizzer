package com.github.nbuesing.quiz.requesthandler.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nbuesing.quiz.model.User;
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

@RequestMapping(path = "/users")
@RestController

public class UserController extends AbstractController {

    public UserController(
            final ApplicationProperties applicationProperties,
            final KafkaProducer<String, Object> kafkaProducer,
            final JsonAvroConverter jsonAvroConverter) {
        super(applicationProperties, kafkaProducer, jsonAvroConverter);
    }


    @PostMapping
    public JsonNode post(@RequestBody final JsonNode data) throws ExecutionException, InterruptedException {

        User user = toSpecificRecord(data, User.class, User.getClassSchema());

        Future<RecordMetadata> future = kafkaProducer.send(
                new ProducerRecord<>(
                        applicationProperties.getUserTopic(),
                        user.getUserId(),
                        user)
        );

        RecordMetadata metadata = future.get();
        System.out.println(metadata.offset());

        return toJsonNode(user);
    }
}
