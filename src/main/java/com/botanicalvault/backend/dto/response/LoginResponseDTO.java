package com.botanicalvault.backend.dto.response;

import java.time.Instant;

public record LoginResponseDTO(
    String token,
    Instant expiresAt
) {}
