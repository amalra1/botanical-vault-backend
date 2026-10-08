package com.botanicalvault.backend.dto.response;

public record CollectionImageResponseDTO(
    Long plantId,
    String commonName,
    Long imageId,
    String originalFilename,
    String url
) {}
