package com.botanicalvault.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SizeRequestDTO(
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 80, message = "Name must have at most 80 characters")
    String name,

    @Size(max = 80, message = "Description must have at most 80 characters")
    String description
) {}
