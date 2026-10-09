package com.botanicalvault.backend.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gcp.storage")
public record GcsStorageProperties(
    String bucketName,
    Duration signedUrl
) {}
