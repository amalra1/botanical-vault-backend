package com.botanicalvault.backend.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlantCreateRequestDTO(
    @NotBlank(message = "Common name cannot be blank")
    @Size(max = 160, message = "Common name must have at most 160 characters")
    String commonName,

    @Size(max = 160, message = "Scientific name must have at most 160 characters")
    String scientificName,

    @NotNull(message = "typeId is required")
    Long typeId,

    @NotNull(message = "sizeId is required")
    Long sizeId,

    List<@NotNull Long> originIds,

    String notes,

    @NotEmpty(message = "At least one image is required")
    List<@Valid @NotNull PlantImageRequestDTO> images
) implements PlantFieldsRequest {
    public PlantCreateRequestDTO {
        originIds = originIds == null ? List.of() : List.copyOf(originIds);
    }
}
