package com.botanicalvault.backend.dto.response;

public record SizeCoverageResponseDTO(
    Long id,
    String name,
    String description,
    boolean covered
) {}
