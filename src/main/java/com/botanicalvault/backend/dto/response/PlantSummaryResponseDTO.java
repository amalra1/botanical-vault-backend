package com.botanicalvault.backend.dto.response;

import java.util.List;

import com.botanicalvault.backend.constants.LightExposure;

public record PlantSummaryResponseDTO(
    Long id,
    String commonName,
    String scientificName,
    TaxonomyItemResponseDTO type,
    SizeResponseDTO size,
    List<TaxonomyItemResponseDTO> origins,
    List<LightExposure> lightExposures,
    int imageCount,
    String coverUrl,
    boolean favorite
) {}
