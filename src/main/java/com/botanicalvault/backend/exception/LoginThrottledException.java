package com.botanicalvault.backend.exception;

import java.time.Duration;

import com.botanicalvault.backend.constants.ErrorCode;

public class LoginThrottledException extends BusinessException {
    private static final long MIN_RETRY_AFTER_SECONDS = 1;
    private static final long MILLIS_PER_SECOND = 1000;

    private final Duration retryAfter;

    public LoginThrottledException(Duration retryAfter) {
        super(ErrorCode.TOO_MANY_LOGIN_ATTEMPTS, "Too many login attempts");
        this.retryAfter = retryAfter;
    }

    public long retryAfterSeconds() {
        long roundedUp = Math.ceilDiv(retryAfter.toMillis(), MILLIS_PER_SECOND);
        return Math.max(MIN_RETRY_AFTER_SECONDS, roundedUp);
    }
}
