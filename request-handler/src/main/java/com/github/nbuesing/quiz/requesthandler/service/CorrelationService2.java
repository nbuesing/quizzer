package com.github.nbuesing.quiz.requesthandler.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.nbuesing.quiz.requesthandler.exception.CorrelationException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class CorrelationService2 {

    private long timeoutMs = 5000l;

    private final Cache<String, DeferredResult<ResponseEntity<JsonNode>>> cache = CacheBuilder.newBuilder().maximumSize(1000).build();

    public DeferredResult<ResponseEntity<JsonNode>> waitForResponse(String id) {
        final DeferredResult<ResponseEntity<JsonNode>> deferredResult = new DeferredResult<>(timeoutMs);
        cache.put(id, deferredResult);
        return deferredResult;
    }

    public boolean hasRequestId(String requestId) {
        return cache.getIfPresent(requestId) != null;
    }
    public void addResponse(String requestId, JsonNode response) {

        log.info("addResponse requestId={}, response={}", requestId, response);

        DeferredResult<ResponseEntity<JsonNode>> deferredResult = cache.getIfPresent(requestId);

        log.info("addResponse deferredResult={}", deferredResult);

        if (deferredResult != null) {

            log.info(">> addResponse deferredResult={}, response={}", deferredResult, response);

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-LOC", "FOO");

            ResponseEntity<JsonNode> content = new ResponseEntity<>(response, headers, HttpStatus.OK);
            deferredResult.setResult(content);
            cache.invalidate(requestId);
        }
    }
}
