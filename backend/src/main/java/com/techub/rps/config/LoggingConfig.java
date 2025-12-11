package com.techub.rps.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.IOException;
import java.util.UUID;

@Configuration
@Slf4j
public class LoggingConfig {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String TRACE_ID_HEADER = "X-Trace-ID";
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String REQUEST_URI_KEY = "requestUri";
    private static final String HTTP_METHOD_KEY = "httpMethod";

    /**
     * Filter that adds correlation ID to MDC,
     * it passes the correlation ID available in all log statements within the request
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public Filter correlationIdFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {

                HttpServletRequest httpRequest = (HttpServletRequest) request;
                HttpServletResponse httpResponse = (HttpServletResponse) response;

                try {
                    String correlationId = getOrGenerateCorrelationId(httpRequest);
                    MDC.put(CORRELATION_ID_KEY, correlationId);
                    MDC.put(REQUEST_URI_KEY, httpRequest.getRequestURI());
                    MDC.put(HTTP_METHOD_KEY, httpRequest.getMethod());

                    httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
                    httpResponse.setHeader(TRACE_ID_HEADER, correlationId);

                    log.debug("Request started: {} {} [correlationId={}]",
                            httpRequest.getMethod(),
                            httpRequest.getRequestURI(),
                            correlationId);

                    chain.doFilter(request, response);

                    log.debug("Request completed: {} {} [correlationId={}] [status={}]",
                            httpRequest.getMethod(),
                            httpRequest.getRequestURI(),
                            correlationId,
                            httpResponse.getStatus());

                } finally {
                    MDC.remove(CORRELATION_ID_KEY);
                    MDC.remove(REQUEST_URI_KEY);
                    MDC.remove(HTTP_METHOD_KEY);
                }
            }

            private String getOrGenerateCorrelationId(HttpServletRequest request) {
                String correlationId = request.getHeader(CORRELATION_ID_HEADER);

                if (correlationId == null || correlationId.isEmpty()) {
                    correlationId = request.getHeader(TRACE_ID_HEADER);
                }

                if (correlationId == null || correlationId.isEmpty()) {
                    correlationId = UUID.randomUUID().toString();
                }

                return correlationId;
            }
        };
    }
}
