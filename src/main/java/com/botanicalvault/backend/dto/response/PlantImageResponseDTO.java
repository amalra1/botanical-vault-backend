package com.botanicalvault.backend.dto.response;

public record PlantImageResponseDTO(
    Long id,
    TaxonomyItemResponseDTO view,
    String originalFilename,
    String url
) {}
