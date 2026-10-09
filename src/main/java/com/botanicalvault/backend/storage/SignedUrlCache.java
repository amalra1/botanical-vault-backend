package com.botanicalvault.backend.storage;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

public class SignedUrlCache {
    private static final int REUSE_FRACTION = 2;

    private final Map<String, CachedSignedUrl> entries = new ConcurrentHashMap<>();
    private final Clock clock;
    private final Duration validity;
    private final Duration reuseWindow;

    public SignedUrlCache(Clock clock, Duration validity) {
        this.clock = clock;
        this.validity = validity;
        this.reuseWindow = validity.dividedBy(REUSE_FRACTION);
    }

    public String get(String key, UnaryOperator<String> signer) {
        Instant now = clock.instant();
        CachedSignedUrl cached = entries.get(key);
        if (cached != null && isReusable(cached, now)) {
            return cached.url();
        }
        evictExpired(now);
        CachedSignedUrl fresh = new CachedSignedUrl(signer.apply(key), now.plus(validity));
        entries.put(key, fresh);
        return fresh.url();
    }

    public void evict(String key) {
        entries.remove(key);
    }

    private boolean isReusable(CachedSignedUrl cached, Instant now) {
        return now.isBefore(cached.expiresAt().minus(reuseWindow));
    }

    private void evictExpired(Instant now) {
        entries.values().removeIf(entry -> !now.isBefore(entry.expiresAt()));
    }
}
