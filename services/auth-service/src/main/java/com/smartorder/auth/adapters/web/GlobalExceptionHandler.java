package com.smartorder.auth.adapters.web;

import com.smartorder.common.exception.ErrorCode;
import com.smartorder.common.exception.ErrorResponse;
import com.smartorder.common.exception.SmartOrderException;
import com.smartorder.common.filter.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Global exception handler for the Auth Service REST layer.
 * Converts all exceptions into the unified {@link ErrorResponse} format.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── SmartOrderException (domain errors) ───────────────────

    @ExceptionHandler(SmartOrderException.class)
    public ResponseEntity<ErrorResponse> handleSmartOrderException(
            SmartOrderException ex,
            HttpServletRequest request) {

        log.warn("Domain exception: errorCode={}, details={}, path={}",
                ex.getErrorCode(), ex.getDetails(), request.getRequestURI());

        ErrorResponse body = ErrorResponse.of(
                ex,
                CorrelationIdFilter.currentTraceId(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(body);
    }

    // ── @Valid failures ───────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String field    = (error instanceof FieldError fe) ? fe.getField() : error.getObjectName();
                    String rejected = (error instanceof FieldError fe && fe.getRejectedValue() != null)
                            ? fe.getRejectedValue().toString() : null;
                    return ErrorResponse.FieldError.builder()
                            .field(field)
                            .rejectedValue(rejected)
                            .message(error.getDefaultMessage())
                            .build();
                })
                .toList();

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(java.time.Instant.now())
                .status(400)
                .errorCode(ErrorCode.VALIDATION_FAILED.name())
                .message("Request validation failed.")
                .traceId(CorrelationIdFilter.currentTraceId())
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    // ── Spring Security access denied ─────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        SmartOrderException wrapped = new SmartOrderException(ErrorCode.ACCESS_DENIED);
        return ResponseEntity
                .status(403)
                .body(ErrorResponse.of(wrapped,
                        CorrelationIdFilter.currentTraceId(),
                        request.getRequestURI()));
    }

    // ── Catch-all ─────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled exception at path={}: {}",
                request.getRequestURI(), ex.getMessage(), ex);

        SmartOrderException wrapped = new SmartOrderException(
                ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage()
        );

        return ResponseEntity
                .status(500)
                .body(ErrorResponse.of(wrapped,
                        CorrelationIdFilter.currentTraceId(),
                        request.getRequestURI()));
    }
}