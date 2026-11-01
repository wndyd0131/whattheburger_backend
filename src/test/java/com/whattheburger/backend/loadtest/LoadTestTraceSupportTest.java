package com.whattheburger.backend.loadtest;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class LoadTestTraceSupportTest {

    @Test
    void resolveTraceId_trimsHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(LoadTestTraceSupport.HEADER_NAME, "  abc  ");
        assertThat(LoadTestTraceSupport.resolveTraceId(request)).isEqualTo("abc");
    }

    @Test
    void resolveTraceId_generatesWhenBlank() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(LoadTestTraceSupport.HEADER_NAME, "   ");
        assertThat(LoadTestTraceSupport.resolveTraceId(request)).isNotBlank();
    }

    @Test
    void isLoadTestPath() {
        assertThat(LoadTestTraceSupport.isLoadTestPath("/api/v1/loadtest/store/1/inventory/deduct"))
                .isTrue();
        assertThat(LoadTestTraceSupport.isLoadTestPath("/api/v1/store/1")).isFalse();
    }
}
