#!/bin/bash

TOPIC=$1

kafka-avro-console-consumer \
	--bootstrap-server localhost:19092 \
	--from-beginning \
	--property print.key=true \
	--property key.separator=" | " \
	--key-deserializer=org.apache.kafka.common.serialization.StringDeserializer \
	--topic ${TOPIC}

