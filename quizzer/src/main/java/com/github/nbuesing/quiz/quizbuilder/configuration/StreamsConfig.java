/*
 * Copyright (c) 2020.
 */

package com.github.nbuesing.quiz.quizbuilder.configuration;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.StreamsBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Map;

@Configuration
@Slf4j
public class StreamsConfig {

  @Bean
  public StreamsBuilder streamsBuilder() {
    return new StreamsBuilder();
  }

  @Bean
  public GenericAvroSerde genericAvroKeySerde(KafkaProperties kafkaProperties) {
    Map<String, String> schemaRegistryConfig = Collections.singletonMap(
            AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaProperties.getSchemaRegistry()
    );
    GenericAvroSerde genericAvroKeySerde = new GenericAvroSerde();
    genericAvroKeySerde.configure(schemaRegistryConfig, true);
    return genericAvroKeySerde;
  }

  @Bean
  public GenericAvroSerde genericAvroValueSerde(KafkaProperties kafkaProperties) {
    Map<String, String> schemaRegistryConfig = Collections.singletonMap(
            AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, kafkaProperties.getSchemaRegistry()
    );
    GenericAvroSerde genericAvroValueSerde = new GenericAvroSerde();
    genericAvroValueSerde.configure(schemaRegistryConfig, false);
    return genericAvroValueSerde;
  }

}
