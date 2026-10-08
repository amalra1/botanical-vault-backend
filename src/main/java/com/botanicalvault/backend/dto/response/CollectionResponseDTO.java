package com.botanicalvault.backend.dto.response;

import java.time.Instant;
import java.util.List;

public record CollectionResponseDTO(
    Long id,
    String name,
    boolean favorites,
    List<PlantSummaryResponseDTO> plants,
    List<SizeCoverageResponseDTO> sizeCoverage,
    Instant updatedAt
) {}
