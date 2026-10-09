package com.botanicalvault.backend.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import com.botanicalvault.backend.config.LoginThrottleProperties;
import com.botanicalvault.backend.exception.LoginThrottledException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginThrottle {
    private static final Logger log = LoggerFactory.getLogger(LoginThrottle.class);
    private static final String CLIENT_SCOPE = "client";
    private static final String GLOBAL_SCOPE = "global";

    private final LoginThrottleProperties properties;
    private final Clock clock;
    private final Map<String, FailureWindow> clients = new ConcurrentHashMap<>();
    private final AtomicReference<FailureWindow> overall = new AtomicReference<>(FailureWindow.EMPTY);

    public void assertAllowed(String clientKey) {
        Instant now = clock.instant();
        FailureWindow client = clients.getOrDefault(clientKey, FailureWindow.EMPTY);
        if (client.isBlocked(properties.maxFailuresPerClient(), now, properties.window())) {
            throw new LoginThrottledException(client.retryAfter(now, properties.window()));
        }
        FailureWindow total = overall.get();
        if (total.isBlocked(properties.maxFailuresTotal(), now, properties.window())) {
            throw new LoginThrottledException(total.retryAfter(now, properties.window()));
        }
    }

    public void recordFailure(String clientKey) {
        Instant now = clock.instant();
        FailureWindow total = overall.updateAndGet(window -> window.recordFailure(now, properties.window()));
        logWhenLocked(total, properties.maxFailuresTotal(), GLOBAL_SCOPE);
        if (clients.containsKey(clientKey) || hasRoomForNewClient(now)) {
            FailureWindow client = clients.compute(clientKey, (key, window) ->
                    (window == null ? FailureWindow.EMPTY : window).recordFailure(now, properties.window()));
            logWhenLocked(client, properties.maxFailuresPerClient(), CLIENT_SCOPE);
        }
    }

    public void recordSuccess(String clientKey) {
        clients.remove(clientKey);
    }

    private boolean hasRoomForNewClient(Instant now) {
        if (clients.size() < properties.maxTrackedClients()) {
            return true;
        }
        Duration window = properties.window();
        clients.values().removeIf(failures -> failures.isExpired(now, window));
        return clients.size() < properties.maxTrackedClients();
    }

    private void logWhenLocked(FailureWindow window, int limit, String scope) {
        if (window.count() == limit) {
            log.warn("Login locked - scope: {}, window: {}", scope, properties.window());
        }
    }
}
