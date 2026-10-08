package com.botanicalvault.backend.dto.response;

import java.util.List;

public record PlantSearchResponseDTO(
    List<PlantSummaryResponseDTO> plants,
    PlantFacetsResponseDTO facets
) {}
