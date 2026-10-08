package com.botanicalvault.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CollectionNameRequestDTO(
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 160, message = "Name must have at most 160 characters")
    String name
) {}
