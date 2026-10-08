package com.botanicalvault.backend.constants;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST),
    IMAGE_FILES_MISMATCH(HttpStatus.BAD_REQUEST),
    DUPLICATE_IMAGE(HttpStatus.BAD_REQUEST),
    CONFLICTING_FILTERS(HttpStatus.BAD_REQUEST),
    INVALID_REASSIGNMENT(HttpStatus.BAD_REQUEST),
    INVALID_TAXONOMY_ORDER(HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    INVALID_FILE_SIGNATURE(HttpStatus.FORBIDDEN),
    DUPLICATE_NAME(HttpStatus.CONFLICT),
    FAVORITES_LOCKED(HttpStatus.CONFLICT),
    REASSIGNMENT_REQUIRED(HttpStatus.CONFLICT),
    LAST_TAXONOMY_ITEM(HttpStatus.CONFLICT),
    FILE_TOO_LARGE(HttpStatus.CONTENT_TOO_LARGE),
    INVALID_IMAGE_FORMAT(HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    BUSINESS_RULE_VIOLATION(HttpStatus.UNPROCESSABLE_CONTENT);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
