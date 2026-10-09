package com.botanicalvault.backend.dto.response;

import java.util.List;

public record LightExposureFacetResponseDTO(
    long all,
    List<LightExposureCountResponseDTO> options
) {}
