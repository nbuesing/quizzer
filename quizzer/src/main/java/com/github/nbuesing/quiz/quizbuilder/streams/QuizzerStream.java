/*
 * Copyright (c) 2020.
 */

package com.github.nbuesing.quiz.quizbuilder.streams;

import com.github.nbuesing.quiz.QuizQuestion;
import com.github.nbuesing.quiz.QuizStart;
import com.github.nbuesing.quiz.QuizSubmission;
import com.github.nbuesing.quiz.model.*;
import com.github.nbuesing.quiz.quizbuilder.properties.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.RecordContext;
import org.apache.kafka.streams.processor.TopicNameExtractor;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

@Component
@Slf4j
public class QuizzerStream {

    private static final Random RANDOM = new Random();

    private static final String SUBMIT_TOKEN = "-1";

    private static final int MIN_DIFFICULTY = 0;
    private static final int MAX_DIFFICULTY = 2;

    private final StreamsBuilder streamsBuilder;
    private final ApplicationProperties applicationProperties;

    public QuizzerStream(
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

        Materialized<String, Quiz, KeyValueStore<Bytes, byte[]>> quizStore = Materialized.as("QUIZ_STORE");

        /// TODO : grace period 0

        Materialized<Integer, Questions, KeyValueStore<Bytes, byte[]>> questionsStore =
                Materialized.<Integer, Questions, KeyValueStore<Bytes, byte[]>>as("QUESTIONS").withKeySerde(Serdes.Integer());

        GlobalKTable<Integer, Questions> diff =
                streamsBuilder.globalTable(applicationProperties.getQuestionsDifficulty(), questionsStore);

        KTable<String, User> users = streamsBuilder.table(applicationProperties.getUsers());

        KTable<String, Question> question = streamsBuilder.table(applicationProperties.getQuestions());

        KStream<String, QuizStart> quizStart = streamsBuilder.stream(applicationProperties.getQuizStart());
        // for 'quizStart' create a 'quizSubmission' as the seed that starts the quiz.
        quizStart
                .peek((k, v) -> log.info("quiz start k={}, v={}", k, v))
                .mapValues(QuizzerStream::starter)
                .to(applicationProperties.getQuizSubmission());

//        quizStart
//                .peek((k, v) -> log.info("quiz start k={}, v={}", k, v))
//                .mapValues(value -> QuizSubmission.newBuilder()
//                        .setQuizId(value.getQuizId())
//                        .setUserId(value.getUserId())
//                        .setQuestionId(SUBMIT_TOKEN)
//                        .setSubmittedAnswer("")
//                        .setQuestionCount(value.getCount())
//                        .setDifficulty(-1)
//                        .build())
//                .to(applicationProperties.getQuizSubmission());

//        KTable<String, Quiz> quiz = quizStart
//                .groupByKey()
//                .aggregate(
//                        () -> null,
//                        (key, value, aggregate) -> Quiz.newBuilder()
//                                .setQuizId(value.getQuizId())
//                                .setUserId(value.getUserId())
//                                .setMax(value.getCount())
//                                .build()
//                );

        KTable<String, Quiz> quiz = streamsBuilder.<String, QuizSubmission>stream(applicationProperties.getQuizSubmission())
                .peek((k, v) -> log.info("quiz submission k={}, v={}", k, v))
                .selectKey((k, v) -> v.getQuestionId())
                .peek((k, v) -> log.info("quiz submission post rekey k={}, v={}", k, v))
                .join(question,
                        (quizSubmission, question1) -> {
                            quizSubmission.setCorrectAnswer(question1.getCorrectAnswer());
                            quizSubmission.setDifficulty(question1.getDifficulty());
                            setDifficulty(quizSubmission);
                            return quizSubmission;
                        })
                .peek((k, v) -> log.info("quiz submission post join k={}, v={}", k, v))
//                .leftJoin(diff,
//                        (key, value) -> {
//                            log.info(">> " + value.getDifficulty());
//                            return value.getDifficulty();
//                        },
//                        (value1, value2) -> {
//                            log.info("" + value2);
//                            return value1;
//                        }
//                )
                .selectKey((k, v) -> v.getQuizId())
                .groupByKey()
                //.windowedBy(TimeWindows.of(Duration.ofSeconds(10L)).grace(Duration.ofSeconds(0)))
                .aggregate(
                        () -> null,
                        (key, value, aggregate) -> {

                            log.info("Quiz Aggregate: key={}, value={}", key, value);

                            if (aggregate == null) {
                                if (!SUBMIT_TOKEN.equals(value.getQuestionId())) {
                                    log.warn("attempting to answer a question before quiz is created.");
                                    return null;
                                }

                                aggregate = Quiz.newBuilder()
                                        .setQuizId(value.getQuizId())
                                        .setUserId(value.getUserId())
                                        .setMax(value.getQuestionCount())
                                        .setNextDifficulty(0)
                                        .setNextQuestionId(null)
                                        .build();

                                return aggregate;
                            }

                            QuizAnswer answer = QuizAnswer.newBuilder()
                                    .setCorrect(value.getCorrectAnswer())
                                    .setSubmitted(value.getSubmittedAnswer())
                                    .build();

                            aggregate.getAnswered().put(value.getQuestionId(), answer);
                            aggregate.setNextDifficulty(value.getDifficulty());
                            aggregate.setNextQuestionId(null);

                            return aggregate;
                        },
                        quizStore
                )
                .transformValues(new ValueTransformerWithKeySupplier<String, Quiz, Quiz>() {
                    @Override
                    public ValueTransformerWithKey<String, Quiz, Quiz> get() {
                        return new ValueTransformerWithKey<String, Quiz, Quiz>() {

                            KeyValueStore<String, ValueAndTimestamp<Quiz>> quizStore;
                            KeyValueStore<Integer, ValueAndTimestamp<Questions>> questionsStore;

                            ProcessorContext context;

                            @SuppressWarnings("unchecked")
                            @Override
                            public void init(ProcessorContext context) {
                                this.context = context;
                                quizStore = (KeyValueStore<String, ValueAndTimestamp<Quiz>>) context.getStateStore("QUIZ_STORE");
                                questionsStore = (KeyValueStore<Integer, ValueAndTimestamp<Questions>>) context.getStateStore("QUESTIONS");
                            }

                            @Override
                            public Quiz transform(String s, Quiz quiz) {
                                log.info("1)transform: s={}, quiz={}, timestamp={}", s, quiz, context.timestamp());
                                nextQuestion(quiz, questionsStore);
                                quizStore.put(s, ValueAndTimestamp.make(quiz, context.timestamp()));
                                log.info("2)transform: s={}, quiz={}", s, quiz);
                                return quiz;
                            }

                            @Override
                            public void close() {
                            }
                        };
                    }
                }, "QUIZ_STORE");

        KStream<String, Quiz> quizProcessing = quiz.toStream();

//        quizProcessing
//                .filter((k, v) -> v.getAnswered().size() >= v.getMax())
//                .mapValues((k, value1) -> createDoneMarker(value1))
//                .to(applicationProperties.getQuizNext());

        quizProcessing
                .peek((k, v) -> log.info("1a) Quiz Processing key={}, value={}", k, v))
                .filter((k, v) -> v.getAnswered().size() >= v.getMax())
                .peek((k, v) -> log.info("1b) Quiz Processing key={}, value={}", k, v))
                .mapValues((k, value1) -> createQuizResult(value1))
                .peek((k, v) -> log.info("1c) Quiz Processing key={}, value={}", k, v))
                .selectKey((k, v) -> v.getUserId())
                .peek((k, v) -> log.info("1d) Quiz Processing key={}, value={}", k, v))
                .leftJoin(users, (quizResult, user) -> {
                    if (user != null) {
                        log.info("User: " + user.getName());
                        quizResult.setUserName(user.getName());
                    }
                    return quizResult;
                })
                .peek((k, v) -> log.info("1e) Quiz Processing key={}, value={}", k, v))
                .selectKey((k, v) -> v.getQuizId())
                .peek((k, v) -> log.info("1f) Quiz Processing key={}, value={}", k, v))
                .transformValues(new ValueTransformerSupplier<QuizResult, QuizResult>() {
                    @Override
                    public ValueTransformer<QuizResult, QuizResult> get() {
                        return new ValueTransformer<QuizResult, QuizResult>() {
                            private ProcessorContext context;
                            public void init(final ProcessorContext context) {
                                this.context = context;
                            }
                            public QuizResult transform(final QuizResult value) {
                                Header header = context.headers().lastHeader("request_id");
                                if (header != null) {
                                    value.setRequestId(new String(header.value(), StandardCharsets.UTF_8));
                                    log.info("setting request_id... " + value.getRequestId());
                                }
                                return value;
                            }
                            public void close() {
                            }
                        };
                    }
                })

                .to(applicationProperties.getQuizResults());

        quizProcessing
                .peek((k, v) -> log.info("2) Quiz Processing key={}, value={}", k, v))
                .filter((k, v) -> v.getAnswered().size() < v.getMax())
                .peek((k, v) -> {
                    log.info("key={}, value={}", k, v);
//                            try {
//                                int id = Integer.parseInt(v.getNextQuestionId());
//                                v.setNextQuestionId("" + (id + 1));
//                            } catch (NumberFormatException e) {
//                            }
                        }
                )
                .selectKey((k, v)  -> v.getNextQuestionId())
                .join(
                        question,
                        (value1, value2) -> QuizQuestion.newBuilder()
                                .setQuizId(value1.getQuizId())
                                .setUserId(value1.getUserId())
                                .setQuestionId(value2.getQuestionId())
                                .setStatement(value2.getStatement())
                                .setA(value2.getA())
                                .setB(value2.getB())
                                .setC(value2.getC())
                                .setD(value2.getD())
                                .setDifficulty(value2.getDifficulty())
                                .build())
                .selectKey((k, v)  -> v.getQuizId())
                .transformValues(new ValueTransformerSupplier<QuizQuestion, QuizQuestion>() {
                    @Override
                    public ValueTransformer<QuizQuestion, QuizQuestion> get() {
                        return new ValueTransformer<QuizQuestion, QuizQuestion>() {
                            private ProcessorContext context;
                            public void init(final ProcessorContext context) {
                                this.context = context;
                            }
                            public QuizQuestion transform(final QuizQuestion value) {
                                Header header = context.headers().lastHeader("request_id");
                                if (header != null) {
                                    value.setRequestId(new String(header.value(), StandardCharsets.UTF_8));
                                    log.info("setting request_id... " + value.getRequestId());
                                }
                                return value;
                            }
                            public void close() {
                            }
                        };
                    }
                })
                .to(applicationProperties.getQuizNext());
//                .to((key, value, recordContext) -> bytesToString(recordContext.headers().lastHeader("request-id").value()));


        quizProcessing
                .peek((k, v) -> log.info("3a) Quiz Processing key={}, value={}", k, v))
                .mapValues((k, value1) -> createQuizStatus(value1))
                .peek((k, v) -> log.info("3b) Quiz Processing key={}, value={}", k, v))
                .selectKey((k, v) -> v.getUserId())
                .peek((k, v) -> log.info("3c) Quiz Processing key={}, value={}", k, v))
                .leftJoin(users, (quizResult, user) -> {
                    if (user != null) {
                        log.info("User: " + user.getName());
                        quizResult.setUserName(user.getName());
                    }
                    return quizResult;
                })
                .peek((k, v) -> log.info("3d) Quiz Processing key={}, value={}", k, v))
                .selectKey((k, v) -> v.getQuizId())
                .peek((k, v) -> log.info("3e) Quiz Processing key={}, value={}", k, v))
                .to(applicationProperties.getQuizStatus());


//        KStream<String, Question> quiz = streamsBuilder.<String, QuizQuestion>stream(applicationProperties.getQuizAnswer())
//                .peek((k, v) -> log.info("key={}, value={}", k, v))
//                .selectKey((k, v) -> v.getQuestionId())
//                .join(question,
    }

