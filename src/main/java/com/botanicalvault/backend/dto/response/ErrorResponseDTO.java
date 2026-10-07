package com.botanicalvault.backend.dto.response;

import java.util.Map;

public record ErrorResponseDTO(
    String error,
    String message,
    Map<String, String> fields
) {}
