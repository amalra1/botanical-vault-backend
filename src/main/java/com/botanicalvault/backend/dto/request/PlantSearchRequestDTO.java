package com.botanicalvault.backend.dto.request;

import com.botanicalvault.backend.constants.LightExposure;
import com.botanicalvault.backend.constants.PlantSort;
import jakarta.validation.constraints.Size;

public record PlantSearchRequestDTO(
    @Size(max = 160, message = "Search must have at most 160 characters")
    String q,

    Long typeId,

    Long originId,

    Boolean unclassified,

    Long sizeId,

    Long viewId,

    LightExposure lightExposure,

    PlantSort sort
) {
    public PlantSearchRequestDTO {
        unclassified = Boolean.TRUE.equals(unclassified);
        sort = sort == null ? PlantSort.NAME : sort;
    }
}
