package com.whattheburger.backend.loadtest;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class LoadTestTraceFilterTest {

    private final LoadTestTraceFilter filter = new LoadTestTraceFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void echoesClientTraceIdOnLoadTestPath() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/api/v1/loadtest/store/1/inventory/deduct"
        );
        request.addHeader(LoadTestTraceSupport.HEADER_NAME, "  k6-1-0-123  ");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcDuringChain = new AtomicReference<>();

        filter.doFilter(request, response, new MockFilterChain((req, res) -> {
            mdcDuringChain.set(MDC.get(LoadTestTraceSupport.MDC_KEY));
        }));

        assertThat(response.getHeader(LoadTestTraceSupport.HEADER_NAME)).isEqualTo("k6-1-0-123");
        assertThat(mdcDuringChain.get()).isEqualTo("k6-1-0-123");
        assertThat(MDC.get(LoadTestTraceSupport.MDC_KEY)).isNull();
    }

    @Test
    void generatesTraceIdWhenHeaderMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/api/v1/loadtest/store/1/inventory/deduct"
        );
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> mdcDuringChain = new AtomicReference<>();

        filter.doFilter(request, response, new MockFilterChain((req, res) -> {
            mdcDuringChain.set(MDC.get(LoadTestTraceSupport.MDC_KEY));
        }));

        String header = response.getHeader(LoadTestTraceSupport.HEADER_NAME);
        assertThat(header).isNotBlank();
        assertThat(mdcDuringChain.get()).isEqualTo(header);
        assertThat(MDC.get(LoadTestTraceSupport.MDC_KEY)).isNull();
    }

    @Test
    void shouldNotFilterNonLoadTestPaths() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/store/1");
        request.addHeader(LoadTestTraceSupport.HEADER_NAME, "ignored");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(LoadTestTraceSupport.HEADER_NAME)).isNull();
        assertThat(MDC.get(LoadTestTraceSupport.MDC_KEY)).isNull();
    }
}
