package com.github.nbuesing.quiz.requesthandler;

import com.github.nbuesing.quiz.requesthandler.service.ResponseConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
@Slf4j
public class StartApplication implements ApplicationListener<ApplicationStartedEvent> {

    private final ApplicationContext applicationContext;

    private final ResponseConsumer<String, String> responseConsumer;
    private final TaskExecutor taskExecutor;

    public StartApplication(
            final ApplicationContext applicationContext,
            final ResponseConsumer<String, String> responseConsumer,
            final @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor) {
        this.applicationContext = applicationContext;
        this.responseConsumer = responseConsumer;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
    }

    @PostConstruct
    public void initialize() {
        taskExecutor.execute(responseConsumer::poll);
    }

    @PreDestroy
    public void close() {
        responseConsumer.close();
    }

}
