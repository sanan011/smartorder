package com.smartorder.common.exception;

import lombok.Getter;

@Getter
public class SmartOrderException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String    details;

    public SmartOrderException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.details   = null;
    }

    public SmartOrderException(ErrorCode errorCode, String details) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.details   = details;
    }

    public SmartOrderException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode.getDefaultMessage(), cause);
        this.errorCode = errorCode;
        this.details   = details;
    }

    // ── Convenience factory methods ──────────────────────────

    public static SmartOrderException notFound(ErrorCode code) {
        return new SmartOrderException(code);
    }

    public static SmartOrderException notFound(ErrorCode code, String details) {
        return new SmartOrderException(code, details);
    }

    public static SmartOrderException conflict(ErrorCode code, String details) {
        return new SmartOrderException(code, details);
    }

    public static SmartOrderException forbidden(ErrorCode code) {
        return new SmartOrderException(code);
    }
}