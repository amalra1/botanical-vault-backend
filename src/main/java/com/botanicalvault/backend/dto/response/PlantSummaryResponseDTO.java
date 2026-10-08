package com.botanicalvault.backend.dto.response;

import java.util.List;

public record PlantSummaryResponseDTO(
    Long id,
    String commonName,
    String scientificName,
    TaxonomyItemResponseDTO type,
    SizeResponseDTO size,
    List<TaxonomyItemResponseDTO> origins,
    int imageCount,
    String coverUrl,
    boolean favorite
) {}
