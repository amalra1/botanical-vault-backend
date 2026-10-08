package com.botanicalvault.backend.dto.response;

public record TaxonomyListItemResponseDTO(
    Long id,
    String name,
    long usageCount
) {}
