package com.github.nbuesing.quiz.quizbuilder.configuration;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.Data;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.*;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "kafka")
@Data
@Validated
public class KafkaProperties {

  public static final String DESERIALIZATION_EXCEPTION_THRESHOLD = "throttling.deserialization.exception.handler.threshold";

  public static final class AvroSerde extends Serdes.WrapperSerde<Object> {
    public AvroSerde() {
      super(new KafkaAvroSerializer(), new KafkaAvroDeserializer());
    }
  }

  @Data
  @Validated
  public static class Producer {

    @NotBlank private String clientId;
    @NotNull @Pattern(regexp = "0|1|all") private String acks = "all";
    @Min(0) private Integer linger = 50;
    @Min(0) private Integer batchSize = 65536;
    @Pattern(regexp = "none|gzip|snappy|lz4|zstd") private String compressionType = "zstd";
    private Boolean enableIdempotence;
    @Min(0) private Integer bufferMemory;
    @Min(0) private Long maxBlock;
    @Min(0) private Integer maxRequestSize;
    @Min(-1) private Integer receiveBuffer;
    @Min(0) private Integer requestTimeout;
    @Min(-1) private Integer sendBuffer;
    @Min(1) private Integer maxInFlightRequestsPerConnection;
    @Min(0) private Integer retries;

    @NotBlank private String keySerializer = StringSerializer.class.getName();
    @NotBlank private String valueSerializer = KafkaAvroSerializer.class.getName();

    private final Map<String, String> properties = new HashMap<>();

