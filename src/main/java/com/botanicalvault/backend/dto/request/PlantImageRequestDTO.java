package com.botanicalvault.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record PlantImageRequestDTO(
    @NotNull(message = "viewId is required")
    Long viewId
) {}
