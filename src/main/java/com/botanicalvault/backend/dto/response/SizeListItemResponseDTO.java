package com.botanicalvault.backend.dto.response;

public record SizeListItemResponseDTO(
    Long id,
    String name,
    String description,
    long usageCount
) {}
