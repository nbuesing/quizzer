/*
 * Copyright (c) 2020.
 */

package com.github.nbuesing.quiz.quizbuilder.streams;

import com.github.nbuesing.quiz.model.*;
import com.github.nbuesing.quiz.quizbuilder.properties.ApplicationProperties;
import com.github.nbuesing.quiz.quizbuilder.util.AvroUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.*;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class QuizBuilderStream {

    private final StreamsBuilder streamsBuilder;
    private final ApplicationProperties applicationProperties;

    public QuizBuilderStream(
            final StreamsBuilder streamsBuilder,
            final ApplicationProperties applicationProperties) {
        this.streamsBuilder = streamsBuilder;
        this.applicationProperties = applicationProperties;
    }

    @PostConstruct
    public void construct() {
        stream();
    }

    void stream() {

        KTable<String, Question> table = streamsBuilder.<String, Question>stream(applicationProperties.getQuestions())
                .groupByKey()
                .reduce(
                        (oldValue, newValue) -> {
                            if (oldValue != null) {
                                newValue.setPreviousDifficulty(oldValue.getDifficulty());
                            }
                            return newValue;
                        });

        table
                .toStream()
//                .filter((k, v) ->
//                        v.getPreviousDifficulty() == null || v.getPreviousDifficulty().intValue() != v.getDifficulty())
                .flatMap(
                        (KeyValueMapper<String, Question, Iterable<KeyValue<Integer, QuestionDifficulty>>>) (key, value) -> {
                            List<KeyValue<Integer, QuestionDifficulty>> list = new ArrayList<>();
                            list.add(new KeyValue<>(value.getDifficulty(), toAdd(value)));
                            if (value.getPreviousDifficulty() != null && value.getPreviousDifficulty() != value.getDifficulty()) {
                                list.add(new KeyValue<>(value.getPreviousDifficulty(), toRemove(value)));
                            }
                            log.info("Flatmap " + list);
                            return list;
                        }
                )
                .groupByKey(Grouped.with(Serdes.Integer(), null))
                .<Questions>aggregate(
                        () -> null,
                        (k, v, a) -> {

                            if (a == null) {
                                a = Questions.newBuilder()
                                        .build();
                            }

                            if (v.getAction() == Action.ADD) {
                                List<String> ids = a.getQuestionIds();
                                if (!ids.contains(v.getQuestionId())) {
                                    ids.add(v.getQuestionId());
                                }
                            } else {
                                // Avro's List implementation "has issues" in that remove() is not
                                // implemented, so we must index over it and remove it, if such element
                                // is found.
                                AvroUtil.remove(a.getQuestionIds(), v.getQuestionId());
                            }

                            return a;
                        },
                        Materialized.with(Serdes.Integer(), null))
                .toStream()
                .peek((k, v) -> log.info("difficultly table updated k={}, value={}", k, v))
                .to(applicationProperties.getQuestionsDifficulty(), Produced.with(Serdes.Integer(), null));
    }

    private QuestionDifficulty toAdd(final Question question) {
        return QuestionDifficulty.newBuilder()
                .setAction(Action.ADD)
                .setDifficulty(question.getDifficulty())
                    .setQuestionId(question.getQuestionId())
                .build();
    }

    private QuestionDifficulty toRemove(final Question question) {
        return QuestionDifficulty.newBuilder()
                .setAction(Action.REMOVE)
                .setDifficulty(question.getPreviousDifficulty())
                .setQuestionId(question.getQuestionId())
                .build();
    }

}
