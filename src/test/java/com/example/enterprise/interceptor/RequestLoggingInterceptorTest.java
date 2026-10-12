package com.example.enterprise.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class RequestLoggingInterceptorTest {

    private RequestLoggingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new RequestLoggingInterceptor();
    }

    @Test
    void preHandle_ShouldStoreStartTimeAndReturnTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        assertThat(request.getAttribute("startTime")).isNotNull();
        assertThat(request.getAttribute("startTime")).isInstanceOf(Long.class);
    }

    @Test
    void afterCompletion_WithoutException_ShouldNotThrow() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products");
        request.setAttribute("startTime", System.currentTimeMillis() - 10);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatCode(() -> interceptor.afterCompletion(request, response, new Object(), null))
                .doesNotThrowAnyException();
    }

    @Test
    void afterCompletion_WithException_ShouldNotThrow() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/products");
        request.setAttribute("startTime", System.currentTimeMillis() - 5);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatCode(() -> interceptor.afterCompletion(
                request, response, new Object(), new RuntimeException("boom")))
                .doesNotThrowAnyException();
    }

    @Test
    void postHandle_ShouldBeNoOp() {
        HttpServletRequest request = new MockHttpServletRequest();
        HttpServletResponse response = new MockHttpServletResponse();

        assertThatCode(() -> interceptor.postHandle(request, response, new Object(), null))
                .doesNotThrowAnyException();
    }
}
