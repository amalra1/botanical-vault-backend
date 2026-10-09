package com.botanicalvault.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.IntStream;

import com.botanicalvault.backend.TestClock;
import com.botanicalvault.backend.config.LoginThrottleProperties;
import com.botanicalvault.backend.exception.LoginThrottledException;
import org.junit.jupiter.api.Test;

class LoginThrottleTest {
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final int PER_CLIENT = 5;
    private static final int TOTAL = 30;
    private static final int TRACKED_CLIENTS = 3;
    private static final String CLIENT = "203.0.113.7";
    private static final String OTHER_CLIENT = "198.51.100.4";

    private final TestClock clock = new TestClock(Instant.parse("2026-10-09T12:00:00Z"));
    private final LoginThrottle throttle = new LoginThrottle(
            new LoginThrottleProperties(PER_CLIENT, TOTAL, WINDOW, TRACKED_CLIENTS, false), clock);

    @Test
    void blocksAClientAfterTheFailureLimit() {
        fail(CLIENT, PER_CLIENT - 1);
        assertThatCode(() -> throttle.assertAllowed(CLIENT)).doesNotThrowAnyException();

        fail(CLIENT, 1);

        assertThatThrownBy(() -> throttle.assertAllowed(CLIENT))
                .isInstanceOfSatisfying(LoginThrottledException.class,
                        ex -> assertThat(ex.retryAfterSeconds()).isEqualTo(WINDOW.toSeconds()));
        assertThatCode(() -> throttle.assertAllowed(OTHER_CLIENT)).doesNotThrowAnyException();
    }

    @Test
    void releasesTheClientWhenTheWindowExpires() {
        fail(CLIENT, PER_CLIENT);
        clock.advance(WINDOW);

        assertThatCode(() -> throttle.assertAllowed(CLIENT)).doesNotThrowAnyException();
    }

    @Test
    void successClearsOnlyThatClient() {
        fail(CLIENT, PER_CLIENT - 1);
        throttle.recordSuccess(CLIENT);
        fail(CLIENT, PER_CLIENT - 1);

        assertThatCode(() -> throttle.assertAllowed(CLIENT)).doesNotThrowAnyException();
    }

    @Test
    void globalLockClosesLoginForEveryoneAfterTheTotalLimit() {
        IntStream.range(0, TOTAL).forEach(index -> throttle.recordFailure("10.0.0." + index));

        assertThatThrownBy(() -> throttle.assertAllowed(OTHER_CLIENT)).isInstanceOf(LoginThrottledException.class);
        clock.advance(WINDOW);
        assertThatCode(() -> throttle.assertAllowed(OTHER_CLIENT)).doesNotThrowAnyException();
    }

    @Test
    void untrackedClientsStillCountTowardsTheGlobalLock() {
        IntStream.range(0, TRACKED_CLIENTS).forEach(index -> throttle.recordFailure("10.0.1." + index));
        fail(CLIENT, PER_CLIENT);

        assertThatCode(() -> throttle.assertAllowed(CLIENT)).doesNotThrowAnyException();
        fail(CLIENT, TOTAL - TRACKED_CLIENTS - PER_CLIENT);
        assertThatThrownBy(() -> throttle.assertAllowed(CLIENT)).isInstanceOf(LoginThrottledException.class);
    }

    @Test
    void expiredClientsMakeRoomForNewOnes() {
        IntStream.range(0, TRACKED_CLIENTS).forEach(index -> throttle.recordFailure("10.0.2." + index));
        clock.advance(WINDOW);
        fail(CLIENT, PER_CLIENT);

        assertThatThrownBy(() -> throttle.assertAllowed(CLIENT)).isInstanceOf(LoginThrottledException.class);
    }

    private void fail(String client, int times) {
        IntStream.range(0, times).forEach(attempt -> throttle.recordFailure(client));
    }
}
