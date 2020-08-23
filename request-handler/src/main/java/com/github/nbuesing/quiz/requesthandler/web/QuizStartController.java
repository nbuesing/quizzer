package com.github.nbuesing.quiz.requesthandler.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.nbuesing.quiz.QuizQuestion;
import com.github.nbuesing.quiz.QuizStart;
import com.github.nbuesing.quiz.QuizSubmission;
import com.github.nbuesing.quiz.requesthandler.properties.ApplicationProperties;
import com.github.nbuesing.quiz.requesthandler.service.CorrelationService;
import com.github.nbuesing.quiz.requesthandler.service.CorrelationService2;
import com.github.nbuesing.quiz.requesthandler.service.ResponseConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.http.HttpStatus;
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
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

@RequestMapping(path = "/quizzes/start")
@RestController
@Slf4j
public class QuizStartController extends AbstractController {

    private final CorrelationService correlationService;
    private final CorrelationService2 correlationService2;
    private final ResponseConsumer responseConsumer;

    private ForkJoinPool forkJoinPool = new ForkJoinPool(100);

    public QuizStartController(
            final ApplicationProperties applicationProperties,
            final KafkaProducer<String, Object> kafkaProducer,
            final JsonAvroConverter jsonAvroConverter,
            final CorrelationService correlationService,
            final CorrelationService2 correlationService2,
            final ResponseConsumer responseConsumer
    ) {
        super(applicationProperties, kafkaProducer, jsonAvroConverter);
        this.correlationService = correlationService;
        this.correlationService2 = correlationService2;
        this.responseConsumer = responseConsumer;
    }


    @PostMapping
    public ResponseEntity<JsonNode> post(@RequestBody final JsonNode data) throws IOException, InterruptedException, ExecutionException {


//        ((ObjectNode) data).put("quiz_id", generateQuizId());
        ((ObjectNode) data).put("request_id", generateRequestId());

        QuizStart startQuiz = toSpecificRecord(data, QuizStart.class, QuizStart.getClassSchema());


        if (!responseConsumer.willGetResponse(startQuiz.getQuizId())) {
            log.info("DEFERRED");
            //TODO -- 406 ....
            ResponseEntity<JsonNode> responseEntity = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
            //deferredResult.setResult(responseEntity);
            return responseEntity;
        }


        log.info("request payload={}", debug(startQuiz));

        Headers headers = new RecordHeaders();
        headers.add("request_id", startQuiz.getRequestId().getBytes(StandardCharsets.UTF_8));

        Future<RecordMetadata> future = kafkaProducer.send(
                new ProducerRecord<>(
                        applicationProperties.getQuizStartTopic(),
                        null,
                        startQuiz.getQuizId(),
                        startQuiz,
                        headers)
        );

        SpecificRecordBase response = correlationService.waitForResponse(startQuiz.getRequestId());

        final JsonNode jsonNode = toJsonNode(response);

        log.info("request payload={}", debug(jsonNode));

        return new ResponseEntity<>(jsonNode, HttpStatus.OK);
    }


   // @PostMapping
    public DeferredResult<ResponseEntity<JsonNode>> post2(@RequestBody final JsonNode data) {

        ((ObjectNode) data).put("quiz_id", RandomStringUtils.randomAlphabetic(10).toLowerCase());
        ((ObjectNode) data).put("request_id", generateRequestId());

        final QuizStart startQuiz = toSpecificRecord(data, QuizStart.class, QuizStart.getClassSchema());

        System.out.println(startQuiz);

        final DeferredResult<ResponseEntity<JsonNode>> deferredResult = correlationService2.waitForResponse(startQuiz.getRequestId());

        if (!responseConsumer.willGetResponse(startQuiz.getQuizId())) {
            ResponseEntity<JsonNode> responseEntity = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
            deferredResult.setResult(responseEntity);
            log.info("the response will not be handled by this machine, so not_acceptable to send here.");
            return deferredResult;
        }

        // ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.submit(() -> {

            log.info("Sending k={}, v={}", startQuiz.getQuizId(), startQuiz);

            Headers headers = new RecordHeaders();
            headers.add("request_id", startQuiz.getRequestId().getBytes(StandardCharsets.UTF_8));

            Future<RecordMetadata> future = kafkaProducer.send(
                    new ProducerRecord<>(
                            applicationProperties.getQuizStartTopic(),
                            null,
                            startQuiz.getQuizId(),
                            startQuiz,
                            headers)
            );

            try {
                System.out.println(future.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });

        return deferredResult;
    }

}
