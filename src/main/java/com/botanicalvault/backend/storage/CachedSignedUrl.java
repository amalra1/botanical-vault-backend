package com.botanicalvault.backend.storage;

import java.time.Instant;

public record CachedSignedUrl(
    String url,
    Instant expiresAt
) {}
