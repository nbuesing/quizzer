package com.github.nbuesing.quiz.requesthandler.exception;

public class CorrelationException extends RuntimeException {

    public CorrelationException() {
    }

    public CorrelationException(String message) {
        super(message);
    }

    public CorrelationException(String message, Throwable cause) {
        super(message, cause);
    }
}
