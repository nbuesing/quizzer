package com.github.nbuesing.quiz.requesthandler.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nbuesing.quiz.model.QuizResult;
import com.github.nbuesing.quiz.requesthandler.configuration.kafka.KafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.internals.ConsumerFactory;
import reactor.kafka.receiver.internals.DefaultKafkaReceiver;
import tech.allegro.schema.json2avro.converter.AvroConversionException;
import tech.allegro.schema.json2avro.converter.JsonAvroConverter;
import tech.allegro.schema.json2avro.converter.UnknownFieldListener;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

@Configuration
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    public KafkaConfig(
            final KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public KafkaProducer<String, Object> kafkaProducer() {
        return new KafkaProducer<>(kafkaProperties.toProducerProperties());
    }

    @Bean
    public JsonAvroConverter jsonAvroConverter() {

        final ObjectMapper objectMapper = new ObjectMapper();
        final UnknownFieldListener unknownFieldListener = (name, value, path) -> {
            throw new AvroConversionException("unknown field name=" + name);
        };

        return new JsonAvroConverter(objectMapper, unknownFieldListener);
    }

    @Bean
    public KafkaReceiver<String, QuizResult> kafkaReceiver(){

        Map<String, Object> properties = kafkaProperties.toConsumerProperties();

        properties.put(ConsumerConfig.GROUP_ID_CONFIG, properties.get(ConsumerConfig.GROUP_ID_CONFIG) + "-leaderboard");

        return new DefaultKafkaReceiver<>(
                ConsumerFactory.INSTANCE,
                ReceiverOptions
                        .<String, QuizResult>create(properties)
                        .subscription(Collections.singleton("quiz_status"))
        );
    }
}