    public Map<String, Object> addProperties(final Map<String, Object> props) {
      final PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

      props.putAll(properties);

      propertyMapper.from(this::getClientId).to(value -> props.put(ProducerConfig.CLIENT_ID_CONFIG, value));
      propertyMapper.from(this::getAcks).to(value -> props.put(ProducerConfig.ACKS_CONFIG, value));
      propertyMapper.from(this::getKeySerializer).to(value -> props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, value));
      propertyMapper.from(this::getValueSerializer).to(value -> props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, value));
      propertyMapper.from(this::getLinger).to(value -> props.put(ProducerConfig.LINGER_MS_CONFIG, value));
      propertyMapper.from(this::getBatchSize).to(value -> props.put(ProducerConfig.BATCH_SIZE_CONFIG, value));
      propertyMapper.from(this::getBufferMemory).to(value -> props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, value));
      propertyMapper.from(this::getCompressionType).to(value -> props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, value));
      propertyMapper.from(this::getEnableIdempotence).to(value -> props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, booleanToString(value)));
      propertyMapper.from(this::getMaxBlock).to(value -> props.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, value));
      propertyMapper.from(this::getMaxRequestSize).to(value -> props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, value));
      propertyMapper.from(this::getReceiveBuffer).to(value -> props.put(ProducerConfig.RECEIVE_BUFFER_CONFIG, value));
      propertyMapper.from(this::getRequestTimeout).to(value -> props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, value));
      propertyMapper.from(this::getSendBuffer).to(value -> props.put(ProducerConfig.SEND_BUFFER_CONFIG, value));
      propertyMapper.from(this::getMaxInFlightRequestsPerConnection).to(value -> props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, value));
      propertyMapper.from(this::getRetries).to(value -> props.put(ProducerConfig.RETRIES_CONFIG, value));

      return props;
    }
  }

  @Data
  @Validated
  public static class Consumer {

    private String clientId;
    @NotBlank private String groupId;
    @NotNull @Pattern(regexp = "none|earliest|latest") String autoOffsetReset = "earliest";
    @NotBlank private String keyDeserializer = StringDeserializer.class.getName();
    @NotBlank private String valueDeserializer = KafkaAvroDeserializer.class.getName();
    @NotNull private Boolean specificAvroReader = true;
    @NotNull private Boolean enableAutoCommit = false;

    private final Map<String, String> properties = new HashMap<>();

    public Map<String, Object> addProperties(final Map<String, Object> props) {
      final PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

      props.putAll(properties);

      propertyMapper.from(this::getClientId).to(value -> props.put(ConsumerConfig.CLIENT_ID_CONFIG, value));
      propertyMapper.from(this::getGroupId).to(value -> props.put(ConsumerConfig.GROUP_ID_CONFIG, value));
      propertyMapper.from(this::getEnableAutoCommit).to(value -> props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, booleanToString(value)));
      propertyMapper.from(this::getAutoOffsetReset).to(value -> props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, value));
      propertyMapper.from(this::getKeyDeserializer).to(value -> props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, value));
      propertyMapper.from(this::getValueDeserializer).to(value -> props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, value));
      propertyMapper.from(this::getSpecificAvroReader).to(value -> props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, booleanToString(value)));

      return props;
    }
  }

  @Data
  @Validated
  public static class Streams {

    @NotBlank private String applicationId;
    @NotBlank private String keySerde = Serdes.StringSerde.class.getName();
    @NotBlank private String valueSerde = AvroSerde.class.getName();
    @NotNull private Boolean specificAvroReader = true;
    @Min(1) private Integer replicationFactor;
    private Integer commitInterval;
    @Min(1) private Integer numStreamThreads;
    @Pattern(regexp = "at_least_once|exactly_once") private String processingGuarantee;
    private String defaultDeserializationExceptionHandler = LogAndContinueExceptionHandler.class.getName();
    private Double deserializationExceptionThreshold;

    private final Map<String, Object> properties = new HashMap<>();

    public Map<String, Object> addProperties(final Map<String, Object> props) {
      final PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

      props.putAll(properties);

      System.out.println(">>>");
      System.out.println(props);

      propertyMapper.from(this::getApplicationId).to(value -> props.put(StreamsConfig.APPLICATION_ID_CONFIG, value));
      propertyMapper.from(this::getKeySerde).to(value -> props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, value));
      propertyMapper.from(this::getValueSerde).to(value -> props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, value));
      propertyMapper.from(this::getSpecificAvroReader).to(value -> props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, booleanToString(value)));
      propertyMapper.from(this::getReplicationFactor).to(value -> props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, value));
      propertyMapper.from(this::getCommitInterval).to(value -> props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, value));
      propertyMapper.from(this::getNumStreamThreads).to(value -> props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, value));
      propertyMapper.from(this::getProcessingGuarantee).to(value -> props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, value));
      propertyMapper.from(this::getDefaultDeserializationExceptionHandler).to(value -> props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, value));
      propertyMapper.from(this::getDeserializationExceptionThreshold).to(value -> props.put(DESERIALIZATION_EXCEPTION_THRESHOLD, Double.toString(value)));

      return props;
    }
  }


  @NotEmpty private List<String> bootstrapServers;
  @NotEmpty @URL private String schemaRegistry;
  @Valid private Producer producer;
  @Valid private Consumer consumer;
  @Valid private Streams streams;

  private Map<String, Object> createProperties() {
    PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

    Map<String, Object> properties = new HashMap<>();

    propertyMapper
      .from(this.getBootstrapServers())
      .to(servers -> properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers.stream().collect(Collectors.joining(","))));

    propertyMapper.from(this::getSchemaRegistry).to(value -> properties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, value));

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

  public Map<String, Object> toStreamsProperties() {

    final Map<String, Object> properties = createProperties();

    Optional.ofNullable(producer).ifPresent(p -> p.addProperties(properties));
    //serdes from streams configuration is used for serialization
    properties.remove(ProducerConfig.CLIENT_ID_CONFIG);
    properties.remove(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG);
    properties.remove(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);

    Optional.ofNullable(consumer).ifPresent(p -> p.addProperties(properties));
    // for a streams application clientId and groupId are derived from applicationId.
    properties.remove(ConsumerConfig.CLIENT_ID_CONFIG);
    properties.remove(ConsumerConfig.GROUP_ID_CONFIG);
    //serdes from streams configuration is used for deserialization
    properties.remove(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG);
    properties.remove(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG);

    Optional.ofNullable(streams).ifPresent(p -> p.addProperties(properties));
    return properties;
  }

  private static String booleanToString(boolean value) {
    return value ? "true" : "false";
  }


  public static Properties toProperties(final Map<String, Object> map) {
    final Properties properties = new Properties();
    properties.putAll(map);
    return properties;
  }

}
