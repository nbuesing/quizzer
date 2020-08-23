package com.github.nbuesing.quiz.requesthandler.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.nbuesing.quiz.model.User;
import com.github.nbuesing.quiz.requesthandler.properties.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.allegro.schema.json2avro.converter.JsonAvroConverter;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RequestMapping(path = "/test")
@RestController
@Slf4j
public class TestController extends AbstractController {

    public TestController(
            final ApplicationProperties applicationProperties,
            final KafkaProducer<String, Object> kafkaProducer,
            final JsonAvroConverter jsonAvroConverter) {
        super(applicationProperties, kafkaProducer, jsonAvroConverter);
    }

    @PostMapping
    public ResponseEntity<JsonNode> post(@RequestBody final JsonNode data) {

        ObjectNode object = JsonNodeFactory.instance.objectNode();

        HttpHeaders httpHeaders = new HttpHeaders();

        String requestId = UUID.randomUUID().toString();

        httpHeaders.add("Location", "http://localhost:9080/test/" + requestId);

        log.info("post!!!! " + requestId);

        return new ResponseEntity<>(null, httpHeaders, HttpStatus.SEE_OTHER);
    }

    @GetMapping(path = "/{requestId}")
    public ResponseEntity<JsonNode> get(@PathVariable(name = "requestId") String requestId)  {

        log.info("get!!!! " + requestId);

//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.add("Location", "http://localhost:9080/test/" + requestId);
//        if (true) {
//            return new ResponseEntity<>(null, httpHeaders, HttpStatus.SEE_OTHER);
//        }

        ObjectNode object = JsonNodeFactory.instance.objectNode();
        object.put("request-id", requestId);
        return new ResponseEntity<>(object, HttpStatus.OK);
    }
}
