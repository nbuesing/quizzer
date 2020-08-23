/*
 * Copyright (c) 2020.
 */

package com.github.nbuesing.quiz.quizbuilder;

import com.github.nbuesing.quiz.quizbuilder.configuration.KafkaProperties;
import com.github.nbuesing.quiz.quizbuilder.properties.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class StartApplication implements ApplicationListener<ApplicationStartedEvent> {

    private final ApplicationContext applicationContext;
    private final KafkaProperties kafkaProperties;
    private ApplicationProperties applicationProperties;
    private final StreamsBuilder streamsBuilder;

    private KafkaStreams streams;

    public StartApplication(
            final ApplicationContext applicationContext,
            final KafkaProperties kafkaProperties,
            final ApplicationProperties applicationProperties,
            final StreamsBuilder streamsBuilder) {
        this.applicationContext = applicationContext;
        this.kafkaProperties = kafkaProperties;
        this.applicationProperties = applicationProperties;
        this.streamsBuilder = streamsBuilder;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        start();
    }

    public void start() {
        Map<String, Object> configs = kafkaProperties.toStreamsProperties();

        final Topology topology = streamsBuilder.build();


        log.info(topology.describe().toString());

        streams = new KafkaStreams(
                topology,
                KafkaProperties.toProperties(configs));

        streams.setUncaughtExceptionHandler((t, e) -> {
            if (streams.state().isRunning()) {
                streams.close();
            }
            SpringApplication.exit(applicationContext, (ExitCodeGenerator) () -> 0);
        });

        if (applicationProperties.getCleanupOnStart()) {
            // This should never be enabled in prod
            streams.cleanUp();
        }

        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            streams.close();
        }));
    }

    public KafkaStreams kafkaStreams() {
        return streams;
    }
}
