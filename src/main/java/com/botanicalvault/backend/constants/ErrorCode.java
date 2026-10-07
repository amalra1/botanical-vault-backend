package com.botanicalvault.backend.constants;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
    BUSINESS_RULE_VIOLATION(HttpStatus.UNPROCESSABLE_CONTENT);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
