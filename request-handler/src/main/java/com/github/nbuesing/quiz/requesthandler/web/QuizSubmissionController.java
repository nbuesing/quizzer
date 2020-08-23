package com.github.nbuesing.quiz.requesthandler.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.nbuesing.quiz.QuizQuestion;
import com.github.nbuesing.quiz.QuizSubmission;
import com.github.nbuesing.quiz.model.QuizResult;
import com.github.nbuesing.quiz.requesthandler.properties.ApplicationProperties;
import com.github.nbuesing.quiz.requesthandler.service.CorrelationService;
import com.github.nbuesing.quiz.requesthandler.service.CorrelationService2;
import com.github.nbuesing.quiz.requesthandler.service.ResponseConsumer;
import io.confluent.ksql.api.client.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import tech.allegro.schema.json2avro.converter.JsonAvroConverter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

@RequestMapping(path = "/quizzes/answer")
@RestController
@Slf4j
public class QuizSubmissionController extends AbstractController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final CorrelationService correlationService;
    private final CorrelationService2 correlationService2;
    private final ResponseConsumer responseConsumer;

    private ForkJoinPool forkJoinPool = new ForkJoinPool(100);

    public QuizSubmissionController(
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
    public ResponseEntity<JsonNode> post(@RequestBody final JsonNode data) {


        ((ObjectNode) data).put("request_id", generateRequestId());

//        QuizSubmission submission = jsonAvroConverter.convertToSpecificRecord(data, QuizSubmission.class, QuizSubmission.getClassSchema());
        QuizSubmission submission = toSpecificRecord(data, QuizSubmission.class, QuizSubmission.getClassSchema());


        if (!responseConsumer.willGetResponse(submission.getQuizId())) {
            log.info("DEFERRED");
            //TODO -- 406 ....
            ResponseEntity<JsonNode> responseEntity = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
//            deferredResult.setResult(responseEntity);
            return responseEntity;
        }


        Headers headers = new RecordHeaders();
        headers.add("request_id", submission.getRequestId().getBytes(StandardCharsets.UTF_8));

        Future<RecordMetadata> future = kafkaProducer.send(
                new ProducerRecord<>(
                        applicationProperties.getQuizSubmissionTopic(),
                        null,
                        submission.getQuizId(),
                        submission,
                        headers)
        );

        SpecificRecordBase response = correlationService.waitForResponse(submission.getRequestId());

        JsonNode result = toJsonNode(response);

        if (response instanceof QuizResult) {
            ((ObjectNode) result).put("type", "result");
        } else {
            ((ObjectNode) result).put("type", "question");
        }

        log.debug("request payload={}", debug(result));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    //@PostMapping
    public DeferredResult<ResponseEntity<JsonNode>> post2(@RequestBody final JsonNode data) {

        ((ObjectNode) data).put("request_id", generateRequestId());

        final QuizSubmission submission = toSpecificRecord(data, QuizSubmission.class, QuizSubmission.getClassSchema());

        try {
            log.info(OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(submission));
        } catch (final JsonProcessingException e) {
        }


        final DeferredResult<ResponseEntity<JsonNode>> deferredResult = correlationService2.waitForResponse(submission.getRequestId());

        log.info(">> quiz={}, willGet={}", submission.getQuizId(), responseConsumer.willGetResponse(submission.getQuizId()));

        /// redirect will respond with a 302 and the new URL in the Location header; the browser/client will then make another request to the new URL

        //307
        if (!responseConsumer.willGetResponse(submission.getQuizId())) {
            log.info("DEFERRED");
            //TODO -- 406 ....
            ResponseEntity<JsonNode> responseEntity = new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
            deferredResult.setResult(responseEntity);
            return deferredResult;
        }

        // ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkJoinPool.submit(() -> {

            Headers headers = new RecordHeaders();
            headers.add("request_id", submission.getRequestId().getBytes(StandardCharsets.UTF_8));

            Future<RecordMetadata> future = kafkaProducer.send(
                    new ProducerRecord<>(
                            applicationProperties.getQuizSubmissionTopic(),
                            null,
                            submission.getQuizId(),
                            submission,
                            headers)
            );
        });


        //NEIL
        log.info("LOCATION");
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.put("location", "http://localhost:9080/quizzes/answer?request-id=" + submission.getRequestId());
        ResponseEntity<JsonNode> responseEntity = new ResponseEntity<>(objectNode, HttpStatus.ACCEPTED);
        deferredResult.setResult(responseEntity);

        return deferredResult;
    }


    private static final ObjectNode OBJECT_NODE = JsonNodeFactory.instance.objectNode();

    private static final String KSQLDB_SERVER_HOST = "ksql-server";
    //private static final String KSQLDB_SERVER_HOST = "localhost";
    private static final int KSQLDB_SERVER_HOST_PORT = 8088;

    private final ClientOptions options = ClientOptions.create()
            .setHost(KSQLDB_SERVER_HOST)
            .setPort(KSQLDB_SERVER_HOST_PORT);

    private Client client = Client.create(options);

    @GetMapping
    public ResponseEntity<JsonNode> get(@RequestParam("request-id") final String requestId, @RequestParam(value = "count", defaultValue = "0") final int count) {

        log.info("get() request_id=" + requestId);

        Map<String, Object> map = getResult(requestId);
        if (map == null) {
            map = getNext(requestId);
        }
        if (map == null) {
            return redirect(requestId, count);
        }

        try {
            return new ResponseEntity<>(OBJECT_MAPPER.convertValue(map, JsonNode.class), null, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> getNext(final String requestId) {

        try {
            BatchedQueryResult result = client.executeQuery("SELECT * FROM KSQL_QUIZ_NEXT where REQUEST_ID='" + requestId + "';");//"' and " + (System.currentTimeMillis() - 60000) + ">= WINDOWSTART;");
            List<Row> list = result.get();

            if (list.size() != 1) {
                return null;
            } else {

                int last = list.size() - 1;

                Map<String, Object> map = new HashMap<>();
                map.put("quiz_id", list.get(last).getString("QUIZ_ID"));
                map.put("question_id", list.get(last).getString("QUESTION_ID"));
                map.put("statement", list.get(last).getString("STATEMENT"));
                map.put("a", list.get(last).getString("A"));
                map.put("b", list.get(last).getString("B"));
                map.put("c", list.get(last).getString("C"));
                map.put("d", list.get(last).getString("D"));
                map.put("difficulty", list.get(last).getInteger("DIFFICULTY"));
                //map.put("request_id", list.get(last).getString("REQUEST_ID"));
                map.put("type", "question");
                map.put("ksqldb", Boolean.TRUE);
                map.put("request_id", requestId);
//                KsqlObject object = list.get(0).getKsqlObject("RESPONSE");
//                Map<String, Object> map = object.getMap();
//                map.put("quiz_id", quizId);
//                map.put("type", "question");
//                map.put("difficulty", Integer.parseInt((String) map.get("difficulty")));
//                map.put("ksqldb", Boolean.TRUE);
                return map;
            }

        } catch (final ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, Object> getResult(final String requestId) {

        try {
            BatchedQueryResult result = client.executeQuery("SELECT * FROM KSQL_QUIZ_RESULT where REQUEST_ID='" + requestId + "';");//"' and " + (System.currentTimeMillis() - 60000) + ">= WINDOWSTART;");
            List<Row> list = result.get();

            if (list.size() != 1) {
                return null;
            } else {
//                KsqlObject object = list.get(0).getKsqlObject("RESPONSE");
//                Map<String, Object> map = object.getMap();
//                map.put("quiz_id", quizId);
//                map.put("type", "result");
//                map.put("correct", Integer.parseInt((String) map.get("correct")));
//                map.put("questions", Integer.parseInt((String) map.get("questions")));
//                map.put("ksqldb", Boolean.TRUE);

                int last = list.size() - 1;

                Map<String, Object> map = new HashMap<>();
                map.put("quiz_id", list.get(last).getString("QUIZ_ID"));
                map.put("type", "result");
                map.put("correct", list.get(last).getInteger("CORRECT"));
                map.put("questions", list.get(last).getInteger("QUESTIONS"));
                map.put("ksqldb", Boolean.TRUE);
                map.put("request_id", requestId);

                return map;
            }

        } catch (final ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ResponseEntity<JsonNode> redirect(final String requestId, final int count) {
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
        objectNode.put("location", "http://localhost:9080/quizzes/answer?request-id=" + requestId + "&count=" + (count + 1));
        return new ResponseEntity<>(objectNode, HttpStatus.ACCEPTED);
    }
}
