package com.botanicalvault.backend.service;

import java.time.Duration;
import java.time.Instant;

public record FailureWindow(
    int count,
    Instant startedAt
) {
    public static final FailureWindow EMPTY = new FailureWindow(0, Instant.EPOCH);

    public boolean isExpired(Instant now, Duration length) {
        return !now.isBefore(startedAt.plus(length));
    }

    public FailureWindow recordFailure(Instant now, Duration length) {
        return isExpired(now, length) ? new FailureWindow(1, now) : new FailureWindow(count + 1, startedAt);
    }

    public boolean isBlocked(int limit, Instant now, Duration length) {
        return !isExpired(now, length) && count >= limit;
    }

    public Duration retryAfter(Instant now, Duration length) {
        return Duration.between(now, startedAt.plus(length));
    }
}
