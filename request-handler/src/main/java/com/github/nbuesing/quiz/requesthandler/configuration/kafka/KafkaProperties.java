
package com.github.nbuesing.quiz.requesthandler.configuration.kafka;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.Data;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@ConfigurationProperties(prefix = "kafka")
@Data
@Validated
public class KafkaProperties {

    @Data
    @Validated
    public static class Producer {

        @NotBlank
        private String clientId;
        @NotNull
        @Pattern(regexp = "0|1|all")
        private String acks = "all";
        @Min(0)
        private Integer linger = 50;
        @Min(0)
        private Integer batchSize = 65536;
        @Pattern(regexp = "none|gzip|snappy|lz4|zstd")
        private String compressionType = "zstd";
        private Boolean enableIdempotence;
        @Min(0)
        private Integer bufferMemory;
        @Min(0)
        private Long maxBlock;
        @Min(0)
        private Integer maxRequestSize;
        @Min(-1)
        private Integer receiveBuffer;
        @Min(0)
        private Integer requestTimeout;
        @Min(-1)
        private Integer sendBuffer;
        @Min(1)
        private Integer maxInFlightRequestsPerConnection;
        @Min(0)
        private Integer retries;

        @NotBlank
        private String keySerializer = StringSerializer.class.getName();
        @NotBlank
        private String valueSerializer = KafkaAvroSerializer.class.getName();

        //TODO -- other properties we need to consider adding
        //metadata.max.age.ms

        public Map<String, Object> addProperties(final Map<String, Object> properties) {
            final PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

            propertyMapper.from(this::getClientId).to(value -> properties.put(ProducerConfig.CLIENT_ID_CONFIG, value));
            propertyMapper.from(this::getAcks).to(value -> properties.put(ProducerConfig.ACKS_CONFIG, value));
            propertyMapper.from(this::getKeySerializer).to(value -> properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, value));
            propertyMapper.from(this::getValueSerializer).to(value -> properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, value));
            propertyMapper.from(this::getLinger).to(value -> properties.put(ProducerConfig.LINGER_MS_CONFIG, value));
            propertyMapper.from(this::getBatchSize).to(value -> properties.put(ProducerConfig.BATCH_SIZE_CONFIG, value));
            propertyMapper.from(this::getBufferMemory).to(value -> properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, value));
            propertyMapper.from(this::getCompressionType).to(value -> properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, value));
            propertyMapper.from(this::getEnableIdempotence).to(value -> properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, booleanToString(value)));
            propertyMapper.from(this::getMaxBlock).to(value -> properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, value));
            propertyMapper.from(this::getMaxRequestSize).to(value -> properties.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, value));
            propertyMapper.from(this::getReceiveBuffer).to(value -> properties.put(ProducerConfig.RECEIVE_BUFFER_CONFIG, value));
            propertyMapper.from(this::getRequestTimeout).to(value -> properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, value));
            propertyMapper.from(this::getSendBuffer).to(value -> properties.put(ProducerConfig.SEND_BUFFER_CONFIG, value));
            propertyMapper.from(this::getMaxInFlightRequestsPerConnection).to(value -> properties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, value));
            propertyMapper.from(this::getRetries).to(value -> properties.put(ProducerConfig.RETRIES_CONFIG, value));

            //properties.putAll(properties);

            return properties;
        }
    }

    @Data
    @Validated
    public static class Consumer {

        private String clientId;
        @NotBlank
        private String groupId;
        @NotNull @Pattern(regexp = "none|earliest|latest") String autoOffsetReset = "earliest";
        @NotBlank
        private String keyDeserializer = StringDeserializer.class.getName();
        @NotBlank
        private String valueDeserializer = KafkaAvroDeserializer.class.getName();
        @NotNull
        private Boolean specificAvroReader = true;
        @NotNull
        private Boolean enableAutoCommit = false;

        public Map<String, Object> addProperties(final Map<String, Object> properties) {
            final PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

            propertyMapper.from(this::getClientId).to(value -> properties.put(ConsumerConfig.CLIENT_ID_CONFIG, value));
            propertyMapper.from(this::getGroupId).to(value -> properties.put(ConsumerConfig.GROUP_ID_CONFIG, value));
            propertyMapper.from(this::getEnableAutoCommit).to(value -> properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, booleanToString(value)));
            propertyMapper.from(this::getAutoOffsetReset).to(value -> properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, value));
            propertyMapper.from(this::getKeyDeserializer).to(value -> properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, value));
            propertyMapper.from(this::getValueDeserializer).to(value -> properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, value));
            propertyMapper.from(this::getSpecificAvroReader).to(value -> properties.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, booleanToString(value)));

            return properties;
        }
    }

    @NotEmpty private List<String> bootstrapServers;
    @NotEmpty @URL private String schemaRegistry;
  //  @NotEmpty private Boolean specificAvroReader = false;
    @Valid private Producer producer;
    @Valid private Consumer consumer;


    private Map<String, Object> createProperties() {
        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

        Map<String, Object> properties = new HashMap<>();

        propertyMapper.from(this.getBootstrapServers()).to(servers -> properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers.stream().collect(Collectors.joining(","))));

        propertyMapper.from(this::getSchemaRegistry).to(value -> properties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, value));
       // propertyMapper.from(this::getSpecificAvroReader).to(value -> properties.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, booleanToString(value)));

        return properties;
    }

    public Map<String, Object> toProducerProperties() {
        final Map<String, Object> properties = createProperties();
        Optional.ofNullable(producer).ifPresent(p -> p.addProperties(properties));
        return properties;
    }

    public Map<String, Object> toConsumerProperties() {
        final Map<String, Object> properties = createProperties();
        Optional.ofNullable(consumer).ifPresent(p -> p.addProperties(properties));
        return properties;
    }

    private static String booleanToString(boolean value) {
        return value ? "true" : "false";
    }

}
