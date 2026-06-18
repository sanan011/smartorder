package com.smartorder.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

/**
 * Servlet filter that ensures every inbound HTTP request carries a
 * Correlation ID and Trace ID through the entire request lifecycle.
 *
 *
 *  1. Reads X-Correlation-Id from the incoming request header.
 *     If absent, generates a new UUID.
 *  2. Reads X-Trace-Id from the incoming request header (injected by
 *     the API Gateway or Micrometer). Falls back to the correlation ID.
 *  3. Stores both in SLF4J MDC so every log line in this service
 *     automatically includes them.
 *  4. Echoes both headers back on the response so callers can correlate.
 *  5. Cleans up MDC after the request completes (critical for thread pools).
 *
 * Register this as a Spring @Bean in each service's adapter layer:
 *   @Bean
 *   public FilterRegistrationBean<CorrelationIdFilter> correlationFilter() { ... }
 */
public class CorrelationIdFilter implements Filter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String TRACE_ID_HEADER       = "X-Trace-Id";
    public static final String MDC_CORRELATION_KEY   = "correlationId";
    public static final String MDC_TRACE_KEY         = "traceId";

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest  httpRequest  = (HttpServletRequest)  request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String correlationId = resolveHeader(httpRequest, CORRELATION_ID_HEADER);
        String traceId       = resolveHeader(httpRequest, TRACE_ID_HEADER);

        // Fallback: if no trace ID from gateway, use correlation ID
        if (traceId == null || traceId.isBlank()) {
            traceId = correlationId;
        }

        try {
            // Populate MDC — available to every logger on this thread
            MDC.put(MDC_CORRELATION_KEY, correlationId);
            MDC.put(MDC_TRACE_KEY,       traceId);

            // Echo back on response so upstream callers / UI can correlate
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);
            httpResponse.setHeader(TRACE_ID_HEADER,       traceId);

            chain.doFilter(request, response);

        } finally {
            // MUST clear MDC — threads are reused from pools
            MDC.remove(MDC_CORRELATION_KEY);
            MDC.remove(MDC_TRACE_KEY);
        }
    }

    // ── Helpers ──────────────────────────────────────────────

    private String resolveHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        if (value == null || value.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return value.trim();
    }

    /**
     * Convenience accessor used by exception handlers to read the
     * current request's correlation ID from MDC without importing
     * MDC directly in every handler class.
     */
    public static String currentCorrelationId() {
        String id = MDC.get(MDC_CORRELATION_KEY);
        return (id != null) ? id : "N/A";
    }

    /**
     * Convenience accessor for the current trace ID.
     */
    public static String currentTraceId() {
        String id = MDC.get(MDC_TRACE_KEY);
        return (id != null) ? id : "N/A";
    }
}