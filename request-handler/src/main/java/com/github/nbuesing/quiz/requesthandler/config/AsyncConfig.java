package com.github.nbuesing.quiz.requesthandler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.concurrent.Executors;

//@Configuration
//@EnableAsync
public class AsyncConfig  implements AsyncConfigurer {

    @Bean
    protected WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
                configurer.setTaskExecutor(applicationTaskExecutor());
            }
        };
    }

    @Bean
    protected ConcurrentTaskExecutor applicationTaskExecutor() {
        return new ConcurrentTaskExecutor(Executors.newFixedThreadPool(10));
    }
}