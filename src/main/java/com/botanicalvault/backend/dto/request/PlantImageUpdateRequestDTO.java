package com.botanicalvault.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record PlantImageUpdateRequestDTO(
    Long id,

    @NotNull(message = "viewId is required")
    Long viewId
) {
    public static PlantImageUpdateRequestDTO newImage(Long viewId) {
        return new PlantImageUpdateRequestDTO(null, viewId);
    }

    public boolean isNew() {
        return id == null;
    }
}
