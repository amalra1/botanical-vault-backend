package com.botanicalvault.backend.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.login-throttle")
public record LoginThrottleProperties(
    int maxFailuresPerClient,
    int maxFailuresTotal,
    Duration window,
    int maxTrackedClients,
    boolean trustForwardedFor
) {}
