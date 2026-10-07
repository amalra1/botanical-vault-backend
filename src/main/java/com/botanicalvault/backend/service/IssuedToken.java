package com.botanicalvault.backend.service;

import java.time.Instant;

public record IssuedToken(
    String token,
    Instant expiresAt
) {}
