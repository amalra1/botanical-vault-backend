package com.botanicalvault.backend.dto.response;

public record PlantFacetsResponseDTO(
    FacetResponseDTO types,
    OriginFacetResponseDTO origins,
    FacetResponseDTO sizes,
    FacetResponseDTO views
) {}
