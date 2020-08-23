package com.github.nbuesing.quiz.requesthandler.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.nbuesing.quiz.QuizQuestion;
import com.github.nbuesing.quiz.model.Question;
import com.github.nbuesing.quiz.model.Quiz;
import com.github.nbuesing.quiz.model.QuizResult;
import com.github.nbuesing.quiz.requesthandler.configuration.kafka.KafkaProperties;
import com.github.nbuesing.quiz.requesthandler.properties.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.internals.DefaultPartitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.allegro.schema.json2avro.converter.JsonAvroConverter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ResponseConsumer<K, V> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ApplicationProperties applicationProperties;
    private final Consumer<K, V> consumer;
    private final CorrelationService correlationService;
    private final CorrelationService2 correlationService2;
    private final JsonAvroConverter jsonAvroConverter;

    private final Partitioner partitioner;

    private AtomicBoolean running = new AtomicBoolean(false);
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private long lastOffsetCommitMs = System.currentTimeMillis();

    public ResponseConsumer(
            final ApplicationProperties applicationProperties,
            final KafkaProperties properties,
            final CorrelationService correlationService,
            final CorrelationService2 correlationService2,
            final JsonAvroConverter jsonAvroConverter
    ) {
        consumer = new KafkaConsumer<K, V>(properties.toConsumerProperties());
        this.applicationProperties = applicationProperties;
        this.correlationService = correlationService;
        this.correlationService2 = correlationService2;
        this.jsonAvroConverter = jsonAvroConverter;

        ProducerConfig config = new ProducerConfig(properties.toProducerProperties());
        this.partitioner = config.getConfiguredInstance(ProducerConfig.PARTITIONER_CLASS_CONFIG, Partitioner.class);

    }

    private int numberOfPartitions = 10;
    private Set<Integer> myPartitions;

    private ConsumerRebalanceListener rebalanceListener = new ConsumerRebalanceListener() {
        @Override
        public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            consumer.commitSync();
            myPartitions = Collections.emptySet();
            log.info("REMOVING " + partitions);
        }

        @Override
        public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
            //TODO either/or topic...
//            numberOfPartitions = partitions.stream().map(partition -> partition.partition()).max(Integer::compare).get();
            myPartitions = partitions.stream().map(partition -> partition.partition()).collect(Collectors.toSet());
            log.info("My Partitions : " + myPartitions);
        }
    };

    public boolean willGetResponse(final String id) {
        int partition = Utils.toPositive(Utils.murmur2(id.getBytes())) % numberOfPartitions;
        return myPartitions.contains(partition);
    }

    public void poll() {

        running.set(true);

        try {

            log.info("subscribing to topic {}", applicationProperties.getQuizNextTopic());
            consumer.subscribe(
                    Arrays.asList(applicationProperties.getQuizNextTopic(), applicationProperties.getQuizResultTopic()),
                    rebalanceListener);

            while (running.get()) {

                try {
                    @SuppressWarnings("unchecked")
                    ConsumerRecords<String, SpecificRecord> records = (ConsumerRecords<String, SpecificRecord>) consumer.poll(applicationProperties.getPollInterval());
                    for (ConsumerRecord<String, SpecificRecord> record : records) {

                        log.info("Response for key={}, value={}, headers={}", record.key(), record.value(), record.headers().lastHeader("request_id"));

                        if (record.headers().lastHeader("request_id") == null) {
                            log.info("no requiest_id header, ignoring.");
                            continue;
                        }

                        String requestId = new String(record.headers().lastHeader("request_id").value(), StandardCharsets.UTF_8);

                        log.info("requestId : " + requestId);

//                        if (!correlationService2.hasRequestId(requestId)) {
//                            log.info("requestId={} is not on this web-service.", requestId);
//                        }

                        try {
                            log.info("key={}, value={}", record.key(), record.value());
                            if (record.value() instanceof QuizQuestion) {
                                QuizQuestion quizQuestion = (QuizQuestion) record.value();
                                correlationService.addResponse(quizQuestion.getRequestId(), quizQuestion);
                                JsonNode node = toJsonNode(quizQuestion);
                                ((ObjectNode) node).put("type", "question");
                                correlationService2.addResponse(quizQuestion.getRequestId(), node);
                            } else if (record.value() instanceof QuizResult) {
                                QuizResult quizResult = (QuizResult) record.value();
                                correlationService.addResponse(quizResult.getRequestId(), quizResult);
                                JsonNode node = toJsonNode(quizResult);
                                ((ObjectNode) node).put("type", "result");
                                correlationService2.addResponse(quizResult.getRequestId(), node);
                            }
                        } catch (final Exception e) {
                            log.error("error processing event with key {}", record.key(), e);
                        }
                    }
                    maybeCommitOffsets();
                } catch (final WakeupException e) {
                    log.info("wakeup exception, ignoring as consumer polling should be able to resume gracefully", e);
                } catch (final KafkaException e) {
                    if (!running.get()) {
                        throw e;
                    } else {
                        log.info("ignoring exceptions during shutdown", e);
                    }
                }
            }

            consumer.commitSync();

        } finally {
            countDownLatch.countDown();
        }
    }

    public void close() {
        running.set(false);

        try {
            countDownLatch.await(10L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            //ignore
        }

        try {
            consumer.close();
        } catch (RuntimeException e) {
            log.warn("unable to close consumer", e);
        }
    }

    private void maybeCommitOffsets() {
        if (System.currentTimeMillis() - lastOffsetCommitMs > applicationProperties.getOffsetCommitInterval().toMillis()) {
            log.trace("committing offsets.");
            consumer.commitSync();
            lastOffsetCommitMs = System.currentTimeMillis();
        }
    }

    private Map<String, byte[]> convertHeaders(Headers headers) {
        Map<String, byte[]> result = new HashMap<>();
        headers.forEach(h -> {
            result.put(h.key(), h.value());
        });
        return result;
    }


    protected JsonNode toJsonNode(SpecificRecordBase specificRecord) {
        try {
            return OBJECT_MAPPER.readValue(jsonAvroConverter.convertToJson(specificRecord), JsonNode.class);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
