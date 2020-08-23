/*
 * Copyright (c) 2020.
 */

package com.github.nbuesing.quiz.quizbuilder;

import com.github.nbuesing.quiz.quizbuilder.configuration.KafkaProperties;
import com.github.nbuesing.quiz.quizbuilder.configuration.StreamsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling
@Import({KafkaProperties.class, StreamsConfig.class})
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

