package com.botanicalvault.backend.dto.response;

import java.time.Instant;
import java.util.List;

import com.botanicalvault.backend.constants.LightExposure;

public record PlantResponseDTO(
    Long id,
    String commonName,
    String scientificName,
    TaxonomyItemResponseDTO type,
    SizeResponseDTO size,
    List<TaxonomyItemResponseDTO> origins,
    List<LightExposure> lightExposures,
    String notes,
    List<PlantImageResponseDTO> images,
    Instant createdAt,
    Instant updatedAt,
    boolean favorite,
    List<Long> collectionIds
) {}
