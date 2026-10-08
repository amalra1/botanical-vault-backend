package com.botanicalvault.backend.dto.response;

import java.time.Instant;
import java.util.List;

public record PlantResponseDTO(
    Long id,
    String commonName,
    String scientificName,
    TaxonomyItemResponseDTO type,
    SizeResponseDTO size,
    List<TaxonomyItemResponseDTO> origins,
    String notes,
    List<PlantImageResponseDTO> images,
    Instant createdAt,
    Instant updatedAt
) {}
