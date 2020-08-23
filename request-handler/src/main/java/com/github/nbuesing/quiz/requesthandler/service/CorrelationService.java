package com.github.nbuesing.quiz.requesthandler.service;

import com.github.nbuesing.quiz.requesthandler.exception.CorrelationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class CorrelationService {

    private final long timeoutMs = 5000l;

    // TODO: cleanup unused CountDownLatch and Responses via background thread

    private ConcurrentMap<String, CountDownLatch> latchByRequestId = new ConcurrentHashMap<>();
    private ConcurrentMap<String, SpecificRecordBase> responseByRequestId = new ConcurrentHashMap<>();

    public SpecificRecordBase waitForResponse(String requestId) {
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch racer = latchByRequestId.putIfAbsent(requestId, latch);
        if (racer != null) {
            // just in case the response beats the await (highly unlikely)
            latch = racer;
        }
        try {
            boolean responseReceived = latch.await(timeoutMs, TimeUnit.MILLISECONDS);
            if (responseReceived) {
                SpecificRecordBase response = responseByRequestId.remove(requestId);
                if (response == null) {
                    throw new NullPointerException("No response received for requestId:" + requestId);
                }
                return response;
            } else {
                throw new CorrelationException("Request timed out for requestId " + requestId + " after " + timeoutMs + " ms");
            }
        } catch (InterruptedException e) {
            throw new CorrelationException("Exception waiting for response", e);
        } finally {
            try {
                latchByRequestId.remove(requestId);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public void addResponse(String id, SpecificRecordBase response) {
        CountDownLatch latch = new CountDownLatch(1); // highly unlikely to ever be used, but in case the response beats the request's await
        CountDownLatch racer = latchByRequestId.putIfAbsent(id, latch);
        if (racer != null) {
            // almost always will get here
            latch = racer;
        }
        responseByRequestId.put(id, response);
        latch.countDown();
    }
}
