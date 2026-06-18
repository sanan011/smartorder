package com.smartorder.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Uniform error payload returned by every service's global exception handler.
 * Serialised as JSON by the REST adapter layer.
 *
 * Example:
 * {
 *   "timestamp":  "2024-06-01T12:00:00Z",
 *   "status":     404,
 *   "errorCode":  "PRODUCT_NOT_FOUND",
 *   "message":    "Product not found.",
 *   "details":    "productId=abc-123",
 *   "traceId":    "6f3e2a1b4c9d",
 *   "path":       "/api/products/abc-123",
 *   "fieldErrors": null
 * }
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /** UTC timestamp of the error. */
    private final Instant timestamp;

    /** HTTP status code (e.g. 404). */
    private final int status;

    /** Machine-readable {@link ErrorCode} name (e.g. "PRODUCT_NOT_FOUND"). */
    private final String errorCode;

    /** Human-readable summary from {@link ErrorCode#getDefaultMessage()}. */
    private final String message;

    /** Optional extra context (e.g. which ID was missing). */
    private final String details;

    /** Distributed trace ID injected by the correlation filter. */
    private final String traceId;

    /** Request path that triggered the error. */
    private final String path;

    /**
     * Per-field validation failures — populated only for
     * {@link ErrorCode#VALIDATION_FAILED} responses.
     */
    private final List<FieldError> fieldErrors;

    // ── Static factory ───────────────────────────────────────

    public static ErrorResponse of(SmartOrderException ex,
                                   String traceId,
                                   String path) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(ex.getErrorCode().getHttpStatus().value())
                .errorCode(ex.getErrorCode().name())
                .message(ex.getMessage())
                .details(ex.getDetails())
                .traceId(traceId)
                .path(path)
                .build();
    }

    // ── Nested DTO for @Valid failures ───────────────────────

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FieldError {

        /** The field name that failed (e.g. "email"). */
        private final String field;

        private final String rejectedValue;

        /** The validation message (e.g. "must not be blank"). */
        private final String message;
    }
}