    private static String bytesToString(final byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static final QuizSubmission starter(final QuizStart quizStart) {
        return QuizSubmission.newBuilder()
                .setQuizId(quizStart.getQuizId())
                .setUserId(quizStart.getUserId())
                .setQuestionId(SUBMIT_TOKEN)
                .setSubmittedAnswer("")
                .setQuestionCount(quizStart.getCount())
                .setDifficulty(-1)
                .setRequestId(quizStart.getRequestId())
                .build();
    }

//    private static final QuizQuestion createDoneMarker(final Quiz quiz) {
//        return QuizQuestion.newBuilder()
//                .setQuizId(quiz.getQuizId())
//                .setUserId(quiz.getUserId())
//                .setQuestionId("")
//                .setA("")
//                .setB("")
//                .setC("")
//                .setD("")
//                .build();
//    }

    private static QuizResult createQuizResult(final Quiz quiz) {

        final int correct = (int) quiz.getAnswered().entrySet().stream()
                .filter(entry -> {
                    final QuizAnswer answer = entry.getValue();
                    return Objects.equals(answer.getCorrect(), answer.getSubmitted());
                })
                .count();

        return QuizResult.newBuilder()
                .setQuizId(quiz.getQuizId())
                .setUserId(quiz.getUserId())
                .setQuestions(quiz.getAnswered().size())
                .setCorrect(correct)
                .build();
    }

    private static QuizStatus createQuizStatus(final Quiz quiz) {

        final int correct = (int) quiz.getAnswered().entrySet().stream()
                .filter(entry -> {
                    final QuizAnswer answer = entry.getValue();
                    return Objects.equals(answer.getCorrect(), answer.getSubmitted());
                })
                .count();

        return QuizStatus.newBuilder()
                .setQuizId(quiz.getQuizId())
                .setUserId(quiz.getUserId())
                .setAnswered(quiz.getAnswered().size())
                .setQuestions(quiz.getMax())
                .setCorrect(correct)
                .build();
    }

    private static void setDifficulty(final QuizSubmission quizSubmission) {

        if (Objects.equals(quizSubmission.getSubmittedAnswer(), quizSubmission.getCorrectAnswer())) {
            quizSubmission.setDifficulty(quizSubmission.getDifficulty() + 1);
        } else {
            quizSubmission.setDifficulty(quizSubmission.getDifficulty() - 1);
        }

        if (quizSubmission.getDifficulty() < MIN_DIFFICULTY) {
            quizSubmission.setDifficulty(MIN_DIFFICULTY);
        } else if (quizSubmission.getDifficulty() > MAX_DIFFICULTY) {
            quizSubmission.setDifficulty(MAX_DIFFICULTY);
        }
    }

    private static void nextQuestion(
            final Quiz quiz,
            final KeyValueStore<Integer, ValueAndTimestamp<Questions>> questionsStore) {

        int difficulty = (quiz.getNextDifficulty() != null) ? quiz.getNextDifficulty() : 0;

        final ValueAndTimestamp<Questions> questions = questionsStore.get(difficulty);

        System.out.println(difficulty);
        List<String> ids = new ArrayList<>(questions.value().getQuestionIds());
        System.out.println(ids);
        ids.removeAll(quiz.getAnswered().keySet());
        System.out.println(ids);

        // questions exhausted, so we will pull from the entire set.
        if (ids.size() == 0) {
            ids = new ArrayList<>(questions.value().getQuestionIds());
        }


        int nextId = RANDOM.nextInt(ids.size());

        System.out.println(nextId);

        String next = ids.get(nextId);

        System.out.println("NEXT : " + next);

        quiz.setNextQuestionId(next);
    }
}
