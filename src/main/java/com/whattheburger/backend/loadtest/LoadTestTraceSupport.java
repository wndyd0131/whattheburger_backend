package com.whattheburger.backend.loadtest;

import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public final class LoadTestTraceSupport {

    public static final String HEADER_NAME = "X-Trace-Id";
    public static final String MDC_KEY = "traceId";
    public static final String PATH_PREFIX = "/api/v1/loadtest/";

    private LoadTestTraceSupport() {}

    public static String resolveTraceId(HttpServletRequest request) {
        String fromHeader = request.getHeader(HEADER_NAME);
        if (fromHeader != null) {
            String trimmed = fromHeader.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return UUID.randomUUID().toString();
    }

    public static boolean isLoadTestPath(String requestUri) {
        return requestUri != null && requestUri.startsWith(PATH_PREFIX);
    }
}
