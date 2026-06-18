package com.smartorder.product.adapters.web;

import com.smartorder.common.exception.ErrorCode;
import com.smartorder.common.exception.ErrorResponse;
import com.smartorder.common.exception.SmartOrderException;
import com.smartorder.common.filter.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SmartOrderException.class)
    public ResponseEntity<ErrorResponse> handleDomain(
            SmartOrderException ex, HttpServletRequest req) {
        log.warn("Domain error: {} - {}", ex.getErrorCode(), ex.getDetails());
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus())
                .body(ErrorResponse.of(ex,
                        CorrelationIdFilter.currentTraceId(), req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getAllErrors().stream()
                .map(e -> ErrorResponse.FieldError.builder()
                        .field(e instanceof FieldError fe ? fe.getField() : e.getObjectName())
                        .message(e.getDefaultMessage())
                        .build())
                .toList();

        return ResponseEntity.badRequest().body(
                ErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(400)
                        .errorCode(ErrorCode.VALIDATION_FAILED.name())
                        .message("Validation failed")
                        .traceId(CorrelationIdFilter.currentTraceId())
                        .path(req.getRequestURI())
                        .fieldErrors(fieldErrors)
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.of(
                        new SmartOrderException(ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage()),
                        CorrelationIdFilter.currentTraceId(), req.getRequestURI()));
    }
}