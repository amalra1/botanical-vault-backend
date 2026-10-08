package com.botanicalvault.backend.dto.response;

import java.time.Instant;
import java.util.List;

public record CollectionSummaryResponseDTO(
    Long id,
    String name,
    boolean favorites,
    int plantCount,
    List<String> mosaicUrls,
    List<SizeCoverageResponseDTO> sizeCoverage,
    Instant updatedAt
) {}
