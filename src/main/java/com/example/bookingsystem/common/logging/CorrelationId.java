package com.example.bookingsystem.common.logging;

import org.slf4j.MDC;

import java.util.UUID;

public final class CorrelationId {

    public static final String MDC_KEY = "correlationId";
    public static final String HEADER = "X-Correlation-ID";

    private CorrelationId() {}

    public static String current() {
        return MDC.get(MDC_KEY);
    }

    public static String currentOrCreate() {
        String correlationId = current();

        return correlationId != null
                ? correlationId
                : generate();
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
