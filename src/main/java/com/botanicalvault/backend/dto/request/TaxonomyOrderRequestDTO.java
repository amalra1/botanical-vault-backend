package com.botanicalvault.backend.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record TaxonomyOrderRequestDTO(
    @NotEmpty(message = "Ids cannot be empty")
    List<@NotNull(message = "Id cannot be null") Long> ids
) {}
