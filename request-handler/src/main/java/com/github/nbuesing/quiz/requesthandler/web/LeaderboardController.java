package com.github.nbuesing.quiz.requesthandler.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.nbuesing.quiz.model.QuizResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import tech.allegro.schema.json2avro.converter.JsonAvroConverter;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;

@RequestMapping(path = "/leaderboard")
@RestController
@Slf4j
public class LeaderboardController {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final KafkaReceiver<String, QuizResult> kafkaReceiver;
    private final JsonAvroConverter jsonAvroConverter;

    private final ConnectableFlux<ServerSentEvent<ObjectNode>> eventPublisher;

    private ParameterizedTypeReference<ServerSentEvent<String>> type = new ParameterizedTypeReference<ServerSentEvent<String>>() {};
    //        final ResolvableType type = ResolvableType.forClassWithGenerics(ServerSentEvent.class, String.class);


    public LeaderboardController(
            final KafkaReceiver<String, QuizResult> kafkaReceiver,
            final JsonAvroConverter jsonAvroConverter) {
        this.kafkaReceiver = kafkaReceiver;
        this.jsonAvroConverter = jsonAvroConverter;

        eventPublisher = kafkaReceiver.receive()
                .map(value -> {

                    log.info("MAP : value={}", value);

                    final ObjectNode objectNode = toObjectNode(value.value());
                    objectNode.put("date", toDate(toLocalDateTime(value.timestamp())));
                    objectNode.put("time", toTime(toLocalDateTime(value.timestamp())));
                    objectNode.put("timestamp", value.timestamp());
                    objectNode.put("partition", value.partition());
                    objectNode.put("offset", value.offset());

                    try {
                        log.info(OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode));
                    } catch (final JsonProcessingException e) {
                        log.error("unable to print", e);
                    }

                    return objectNode;
                })
                .map(value -> ServerSentEvent.builder(value).build())
                .publish();

        eventPublisher.connect();
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    //@Async("applicationTaskExecutor")
    public Flux getSimpleFlux() {
        return eventPublisher;
    }
//    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public Flux<ServerSentEvent<String>> streamEvents() {
//
//
//        WebClient client = WebClient.create();
//
//        return client.post()
//                .uri("http://localhost:8088/query")
//                .header("Content-Type", "application/vnd.ksql.v1+json")
//                .header("Accept", "application/vnd.ksql.v1+json")
//                .body(BodyInserters.fromValue("{\"ksql\": \"select r.quiz_id, u.name name, r.questions total, r.correct correct from results r join users u on r.user_id = u.user_id emit changes;\", \"streamsProperties\": {\"ksql.streams.auto.offset.reset\": \"earliest\"}"))
//                .exchange()
//                .flatMapMany(response -> response.body(BodyExtractors.toFlux(type)));
//
////                        .flatMapMany(response -> ServerSentEvent.<String> builder()
////                .id(String.valueOf(1))
////                .event("periodic-event")
////                .data("")
////                .build()
////        );
//
//
////        return Flux.interval(Duration.ofSeconds(1))
////                .map(sequence -> ServerSentEvent.<String> builder()
////                        .id(String.valueOf(sequence))
////                        .event("periodic-event")
////                        .data("SSE - " + LocalTime.now().toString())
////                        .build());
//    }

    private LocalDateTime toLocalDateTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.of("America/Chicago"))
                .toLocalDateTime();
    }

    private String toDate(final LocalDateTime time) {
        return DateTimeFormatter.ofPattern("MM/dd/yy").format(time);
    }

    private String toTime(final LocalDateTime time) {
        return DateTimeFormatter.ofPattern("hh:mm:ss a").format(time);
    }

    private ObjectNode toObjectNode(SpecificRecordBase specificRecord) {
        try {
            return (ObjectNode) OBJECT_MAPPER.readValue(jsonAvroConverter.convertToJson(specificRecord), JsonNode.class);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
