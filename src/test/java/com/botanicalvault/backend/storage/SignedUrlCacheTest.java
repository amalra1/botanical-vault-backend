package com.botanicalvault.backend.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class SignedUrlCacheTest {
    private static final Duration VALIDITY = Duration.ofMinutes(60);
    private static final Instant NOW = Instant.parse("2026-10-08T12:00:00Z");
    private static final String KEY = "abc.png";

    private final Clock clock = mock(Clock.class);
    private final SignedUrlCache cache = new SignedUrlCache(clock, VALIDITY);
    private final AtomicInteger signatures = new AtomicInteger();

    @Test
    void reusesUrlWithinFirstHalfOfValidity() {
        String first = urlAt(NOW, KEY);
        String second = urlAt(NOW.plus(VALIDITY.dividedBy(2)).minusSeconds(1), KEY);

        assertThat(second).isEqualTo(first);
        assertThat(signatures).hasValue(1);
    }

    @Test
    void signsAgainAfterHalfOfValidity() {
        String first = urlAt(NOW, KEY);
        String second = urlAt(NOW.plus(VALIDITY.dividedBy(2)), KEY);

        assertThat(second).isNotEqualTo(first);
        assertThat(signatures).hasValue(2);
    }

    @Test
    void signsEachKeySeparately() {
        String first = urlAt(NOW, KEY);
        String other = urlAt(NOW, "other.png");

        assertThat(other).isNotEqualTo(first);
        assertThat(signatures).hasValue(2);
    }

    @Test
    void signsAgainAfterEviction() {
        urlAt(NOW, KEY);
        cache.evict(KEY);
        urlAt(NOW, KEY);

        assertThat(signatures).hasValue(2);
    }

    private String urlAt(Instant instant, String key) {
        when(clock.instant()).thenReturn(instant);
        return cache.get(key, this::sign);
    }

    private String sign(String key) {
        return key + "?signature=" + signatures.incrementAndGet();
    }
}
