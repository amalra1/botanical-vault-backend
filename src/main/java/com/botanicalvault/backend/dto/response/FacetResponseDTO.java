package com.botanicalvault.backend.dto.response;

import java.util.List;

public record FacetResponseDTO(
    long all,
    List<FacetCountResponseDTO> options
) {}
