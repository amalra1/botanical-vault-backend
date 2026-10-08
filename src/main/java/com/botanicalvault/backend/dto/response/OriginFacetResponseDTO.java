package com.botanicalvault.backend.dto.response;

import java.util.List;

public record OriginFacetResponseDTO(
    long all,
    long unclassified,
    List<FacetCountResponseDTO> options
) {}
