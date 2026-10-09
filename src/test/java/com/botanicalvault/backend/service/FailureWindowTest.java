package com.botanicalvault.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

class FailureWindowTest {
    private static final Instant NOW = Instant.parse("2026-10-09T12:00:00Z");
    private static final Duration WINDOW = Duration.ofMinutes(15);

    @Test
    void emptyWindowStartsCountingAtTheFirstFailure() {
        FailureWindow window = FailureWindow.EMPTY.recordFailure(NOW, WINDOW);

        assertThat(window).isEqualTo(new FailureWindow(1, NOW));
    }

    @Test
    void failuresInsideTheWindowAccumulate() {
        FailureWindow window = new FailureWindow(2, NOW).recordFailure(NOW.plusSeconds(60), WINDOW);

        assertThat(window).isEqualTo(new FailureWindow(3, NOW));
    }

    @Test
    void expiredWindowRestarts() {
        FailureWindow window = new FailureWindow(9, NOW).recordFailure(NOW.plus(WINDOW), WINDOW);

        assertThat(window).isEqualTo(new FailureWindow(1, NOW.plus(WINDOW)));
    }

    @Test
    void blocksOnlyWhileTheLimitIsReachedInsideTheWindow() {
        FailureWindow window = new FailureWindow(5, NOW);

        assertThat(window.isBlocked(5, NOW.plusSeconds(1), WINDOW)).isTrue();
        assertThat(window.isBlocked(6, NOW.plusSeconds(1), WINDOW)).isFalse();
        assertThat(window.isBlocked(5, NOW.plus(WINDOW), WINDOW)).isFalse();
    }

    @Test
    void retryAfterIsTheTimeLeftInTheWindow() {
        assertThat(new FailureWindow(5, NOW).retryAfter(NOW.plusSeconds(60), WINDOW))
                .isEqualTo(WINDOW.minusSeconds(60));
    }
}
