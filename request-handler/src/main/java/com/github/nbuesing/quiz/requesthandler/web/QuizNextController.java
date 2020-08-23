package com.github.nbuesing.quiz.requesthandler.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.nbuesing.quiz.QuizQuestion;
import com.github.nbuesing.quiz.requesthandler.properties.ApplicationProperties;
import com.github.nbuesing.quiz.requesthandler.service.CorrelationService;
import com.github.nbuesing.quiz.requesthandler.service.CorrelationService2;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import tech.allegro.schema.json2avro.converter.JsonAvroConverter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

//@RequestMapping(path = "/quizzes/next_xx")
//@RestController
@Slf4j
public class QuizNextController extends AbstractController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
//    private static final Schema QUIZ_NEXT;
//    static {
//        try {
//            QUIZ_NEXT = new Schema.Parser().parse(UserController.class.getResourceAsStream("/schemas/quiz_question.avsc"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    private final CorrelationService correlationService;
    private final CorrelationService2 correlationService2;

    private final JsonAvroConverter jsonAvroConverter;

    public QuizNextController(
            final ApplicationProperties applicationProperties,
            final KafkaProducer<String, Object> kafkaProducer,
            CorrelationService correlationService, CorrelationService2 correlationService2, final JsonAvroConverter jsonAvroConverter) {
        super(applicationProperties, kafkaProducer, jsonAvroConverter);
        this.correlationService = correlationService;
        this.correlationService2 = correlationService2;
        this.jsonAvroConverter = jsonAvroConverter;
    }


    @PostMapping
    public JsonNode post(@RequestBody final JsonNode data) throws IOException, InterruptedException, ExecutionException {

        ((ObjectNode) data).put("request_id", generateRequestId());

        // GenericRecord answer = jsonAvroConverter.convertToGenericDataRecord(data, QUIZ_NEXT);
        //QuizQuestion question = jsonAvroConverter.convertToSpecificRecord(data, QuizQuestion.class, QuizQuestion.getClassSchema());
        QuizQuestion question = toSpecificRecord(data, QuizQuestion.class, QuizQuestion.getClassSchema());

//        final DeferredResult<ResponseEntity<JsonNode>> deferredResult = correlationService2.waitForResponse(question.getRequestId());
        SpecificRecordBase response = correlationService.waitForResponse(question.getRequestId());

        try {
            log.info(OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(question));
        } catch (final JsonProcessingException e) {
        }

        Headers headers = new RecordHeaders();
        headers.add("request_id", question.getRequestId().getBytes(StandardCharsets.UTF_8));

        Future<RecordMetadata> future = kafkaProducer.send(
                new ProducerRecord<>(
                        applicationProperties.getQuizNextTopic(),
                        null,
                        (String) question.getQuizId(),
                        question,
                        headers)
        );

        RecordMetadata metadata = future.get();
        System.out.println(metadata.offset());


        return OBJECT_MAPPER.readValue(jsonAvroConverter.convertToJson(question), JsonNode.class);
    }
}
