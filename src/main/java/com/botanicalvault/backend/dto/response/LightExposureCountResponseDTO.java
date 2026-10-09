package com.botanicalvault.backend.dto.response;

import com.botanicalvault.backend.constants.LightExposure;

public record LightExposureCountResponseDTO(
    LightExposure exposure,
    long count
) {}
