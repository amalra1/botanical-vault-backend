package com.botanicalvault.backend.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage.local")
public record LocalStorageProperties(
    String directory,
    String publicBaseUrl,
    String signingSecret,
    Duration signedUrl
) {}
